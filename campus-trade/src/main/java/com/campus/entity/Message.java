package com.campus.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@NoArgsConstructor
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "content_type", nullable = false, length = 10)
    private String contentType = "TEXT";

    // 【修改点】：将 Integer 改为 Boolean，默认值为 false
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 【新增】：发送方是否已删除该消息
    @Column(name = "deleted_by_sender", nullable = false)
    private Boolean deletedBySender = false;

    // 【新增】：接收方是否已删除该消息
    @Column(name = "deleted_by_receiver", nullable = false)
    private Boolean deletedByReceiver = false;


    // 【新增的 Getter/Setter】
    public Boolean getDeletedBySender() { return deletedBySender; }
    public void setDeletedBySender(Boolean deletedBySender) { this.deletedBySender = deletedBySender; }

    public Boolean getDeletedByReceiver() { return deletedByReceiver; }
    public void setDeletedByReceiver(Boolean deletedByReceiver) { this.deletedByReceiver = deletedByReceiver; }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    // 【修改点】：同步修改 Getter 和 Setter 参数类型
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}