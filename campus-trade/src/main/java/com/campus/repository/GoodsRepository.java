package com.campus.repository;

import com.campus.entity.Goods;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

public interface GoodsRepository extends JpaRepository<Goods, Long> {

    // 简单的命名查询，@SQLRestriction 会自动生效
    Page<Goods> findByStatus(String status, Pageable pageable);
    Page<Goods> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);
    Page<Goods> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, String status, Pageable pageable);

    // 【核心修复 1】：显式加上 AND g.isDeleted = false，防止 @SQLRestriction 在复杂查询中失效
    // 【优化 2】：增加 :keyword = '' 判断，防止前端传空字符串时退化为全表扫
    // 【优化 3】：修复 LIKE %:keyword% 的非标准 JPQL 语法，改用标准的 CONCAT
    @Query("SELECT g FROM Goods g WHERE g.status = 'ON_SALE' AND g.isDeleted = false" +
            " AND (:keyword IS NULL OR :keyword = '' OR g.title LIKE CONCAT('%', :keyword, '%') OR g.description LIKE CONCAT('%', :keyword, '%'))" +
            " AND (:categoryId IS NULL OR g.categoryId IN :categoryIds)" +
            " AND (:minPrice IS NULL OR g.price >= :minPrice)" +
            " AND (:maxPrice IS NULL OR g.price <= :maxPrice)")
    Page<Goods> search(@Param("keyword") String keyword,
                       @Param("categoryId") Long categoryId,
                       @Param("categoryIds") java.util.List<Long> categoryIds,
                       @Param("minPrice") BigDecimal minPrice,
                       @Param("maxPrice") BigDecimal maxPrice,
                       Pageable pageable);

    @Modifying
    @Transactional
    // 【优化 4】：更新浏览量时也强制排除已删除的商品，防止“幽灵数据”被意外更新
    @Query("UPDATE Goods g SET g.views = g.views + 1 WHERE g.id = :id AND g.isDeleted = false")
    void incrementViews(@Param("id") Long id);

    // 在 GoodsRepository 中追加：

    // 【核心防超卖】：利用数据库行锁进行原子级别的状态更新
    @Modifying
    @Transactional
    @Query("UPDATE Goods g SET g.status = 'RESERVED' WHERE g.id = :id AND g.status = 'ON_SALE' AND g.isDeleted = false")
    int lockGoodsForOrder(@Param("id") Long id);
}