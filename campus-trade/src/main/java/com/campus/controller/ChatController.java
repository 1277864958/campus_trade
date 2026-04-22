package com.campus.controller;
import com.campus.common.result.Result;
import com.campus.dto.req.ChatSessionReq;
import com.campus.dto.resp.*;
import com.campus.service.impl.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

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
}
