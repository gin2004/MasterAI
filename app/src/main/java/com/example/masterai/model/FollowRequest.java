package com.example.masterai.model;

import com.google.gson.annotations.SerializedName;

public class FollowRequest {
    @SerializedName("follower_id")
    private String followerId;

    public FollowRequest(String followerId) {
        this.followerId = followerId;
    }

    public String getFollowerId() { return followerId; }
    public void setFollowerId(String followerId) { this.followerId = followerId; }
}