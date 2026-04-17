package com.example.masterai.model;

public class Generation {
    private String id;
    private String type;
    private String prompt;
    private String media_url; // Khớp với JSON
    private String aspect_ratio;
    private String resolution;
    private String created_at;

    // Getters
    public String getId() { return id; }
    public String getPrompt() { return prompt; }
    public String getMediaUrl() { return media_url; }
    public String getCreatedAt() { return created_at; }
}