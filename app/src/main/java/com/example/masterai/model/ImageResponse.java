package com.example.masterai.model;

public class ImageResponse {
    public boolean success;
    public String media_url;
    public String message;
    public String error; // Dùng để bắt lỗi từ server
    public String aspect_ratio;
    public String resolution_config;

    public ImageResponse(boolean success, String media_url, String message, String error) {
        this.success = success;
        this.media_url = media_url;
        this.message = message;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMedia_url() {
        return media_url;
    }

    public void setMedia_url(String media_url) {
        this.media_url = media_url;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}