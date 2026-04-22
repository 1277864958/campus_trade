package com.campus.repository;
import com.campus.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findByBuyerIdAndSellerIdAndGoodsId(Long buyerId, Long sellerId, Long goodsId);
    List<ChatSession> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(Long buyerId, Long sellerId);
}
