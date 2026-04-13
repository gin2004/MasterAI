package com.example.masterai.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
}
