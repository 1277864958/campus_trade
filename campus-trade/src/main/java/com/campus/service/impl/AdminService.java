package com.campus.service.impl;
import com.campus.common.exception.BusinessException;
import com.campus.dto.resp.*;
import com.campus.entity.User;
import com.campus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository   userRepo;
    private final GoodsRepository  goodsRepo;
    private final OrderRepository  orderRepo;
    private final ReportRepository reportRepo;
    private final PasswordEncoder  encoder;

    public AdminStatResp stat() {
        AdminStatResp s = new AdminStatResp();
        s.setTotalUsers(userRepo.count()); s.setTotalGoods(goodsRepo.count());
        s.setTotalOrders(orderRepo.count()); s.setFinishedOrders(orderRepo.countFinished());
        s.setTotalAmount(orderRepo.sumFinishedAmount());
        s.setPendingReports(reportRepo.findByStatusOrderByCreatedAtDesc(
                "PENDING", PageRequest.of(0,1)).getTotalElements());
        return s;
    }

    public PageResp<UserResp> users(int page, int size) {
        Page<User> p = userRepo.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResp.of(p.getContent().stream().map(u -> AuthService.toResp(u, null)).toList(),
                p.getTotalElements(), page, size);
    }

    @Transactional
    public void setStatus(Long userId, int status) {
        User u = userRepo.findById(userId).orElseThrow(() -> BusinessException.notFound("用户不存在"));
        u.setStatus(status); userRepo.save(u);
    }

    @Transactional
    public void resetPassword(Long userId, String newPwd) {
        User u = userRepo.findById(userId).orElseThrow(() -> BusinessException.notFound("用户不存在"));
        u.setPasswordHash(encoder.encode(newPwd)); userRepo.save(u);
    }
}
