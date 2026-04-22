package com.campus.dto.resp;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class MessageResp {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private String contentType;
    private Integer isRead;
    private LocalDateTime createdAt;
}
