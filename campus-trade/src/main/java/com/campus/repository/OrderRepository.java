package com.campus.repository;
import com.campus.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);
    Page<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);
    Page<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);
    boolean existsByGoodsIdAndBuyerIdAndStatusIn(Long goodsId, Long buyerId, List<String> statuses);
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status='FINISHED'") long countFinished();
    @Query("SELECT COALESCE(SUM(o.price),0) FROM Order o WHERE o.status='FINISHED'") BigDecimal sumFinishedAmount();
}
