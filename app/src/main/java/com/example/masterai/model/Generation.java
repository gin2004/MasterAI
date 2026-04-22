package com.example.masterai.model;

import com.google.gson.annotations.SerializedName;

public class Generation {
    private String id;
    private String type;
    private String prompt;

    @SerializedName("media_url")
    private String media_url; // Khớp với JSON

    @SerializedName("aspect_ratio")
    private String aspect_ratio;

    private String resolution;

    @SerializedName("created_at")
    private String created_at;

    public Generation() {
    }

    public Generation(String id, String type, String prompt, String media_url, String created_at) {
        this.id = id;
        this.type = type;
        this.prompt = prompt;
        this.media_url = media_url;
        this.created_at = created_at;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getMediaUrl() { return media_url; }
    public void setMediaUrl(String media_url) { this.media_url = media_url; }

    public String getAspectRatio() { return aspect_ratio; }
    public void setAspectRatio(String aspect_ratio) { this.aspect_ratio = aspect_ratio; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public String getCreatedAt() { return created_at; }
    public void setCreatedAt(String created_at) { this.created_at = created_at; }
}