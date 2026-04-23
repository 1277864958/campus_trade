package com.campus.controller;
import com.campus.common.result.Result;
import com.campus.dto.req.ChatSessionReq;
import com.campus.dto.req.SendMsgPayload;
import com.campus.dto.resp.*;
import com.campus.entity.ChatSession;
import com.campus.service.impl.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messaging;

    @PostMapping("/sessions")
    public Result<ChatSessionResp> getOrCreate(@AuthenticationPrincipal Long userId,
                                               @Valid @RequestBody ChatSessionReq req) {
        return Result.success(chatService.getOrCreate(userId, req));
    }
    @GetMapping("/sessions")
    public Result<List<ChatSessionResp>> sessions(@AuthenticationPrincipal Long userId) {
        return Result.success(chatService.listSessions(userId));
    }
    @GetMapping("/sessions/{id}/messages")
    public Result<PageResp<MessageResp>> messages(@PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(chatService.history(id, userId, page, size));
    }
    @GetMapping("/unread")
    public Result<?> unread(@AuthenticationPrincipal Long userId) {
        return Result.success(chatService.unreadSummary(userId));
    }
    @PostMapping("/send")
    public Result<MessageResp> sendViaHttp(@AuthenticationPrincipal Long userId,
                                           @RequestBody SendMsgPayload payload) {
        MessageResp saved = chatService.saveMessage(userId, payload);
        messaging.convertAndSend("/topic/chat/" + payload.getChatId(), saved);
        ChatSession session = chatService.getSession(payload.getChatId());
        Long receiverId = chatService.getReceiverId(session, userId);
        long unread = chatService.getUnread(payload.getChatId(), receiverId);
        messaging.convertAndSendToUser(
                receiverId.toString(), "/queue/notify",
                Map.of("sessionId", payload.getChatId(), "unreadCount", unread));
        return Result.success(saved);
    }

    // 【新增】：删除聊天记录（支持多选）
    @DeleteMapping("/sessions/{id}/messages")
    public Result<Void> deleteMessages(@PathVariable Long id,
                                       @AuthenticationPrincipal Long userId,
                                       @RequestBody List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return Result.success(null);
        }
        chatService.deleteMessages(userId, id, messageIds);
        return Result.success(null);
    }
}
