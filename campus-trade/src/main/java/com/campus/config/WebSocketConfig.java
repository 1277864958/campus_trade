package com.campus.config;

import com.campus.common.utils.JwtUtils;
import com.campus.dto.req.SendMsgPayload;
import com.campus.dto.resp.MessageResp;
import com.campus.entity.ChatSession;
import com.campus.service.impl.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * WebSocket / STOMP 配置
 *
 * 连接端点: ws://host:8080/ws
 * 客户端订阅:
 *   /topic/chat/{sessionId}      — 会话广播（双方收消息）
 *   /user/queue/notify           — 个人未读角标推送
 * 客户端发送:
 *   /app/chat.send               — 发消息
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthInterceptor stompAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/user");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthInterceptor);
    }
}

/**
 * STOMP 入站拦截器：在 CONNECT 帧验证 JWT，
 * 将 userId 设置为 WebSocket Principal（供点对点推送使用）
 */
@Component
@RequiredArgsConstructor
class StompAuthInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(StompAuthInterceptor.class);

    private final JwtUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (jwtUtils.validateToken(token)) {
                        Long   userId = jwtUtils.getUserId(token);
                        String role   = jwtUtils.getRole(token);
                        var auth = new UsernamePasswordAuthenticationToken(
                                userId, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                        accessor.setUser(auth);
                        log.debug("WebSocket 认证成功 userId={}", userId);
                    }
                } catch (Exception e) {
                    log.warn("WebSocket CONNECT Token 解析失败: {}", e.getMessage());
                }
            }
        }
        return message;
    }
}

/**
 * WebSocket 消息处理：接收 → 持久化 → 广播
 */
@Slf4j
@Controller
@RequiredArgsConstructor
class ChatWsController {

    private final ChatService           chatService;
    private final SimpMessagingTemplate messaging;

    /**
     * 客户端发：STOMP SEND  /app/chat.send
     * 服务端推：/topic/chat/{sessionId}
     *          /user/{receiverId}/queue/notify
     */
    @MessageMapping("/chat.send")
    public void handleMessage(@Payload SendMsgPayload payload, Principal principal) {
        if (principal == null) {
            log.warn("未认证的 WebSocket 消息，已忽略");
            return;
        }

        Long senderId = ((UsernamePasswordAuthenticationToken) principal)
                .getPrincipal() instanceof Long id ? id
                : Long.parseLong(principal.getName());

        // 1. 持久化
        MessageResp saved = chatService.saveMessage(senderId, payload);

        // 2. 广播到会话频道（买家+卖家都订阅 /topic/chat/{sessionId}）
        messaging.convertAndSend("/topic/chat/" + payload.getChatId(), saved);

        // 3. 向接收方推送未读角标
        ChatSession session = chatService.getSession(payload.getChatId());
        Long receiverId = chatService.getReceiverId(session, senderId);
        long unread = chatService.getUnread(payload.getChatId(), receiverId);
        messaging.convertAndSendToUser(
                receiverId.toString(), "/queue/notify",
                java.util.Map.of("sessionId", payload.getChatId(), "unreadCount", unread));

        log.debug("消息广播完成 chatId={} sender={}", payload.getChatId(), senderId);
    }
}
