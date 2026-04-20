package com.example.masterai.model;

import com.google.gson.annotations.SerializedName;

public class FollowResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("is_following")
    private boolean isFollowing;

    public String getMessage() { return message; }
    public boolean isFollowing() { return isFollowing; }
}
