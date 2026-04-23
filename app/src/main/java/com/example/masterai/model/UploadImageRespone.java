package com.example.masterai.model;

public class UploadImageRespone {
    public String image_url;
    public String message;

    public UploadImageRespone(String image_url, String message) {
        this.image_url = image_url;
        this.message = message;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
