package com.example.masterai.model;

public class Message {
    private String username;
    private String lastMessage;
    private String time;
    private String avatarUrl;
    private boolean isOnline;

    public Message(String username, String lastMessage, String time, String avatarUrl, boolean isOnline) {
        this.username = username;
        this.lastMessage = lastMessage;
        this.time = time;
        this.avatarUrl = avatarUrl;
        this.isOnline = isOnline;
    }

    public String getUsername() { return username; }
    public String getLastMessage() { return lastMessage; }
    public String getTime() { return time; }
    public String getAvatarUrl() { return avatarUrl; }
    public boolean isOnline() { return isOnline; }
}