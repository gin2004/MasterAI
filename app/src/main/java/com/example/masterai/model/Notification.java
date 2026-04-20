package com.example.masterai.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Notification implements Serializable {
    private String id;
    
    @SerializedName("recipient_id")
    private String recipientId;
    
    private String sender_name;
    private String sender_avatar;
    private String content; // Keep for backward compatibility or direct use
    
    private String title;
    private String message;
    
    @SerializedName("is_read")
    private boolean isRead;
    
    @SerializedName("created_at")
    private String createdAt;
    
    private String type; // post, like, comment, generation, etc.

    // No-argument constructor
    public Notification() {
    }

    // Constructor for creating new notification
    public Notification(String recipientId, String content, String type) {
        this.recipientId = recipientId;
        this.content = content;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getSenderName() { return sender_name; }
    public void setSenderName(String sender_name) { this.sender_name = sender_name; }

    public String getSenderAvatar() { return sender_avatar; }
    public void setSenderAvatar(String sender_avatar) { this.sender_avatar = sender_avatar; }

    public String getContent() { 
        if (content != null && !content.isEmpty()) {
            return content;
        }
        if (title != null && !title.isEmpty() && message != null && !message.isEmpty()) {
            return title + ": " + message;
        } else if (message != null && !message.isEmpty()) {
            return message;
        } else if (title != null && !title.isEmpty()) {
            return title;
        }
        return "";
    }
    public void setContent(String content) { this.content = content; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
