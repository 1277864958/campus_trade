package com.campus.dto.req;

public class SendMsgPayload {
    private Long   chatId;
    private String content;
    private String contentType = "TEXT"; // TEXT / IMAGE

    // Getters and Setters
    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
}
