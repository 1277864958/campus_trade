package com.campus.service.impl;
import com.campus.common.exception.BusinessException;
import com.campus.dto.req.ReportReq;
import com.campus.dto.resp.*;
import com.campus.entity.*;
import com.campus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepo;
    private final GoodsRepository  goodsRepo;
    private final UserRepository   userRepo;

    @Transactional
    public void submit(Long reporterId, ReportReq req) {
        if (!goodsRepo.existsById(req.getGoodsId())) throw BusinessException.notFound("商品不存在");
        if (reportRepo.existsByReporterIdAndGoodsId(reporterId, req.getGoodsId()))
            throw BusinessException.of("您已举报过该商品，请勿重复举报");
        Report r = new Report();
        r.setReporterId(reporterId); r.setGoodsId(req.getGoodsId()); r.setReason(req.getReason());
        reportRepo.save(r);
    }

    public PageResp<ReportResp> adminList(String status, int page, int size) {
        Pageable pg = PageRequest.of(page, size);
        Page<Report> p = (status == null || status.isBlank())
                ? reportRepo.findAllByOrderByCreatedAtDesc(pg)
                : reportRepo.findByStatusOrderByCreatedAtDesc(status, pg);
        return PageResp.of(p.getContent().stream().map(this::toResp).toList(), p.getTotalElements(), page, size);
    }

    @Transactional
    public void handle(Long reportId, String action) {
        Report r = reportRepo.findById(reportId).orElseThrow(() -> BusinessException.notFound("举报不存在"));
        if (!"PENDING".equals(r.getStatus())) throw BusinessException.of("该举报已处理");
        r.setStatus("handle".equals(action) ? "HANDLED" : "DISMISSED");
        reportRepo.save(r);
    }

    private ReportResp toResp(Report r) {
        ReportResp resp = new ReportResp(); BeanUtils.copyProperties(r, resp);
        userRepo.findById(r.getReporterId()).ifPresent(u ->
            resp.setReporterName(u.getNickname() != null ? u.getNickname() : u.getUsername()));
        goodsRepo.findById(r.getGoodsId()).ifPresent(g -> resp.setGoodsTitle(g.getTitle()));
        return resp;
    }
}
