package com.campus.repository;
import com.campus.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterIdAndGoodsId(Long reporterId, Long goodsId);
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Report> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}
