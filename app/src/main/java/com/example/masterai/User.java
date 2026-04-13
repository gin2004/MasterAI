package com.example.masterai;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("username")
    private String username;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("password")
    private String password;

    @SerializedName("profile_image") // Tên trường khớp với API trả về
    private String profileImage;

    public User(String username, String password, String s) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}
