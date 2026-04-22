package com.campus.repository;
import com.campus.entity.Review;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);
    boolean existsByOrderIdAndReviewerId(Long orderId, Long reviewerId);
    @Query("SELECT AVG(r.score) FROM Review r WHERE r.revieweeId = :userId")
    Double avgScoreByRevieweeId(@Param("userId") Long userId);
}
