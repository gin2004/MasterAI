package com.example.masterai.model;

import com.google.gson.annotations.SerializedName;

public class StatusRequest {
    @SerializedName("user_id")
    private String userId;

    @SerializedName("is_online")
    private boolean isOnline;

    public StatusRequest(String userId, boolean isOnline) {
        this.userId = userId;
        this.isOnline = isOnline;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { this.isOnline = online; }
}