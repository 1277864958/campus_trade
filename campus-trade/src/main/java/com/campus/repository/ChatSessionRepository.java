package com.campus.repository;
import com.campus.entity.ChatSession;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findByBuyerIdAndSellerIdAndGoodsId(Long buyerId, Long sellerId, Long goodsId);
    List<ChatSession> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(Long buyerId, Long sellerId);

    // 在 ChatSessionRepository 接口中追加：
    List<ChatSession> findByGoodsId(Long goodsId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChatSession c WHERE c.goodsId = :goodsId")
    void deleteByGoodsId(@Param("goodsId") Long goodsId);


}
