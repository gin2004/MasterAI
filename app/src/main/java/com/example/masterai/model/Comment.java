package com.example.masterai.model;

public class Comment {
    private String userName;
    private String content;
    private String time;
    private String avatarUrl;

    public Comment(String userName, String content, String time, String avatarUrl) {
        this.userName = userName;
        this.content = content;
        this.time = time;
        this.avatarUrl = avatarUrl;
    }

    public String getUserName() { return userName; }
    public String getContent() { return content; }
    public String getTime() { return time; }
    public String getAvatarUrl() { return avatarUrl; }
}