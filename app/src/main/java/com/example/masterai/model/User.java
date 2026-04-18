package com.example.masterai.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class User implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("username")
    private String username;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("password")
    private String password;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("is_followed")
    private boolean isFollowed; // Trạng thái đã follow hay chưa

    @SerializedName("bio")
    private String bio;

    @SerializedName("post_count")
    private int postCount;

    @SerializedName("follower_count")
    private int followerCount;

    @SerializedName("following_count")
    private int followingCount;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
    public boolean isFollowed() { return isFollowed; }
    public void setFollowed(boolean followed) { isFollowed = followed; }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", isFollowed=" + isFollowed +
                ", bio='" + bio + '\'' +
                ", postCount=" + postCount +
                ", followerCount=" + followerCount +
                ", followingCount=" + followingCount +
                '}';
    }
}
