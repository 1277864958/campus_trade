package com.campus.service.impl;

import com.campus.common.exception.BusinessException;
import com.campus.dto.req.ChatSessionReq;
import com.campus.dto.req.SendMsgPayload;
import com.campus.dto.resp.*;
import com.campus.entity.*;
import com.campus.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 聊天服务：会话创建、消息持久化、未读数管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository sessionRepo;
    private final MessageRepository     messageRepo;
    private final UserRepository        userRepo;
    private final GoodsRepository       goodsRepo;
    private final StringRedisTemplate   redis;

    private static final String UNREAD_KEY = "chat:unread:%d:%d"; // chatId:userId
    private static final long   UNREAD_TTL = 30L; // 天

    // ── 获取或创建会话（幂等）────────────────────────────────
    @Transactional
    public ChatSessionResp getOrCreate(Long buyerId, ChatSessionReq req) {
        if (buyerId.equals(req.getSellerId()))
            throw BusinessException.of("不能与自己建立会话");

        ChatSession session = sessionRepo
                .findByBuyerIdAndSellerIdAndGoodsId(buyerId, req.getSellerId(), req.getGoodsId())
                .orElseGet(() -> {
                    ChatSession s = new ChatSession();
                    s.setBuyerId(buyerId);
                    s.setSellerId(req.getSellerId());
                    s.setGoodsId(req.getGoodsId());
                    return sessionRepo.save(s);
                });
        return toSessionResp(session, buyerId);
    }

    // ── 会话列表 ─────────────────────────────────────────────
    public List<ChatSessionResp> listSessions(Long userId) {
        return sessionRepo.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId)
                .stream().map(s -> toSessionResp(s, userId)).toList();
    }

    // ── 持久化消息并返回 DTO（供 WebSocket 广播使用）─────────
    @Transactional
    public MessageResp saveMessage(Long senderId, SendMsgPayload payload) {
        ChatSession session = sessionRepo.findById(payload.getChatId())
                .orElseThrow(() -> BusinessException.notFound("会话不存在"));

        Long receiverId = getReceiverId(session, senderId);

        Message msg = new Message();
        msg.setChatId(payload.getChatId());
        msg.setSenderId(senderId);
        msg.setContent(payload.getContent());
        msg.setContentType(payload.getContentType() != null ? payload.getContentType() : "TEXT");
        messageRepo.save(msg);

        // 接收方未读数 +1
        incrementUnread(payload.getChatId(), receiverId);

        return toMsgResp(msg);
    }

    // ── 历史消息（分页，同时标记已读）───────────────────────
    @Transactional
    public PageResp<MessageResp> history(Long chatId, Long userId, int page, int size) {
        ChatSession session = sessionRepo.findById(chatId)
                .orElseThrow(() -> BusinessException.notFound("会话不存在"));
        if (!userId.equals(session.getBuyerId()) && !userId.equals(session.getSellerId()))
            throw BusinessException.forbidden("无权访问此会话");

        messageRepo.markAllRead(chatId, userId);
        resetUnread(chatId, userId);

        Page<Message> p = messageRepo.findByChatIdOrderByCreatedAtAsc(
                chatId, PageRequest.of(page, size));
        return PageResp.of(p.getContent().stream().map(this::toMsgResp).toList(),
                p.getTotalElements(), page, size);
    }

    // ── 各会话未读数汇总 ─────────────────────────────────────
    public List<java.util.Map<String, Object>> unreadSummary(Long userId) {
        return sessionRepo.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId)
                .stream().map(s -> java.util.Map.<String, Object>of(
                        "sessionId",  s.getId(),
                        "unreadCount", getUnread(s.getId(), userId)))
                .toList();
    }

    // ── Redis 未读数操作 ──────────────────────────────────────
    public void incrementUnread(Long chatId, Long userId) {
        String key = String.format(UNREAD_KEY, chatId, userId);
        redis.opsForValue().increment(key);
        redis.expire(key, UNREAD_TTL, TimeUnit.DAYS);
    }

    public void resetUnread(Long chatId, Long userId) {
        redis.delete(String.format(UNREAD_KEY, chatId, userId));
    }

    public long getUnread(Long chatId, Long userId) {
        String v = redis.opsForValue().get(String.format(UNREAD_KEY, chatId, userId));
        return v == null ? 0L : Long.parseLong(v);
    }

    // ── 获取接收方 ID ─────────────────────────────────────────
    public Long getReceiverId(ChatSession session, Long senderId) {
        return senderId.equals(session.getBuyerId()) ? session.getSellerId() : session.getBuyerId();
    }

    public ChatSession getSession(Long chatId) {
        return sessionRepo.findById(chatId)
                .orElseThrow(() -> BusinessException.notFound("会话不存在"));
    }

    // ── 私有 DTO 转换 ─────────────────────────────────────────
    private ChatSessionResp toSessionResp(ChatSession s, Long currentUserId) {
        ChatSessionResp r = new ChatSessionResp();
        r.setSessionId(s.getId());
        r.setBuyerId(s.getBuyerId());
        r.setSellerId(s.getSellerId());
        r.setGoodsId(s.getGoodsId());
        r.setCreatedAt(s.getCreatedAt());
        r.setUnreadCount(getUnread(s.getId(), currentUserId));

        // 对方信息
        Long otherId = currentUserId.equals(s.getBuyerId()) ? s.getSellerId() : s.getBuyerId();
        userRepo.findById(otherId).ifPresent(u -> {
            r.setOtherUserName(u.getNickname() != null ? u.getNickname() : u.getUsername());
            r.setOtherUserAvatar(u.getAvatarUrl());
        });
        // 商品信息
        goodsRepo.findById(s.getGoodsId()).ifPresent(g -> {
            r.setGoodsTitle(g.getTitle());
        });
        // 最后一条消息
        Page<Message> last = messageRepo.findByChatIdOrderByCreatedAtAsc(
                s.getId(), PageRequest.of(0, 1, Sort.by("createdAt").descending()));
        last.getContent().stream().findFirst().ifPresent(m -> r.setLastMessage(toMsgResp(m)));
        return r;
    }

    private MessageResp toMsgResp(Message m) {
        MessageResp r = new MessageResp();
        BeanUtils.copyProperties(m, r);
        userRepo.findById(m.getSenderId()).ifPresent(u -> {
            r.setSenderName(u.getNickname() != null ? u.getNickname() : u.getUsername());
            r.setSenderAvatar(u.getAvatarUrl());
        });
        return r;
    }
}
