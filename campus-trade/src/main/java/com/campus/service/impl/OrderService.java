package com.campus.service.impl;

import com.campus.common.exception.BusinessException;
import com.campus.dto.req.OrderReq;
import com.campus.dto.req.ReviewReq;
import com.campus.dto.resp.*;
import com.campus.entity.*;
import com.campus.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务：创建 / 确认 / 完成 / 取消 / 评价
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository  orderRepo;
    private final GoodsRepository  goodsRepo;
    private final UserRepository   userRepo;
    private final ReviewRepository reviewRepo;
    private final GoodsService     goodsService;

    // ── 创建订单（买家下单）──────────────────────────────────
    @Transactional
    public OrderResp create(Long buyerId, OrderReq req) {
        Goods g = goodsRepo.findById(req.getGoodsId())
                .orElseThrow(() -> BusinessException.notFound("商品不存在"));

        // 1. 基础校验（内存级拦截）
        if (!"ON_SALE".equals(g.getStatus()))
            throw BusinessException.of("该商品当前不可购买");
        if (g.getSellerId().equals(buyerId))
            throw BusinessException.of("不能购买自己的商品");
        if (orderRepo.existsByGoodsIdAndBuyerIdAndStatusIn(
                g.getId(), buyerId, List.of("PENDING", "CONFIRMED")))
            throw BusinessException.of("您已对该商品下单，请勿重复操作");

        // 2. 【防超卖核心】：执行原子抢占。
        // 如果返回的 updatedRow 为 0，说明在这短短几毫秒的并发中，商品已经被别人抢先变为 RESERVED 了
        int updatedRow = goodsRepo.lockGoodsForOrder(g.getId());
        if (updatedRow == 0) {
            throw BusinessException.of("手慢了，该商品已被其他同学抢先下单");
        }

        // 3. 抢占成功，安全生成订单
        Order o = new Order();
        o.setOrderNo(generateOrderNo());
        o.setBuyerId(buyerId);
        o.setSellerId(g.getSellerId());
        o.setGoodsId(g.getId());
        o.setPrice(g.getPrice());
        o.setRemark(req.getRemark());
        o.setStatus("PENDING");
        orderRepo.save(o);

        // 注意：这里不需要再调用 goodsRepo.save(g) 了，因为上面的 SQL 已经完成了更新

        log.info("订单创建成功: {} 买家:{} 商品:{}", o.getOrderNo(), buyerId, g.getId());
        return toResp(o);
    }

    // ── 卖家确认订单 ─────────────────────────────────────────
    @Transactional
    public OrderResp confirm(Long sellerId, Long orderId) {
        Order o = getOrder(orderId);
        if (!o.getSellerId().equals(sellerId))
            throw BusinessException.forbidden("无权操作此订单");
        if (!"PENDING".equals(o.getStatus()))
            throw BusinessException.of("当前订单状态不可确认");

        o.setStatus("CONFIRMED");
        o.setPaidAt(LocalDateTime.now());
        return toResp(orderRepo.save(o));
    }

    // ── 买家确认收货（完成交易）──────────────────────────────
// campus-trade-complete/campus-trade/src/main/java/com/campus/service/impl/OrderService.java

    @Transactional
    public OrderResp finish(Long buyerId, Long orderId) {
        Order o = getOrder(orderId);
        if (!o.getBuyerId().equals(buyerId))
            throw BusinessException.forbidden("无权操作此订单");
        if (!"CONFIRMED".equals(o.getStatus()))
            throw BusinessException.of("请等待卖家确认后再完成交易");

        o.setStatus("FINISHED");
        o.setFinishedAt(LocalDateTime.now());

        // 优化：使用 Repository 直接更新，减少一次 findById 和可能的 Version 冲突风险
        goodsRepo.findById(o.getGoodsId()).ifPresent(g -> {
            if ("SOLD".equals(g.getStatus())) {
                throw BusinessException.of("商品已售出");
            }
            g.setStatus("SOLD");
            goodsRepo.save(g);
        });

        Order savedOrder = orderRepo.save(o);
        return toResp(savedOrder);
    }

    // ── 取消订单（买家 PENDING 状态下可取消）────────────────
    @Transactional
    public OrderResp cancel(Long userId, Long orderId) {
        Order o = getOrder(orderId);
        boolean isBuyer  = o.getBuyerId().equals(userId);
        boolean isSeller = o.getSellerId().equals(userId);

        if (!isBuyer && !isSeller)
            throw BusinessException.forbidden("无权操作此订单");
        if ("FINISHED".equals(o.getStatus()) || "CANCELED".equals(o.getStatus()))
            throw BusinessException.of("订单已结束，无法取消");
        if (isSeller && "CONFIRMED".equals(o.getStatus()))
            throw BusinessException.of("订单已确认，请联系买家协商");

        o.setStatus("CANCELED");
        o.setCancelAt(LocalDateTime.now());

        // 商品恢复在售
        goodsRepo.findById(o.getGoodsId()).ifPresent(g -> {
            if ("RESERVED".equals(g.getStatus())) {
                g.setStatus("ON_SALE");
                goodsRepo.save(g);
            }
        });
        return toResp(orderRepo.save(o));
    }

    // ── 提交评价 ─────────────────────────────────────────────
    @Transactional
    public ReviewResp submitReview(Long reviewerId, ReviewReq req) {
        Order o = getOrder(req.getOrderId());
        if (!"FINISHED".equals(o.getStatus()))
            throw BusinessException.of("只有已完成的订单才能评价");

        boolean isBuyer  = o.getBuyerId().equals(reviewerId);
        boolean isSeller = o.getSellerId().equals(reviewerId);
        if (!isBuyer && !isSeller)
            throw BusinessException.forbidden("您不是此订单的参与方");
        if (reviewRepo.existsByOrderIdAndReviewerId(o.getId(), reviewerId))
            throw BusinessException.of("您已评价过此订单");

        Long revieweeId = isBuyer ? o.getSellerId() : o.getBuyerId();

        Review rv = new Review();
        rv.setOrderId(o.getId());
        rv.setReviewerId(reviewerId);
        rv.setRevieweeId(revieweeId);
        rv.setScore(req.getScore());
        rv.setContent(req.getContent());
        reviewRepo.save(rv);
        return toReviewResp(rv);
    }

    // ── 我的购买订单 ─────────────────────────────────────────
    public PageResp<OrderResp> myPurchases(Long buyerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> p = orderRepo.findByBuyerIdOrderByCreatedAtDesc(buyerId, pageable);
        return toPageResp(p, page, size);
    }

    // ── 我的出售订单 ─────────────────────────────────────────
    public PageResp<OrderResp> mySales(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> p = orderRepo.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
        return toPageResp(p, page, size);
    }

    // ── 用户收到的评价 ────────────────────────────────────────
    public List<ReviewResp> userReviews(Long userId) {
        return reviewRepo.findByRevieweeIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toReviewResp).toList();
    }

    // ── 私有辅助 ─────────────────────────────────────────────
    private Order getOrder(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> BusinessException.notFound("订单不存在"));
    }

    private String generateOrderNo() {
        String ts   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String rand = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        return ts + rand;
    }

    private PageResp<OrderResp> toPageResp(Page<Order> p, int page, int size) {
        return PageResp.of(p.getContent().stream().map(this::toResp).toList(),
                p.getTotalElements(), page, size);
    }

    OrderResp toResp(Order o) {
        OrderResp r = new OrderResp();
        BeanUtils.copyProperties(o, r);

        userRepo.findById(o.getBuyerId()).ifPresent(u ->
                r.setBuyerName(u.getNickname() != null ? u.getNickname() : u.getUsername()));
        userRepo.findById(o.getSellerId()).ifPresent(u ->
                r.setSellerName(u.getNickname() != null ? u.getNickname() : u.getUsername()));
        goodsRepo.findById(o.getGoodsId()).ifPresent(g -> {
            r.setGoodsTitle(g.getTitle());
            GoodsResp gr = goodsService.toResp(g);
            r.setGoodsCover(gr.getCoverUrl());
        });
        // 评价状态
        r.setBuyerReviewed(reviewRepo.existsByOrderIdAndReviewerId(o.getId(), o.getBuyerId()));
        r.setSellerReviewed(reviewRepo.existsByOrderIdAndReviewerId(o.getId(), o.getSellerId()));
        return r;
    }

    private ReviewResp toReviewResp(Review rv) {
        ReviewResp r = new ReviewResp();
        BeanUtils.copyProperties(rv, r);
        userRepo.findById(rv.getReviewerId()).ifPresent(u -> {
            r.setReviewerName(u.getNickname() != null ? u.getNickname() : u.getUsername());
            r.setReviewerAvatar(u.getAvatarUrl());
        });
        return r;
    }
}
