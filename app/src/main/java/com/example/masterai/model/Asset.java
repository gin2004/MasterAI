package com.example.masterai.model;

import com.google.gson.annotations.SerializedName;

public class Asset {
    @SerializedName("id")
    private String id;

    @SerializedName("type")
    private String type;

    @SerializedName("media_url")
    private String mediaUrl; // Dùng camelCase cho Java

    @SerializedName("prompt")
    private String prompt;

    @SerializedName("created_at")
    private String createdAt;

    // Constructor cho việc test dữ liệu ảo (nếu cần)
    public Asset(String id, String type, String mediaUrl, String prompt) {
        this.id = id;
        this.type = type;
        this.mediaUrl = mediaUrl;
        this.prompt = prompt;
    }

    public Asset(String id, String type, String mediaUrl, String prompt, String createdAt) {
        this.id = id;
        this.type = type;
        this.mediaUrl = mediaUrl;
        this.prompt = prompt;
        this.createdAt = createdAt;
    }


    // Getters
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // Setters (Nếu cần cập nhật dữ liệu trong code)
    public void setId(String id) {
        this.id = id;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }
}