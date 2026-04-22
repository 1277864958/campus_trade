package com.campus.repository;
import com.campus.entity.BrowseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
public interface BrowseHistoryRepository extends JpaRepository<BrowseHistory, Long> {
    Page<BrowseHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    @Modifying @Transactional
    @Query("DELETE FROM BrowseHistory b WHERE b.userId=:uid AND b.goodsId=:gid")
    void deleteByUserIdAndGoodsId(@Param("uid") Long userId, @Param("gid") Long goodsId);
}
