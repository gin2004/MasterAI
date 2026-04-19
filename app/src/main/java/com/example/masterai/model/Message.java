package com.example.masterai.model;

public class Message {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;

    private String id;
    private String senderId;
    private String text;
    private String imageUrl; // null nếu là tin nhắn chữ
    private long timestamp;
    private int type; // TYPE_TEXT hoặc TYPE_IMAGE
    private boolean isSentByMe;

    public Message(String senderId, String text, String imageUrl, int type, boolean isSentByMe) {
        this.senderId = senderId;
        this.text = text;
        this.imageUrl = imageUrl;
        this.type = type;
        this.isSentByMe = isSentByMe;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters và Setters...

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isSentByMe() {
        return isSentByMe;
    }

    public void setSentByMe(boolean sentByMe) {
        isSentByMe = sentByMe;
    }

    public Message() {
    }

}