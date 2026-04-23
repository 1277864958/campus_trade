package com.campus.repository;
import com.campus.entity.Goods;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
public interface GoodsRepository extends JpaRepository<Goods, Long> {
    Page<Goods> findByStatus(String status, Pageable pageable);
    Page<Goods> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);
    Page<Goods> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, String status, Pageable pageable);

    @Query("SELECT g FROM Goods g WHERE g.status = 'ON_SALE'" +
            " AND (:keyword IS NULL OR g.title LIKE %:keyword% OR g.description LIKE %:keyword%)" +
            " AND (:categoryId IS NULL OR g.categoryId IN :categoryIds)" + // 改为 IN
            " AND (:minPrice IS NULL OR g.price >= :minPrice)" +
            " AND (:maxPrice IS NULL OR g.price <= :maxPrice)")
    Page<Goods> search(@Param("keyword") String keyword,
                       @Param("categoryId") Long categoryId, // 保留用于判空
                       @Param("categoryIds") java.util.List<Long> categoryIds, // 新增列表匹配
                       @Param("minPrice") BigDecimal minPrice,
                       @Param("maxPrice") BigDecimal maxPrice,
                       Pageable pageable);

    @Modifying @Transactional
    @Query("UPDATE Goods g SET g.views = g.views + 1 WHERE g.id = :id")
    void incrementViews(@Param("id") Long id);
}
