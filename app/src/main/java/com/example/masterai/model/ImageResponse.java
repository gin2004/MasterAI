package com.example.masterai.model;

public class ImageResponse {
    public String generation_id;
    public boolean success;
    public String media_url;
    public String message;
    public String error; // Dùng để bắt lỗi từ server
    public String aspect_ratio;
    public String resolution_config;

    public ImageResponse(String generation_id, boolean success, String message, String media_url, String aspect_ratio, String resolution_config) {
        this.generation_id = generation_id;
        this.success = success;
        this.message = message;
        this.media_url = media_url;
        this.aspect_ratio = aspect_ratio;
        this.resolution_config = resolution_config;
    }

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