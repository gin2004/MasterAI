package com.example.masterai.model;

import com.google.gson.annotations.SerializedName;

public class AudioResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("generation_id")
    private String generationId;

    @SerializedName("media_url")
    private String mediaUrl;

    @SerializedName("type")
    private String type;

    public AudioResponse() {
    }

    // Tạo Getters và Setters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getGenerationId() { return generationId; }
    public String getMediaUrl() { return mediaUrl; }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setGenerationId(String generationId) {
        this.generationId = generationId;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}