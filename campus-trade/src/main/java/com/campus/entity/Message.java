package com.campus.entity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@NoArgsConstructor
@Entity @Table(name = "messages")
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "chat_id", nullable = false)
    private Long chatId;
    @Column(name = "sender_id", nullable = false)
    private Long senderId;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    @Column(name = "content_type", nullable = false, length = 10)
    private String contentType = "TEXT";
    @Column(name = "is_read", nullable = false)
    private Integer isRead = 0;
    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

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

    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
