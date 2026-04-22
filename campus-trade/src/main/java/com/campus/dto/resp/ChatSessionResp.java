package com.campus.dto.resp;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class ChatSessionResp {
    private Long sessionId;
    private Long buyerId;
    private Long sellerId;
    private String otherUserName;
    private String otherUserAvatar;
    private Long goodsId;
    private String goodsTitle;
    private String goodsCover;
    private long unreadCount;
    private MessageResp lastMessage;
    private LocalDateTime createdAt;
}
