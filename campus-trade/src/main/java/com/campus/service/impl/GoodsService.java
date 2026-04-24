package com.campus.service.impl;

import com.campus.common.exception.BusinessException;
import com.campus.dto.req.GoodsReq;
import com.campus.dto.req.GoodsSearchReq;
import com.campus.dto.resp.*;
import com.campus.entity.*;
import com.campus.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务：发布、编辑、上下架、搜索、浏览记录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsService {

    private final GoodsRepository        goodsRepo;
    private final GoodsImageRepository   imageRepo;
    private final CategoryRepository     categoryRepo;
    private final UserRepository         userRepo;
    private final BrowseHistoryRepository browseRepo;
    private final ReviewRepository       reviewRepo;

    private final ChatSessionRepository  chatSessionRepo;
    private final MessageRepository      messageRepo;
    private final ChatService            chatService;

    // ── 发布商品 ──────────────────────────────────────────────
    @Transactional
    public GoodsResp publish(Long sellerId, GoodsReq req) {
        validateCategory(req.getCategoryId());

        Goods g = new Goods();
        BeanUtils.copyProperties(req, g);
        g.setSellerId(sellerId);
        g.setStatus(req.getStatus() != null ? req.getStatus() : "DRAFT");

        // 【核心修复】：重新赋值给 g，获取包含了自增主键 ID 的持久化对象
        g = goodsRepo.save(g);

        // 此时 g.getId() 绝对有值，不会再引发外键 null 异常
        saveImages(g.getId(), req.getImageUrls());

        return toResp(g);
    }

    // ── 编辑商品（仅卖家本人）────────────────────────────────
    @Transactional
    public GoodsResp update(Long userId, Long goodsId, GoodsReq req) {
        Goods g = getOwnGoods(userId, goodsId);
        if ("SOLD".equals(g.getStatus()))
            throw BusinessException.of("已售出商品不能编辑");

        if (req.getTitle()         != null) g.setTitle(req.getTitle());
        if (req.getDescription()   != null) g.setDescription(req.getDescription());
        if (req.getPrice()         != null) g.setPrice(req.getPrice());
        if (req.getOriginalPrice() != null) g.setOriginalPrice(req.getOriginalPrice());
        if (req.getCategoryId()    != null) { validateCategory(req.getCategoryId()); g.setCategoryId(req.getCategoryId()); }
        if (req.getLocation()      != null) g.setLocation(req.getLocation());

        // 重置图片
        if (req.getImageUrls() != null) {
            imageRepo.deleteByGoodsId(goodsId);
            saveImages(goodsId, req.getImageUrls());
        }
        return toResp(goodsRepo.save(g));
    }

    // ── 上架 ─────────────────────────────────────────────────
    @Transactional
    public void putOnSale(Long userId, Long goodsId) {
        Goods g = getOwnGoods(userId, goodsId);
        if ("SOLD".equals(g.getStatus())) throw BusinessException.of("已售出商品不能上架");
        g.setStatus("ON_SALE");
        goodsRepo.save(g);
    }

    // ── 下架 ─────────────────────────────────────────────────
    @Transactional
    public void takeDown(Long userId, Long goodsId) {
        Goods g = getOwnGoods(userId, goodsId);
        g.setStatus("DRAFT");
        goodsRepo.save(g);
    }

    // ── 删除商品 ──────────────────────────────────────────────
    @Transactional
    public void delete(Long userId, Long goodsId) {
        Goods g = getOwnGoods(userId, goodsId);

        if ("RESERVED".equals(g.getStatus()) || "SOLD".equals(g.getStatus()))
            throw BusinessException.of("交易中或已售出的商品不能删除");
        imageRepo.deleteByGoodsId(goodsId);

        // 先查出该商品关联的所有聊天会话
        List<ChatSession> sessions = chatSessionRepo.findByGoodsId(goodsId);
        for (ChatSession session : sessions) {
            // 删除会话底下的所有具体聊天消息
            messageRepo.deleteByChatId(session.getId());
        }
        // 再删除聊天会话本身
        chatSessionRepo.deleteByGoodsId(goodsId);
// 2. 调用 ChatService 的级联清理方法 (注入 ChatService)
        chatService.deleteSessionsByGoodsId(goodsId);
        goodsRepo.delete(g);
    }

    // ── 商品详情（含浏览计数）────────────────────────────────
    @Transactional
    public GoodsResp getDetail(Long goodsId, Long currentUserId) {
        Goods g = goodsRepo.findById(goodsId)
                .orElseThrow(() -> BusinessException.notFound("商品不存在"));
        // 异步增加浏览量 + 记录浏览历史
        incrementViewsAsync(goodsId, currentUserId);
        return toResp(g);
    }

    // ── 搜索商品 ──────────────────────────────────────────────
// ── 搜索商品 ──────────────────────────────────────────────
    public PageResp<GoodsResp> search(GoodsSearchReq req) {
        Sort sort = switch (req.getSortBy()) {
            case "price_asc"  -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "views"      -> Sort.by("views").descending();
            default           -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        // 关键修复：构造包含当前ID及其所有子级ID的列表
        java.util.List<Long> categoryIds = new java.util.ArrayList<>();
        if (req.getCategoryId() != null) {
            categoryIds.add(req.getCategoryId());
            // categoryRepo 已在 GoodsService 顶部被注入
            categoryRepo.findByParentIdOrderBySortAsc(req.getCategoryId())
                    .forEach(child -> categoryIds.add(child.getId()));
        } else {
            categoryIds.add(-1L); // 防止 categoryId 为空时 IN () 语法报错
        }

        // 调用更新后的 Repo 方法
        Page<Goods> page  = goodsRepo.search(
                req.getKeyword(), req.getCategoryId(), categoryIds,
                req.getMinPrice(), req.getMaxPrice(), pageable);

        List<GoodsResp> list = page.getContent().stream().map(this::toResp).toList();
        return PageResp.of(list, page.getTotalElements(), req.getPage(), req.getSize());
    }

    // ── 我发布的商品 ──────────────────────────────────────────
    public PageResp<GoodsResp> myGoods(Long userId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Goods> p = (status == null)
                ? goodsRepo.findBySellerIdOrderByCreatedAtDesc(userId, pageable)
                : goodsRepo.findBySellerIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        return PageResp.of(p.getContent().stream().map(this::toResp).toList(),
                p.getTotalElements(), page, size);
    }

    // ── 浏览历史 ──────────────────────────────────────────────
    public PageResp<GoodsResp> browseHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BrowseHistory> p = browseRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<GoodsResp> list = p.getContent().stream()
                .map(bh -> goodsRepo.findById(bh.getGoodsId()).map(this::toResp).orElse(null))
                .filter(g -> g != null)
                .toList();
        return PageResp.of(list, p.getTotalElements(), page, size);
    }

    // ── 管理员搜索（不过滤 status）────────────────────────────
    public PageResp<GoodsResp> adminSearch(GoodsSearchReq req) {
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(),
                Sort.by("createdAt").descending());
        Page<Goods> page = goodsRepo.findAll(pageable);
        return PageResp.of(page.getContent().stream().map(this::toResp).toList(),
                page.getTotalElements(), req.getPage(), req.getSize());
    }

    // ── 管理员强制下架 ────────────────────────────────────────
    @Transactional
    public void adminTakeDown(Long goodsId) {
        Goods g = goodsRepo.findById(goodsId)
                .orElseThrow(() -> BusinessException.notFound("商品不存在"));
        g.setStatus("DRAFT");
        goodsRepo.save(g);
    }

    // ── 异步浏览记录 ──────────────────────────────────────────
    @Async
    public void incrementViewsAsync(Long goodsId, Long userId) {
        goodsRepo.incrementViews(goodsId);
        if (userId != null) {
            browseRepo.deleteByUserIdAndGoodsId(userId, goodsId); // 去重
            BrowseHistory bh = new BrowseHistory();
            bh.setUserId(userId);
            bh.setGoodsId(goodsId);
            browseRepo.save(bh);
        }
    }

    // ── 私有辅助 ─────────────────────────────────────────────
    private Goods getOwnGoods(Long userId, Long goodsId) {
        Goods g = goodsRepo.findById(goodsId)
                .orElseThrow(() -> BusinessException.notFound("商品不存在"));
        if (!g.getSellerId().equals(userId))
            throw BusinessException.forbidden("无权操作此商品");
        return g;
    }

    private void validateCategory(Long categoryId) {
        if (!categoryRepo.existsById(categoryId))
            throw BusinessException.badRequest("分类不存在");
    }

    private void saveImages(Long goodsId, List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        for (int i = 0; i < urls.size(); i++) {
            GoodsImage img = new GoodsImage();
            img.setGoodsId(goodsId);
            img.setImageUrl(urls.get(i));
            img.setSortOrder(i);
            imageRepo.save(img);
        }
    }

    GoodsResp toResp(Goods g) {
        GoodsResp r = new GoodsResp();
        BeanUtils.copyProperties(g, r);

        // 图片
        List<GoodsImage> imgs = imageRepo.findByGoodsIdOrderBySortOrderAsc(g.getId());
        List<String> urls = imgs.stream().map(GoodsImage::getImageUrl).collect(Collectors.toList());
        r.setImageUrls(urls);
        r.setCoverUrl(urls.isEmpty() ? null : urls.get(0));

        // 卖家信息
        userRepo.findById(g.getSellerId()).ifPresent(u -> {
            r.setSellerName(u.getNickname() != null ? u.getNickname() : u.getUsername());
            r.setSellerAvatar(u.getAvatarUrl());
        });

        // 分类名
        categoryRepo.findById(g.getCategoryId()).ifPresent(c -> r.setCategoryName(c.getName()));
        return r;
    }
}

// Note: adminSearch is the same as search but without status filter - added separately via method below
