package com.example.masterai.model;

public class Asset {
    private String name;
    private String status;
    private int imageRes;

    public Asset(String name, String status, int imageRes) {
        this.name = name;
        this.status = status;
        this.imageRes = imageRes;
    }

    public String getName() { return name; }
    public String getStatus() { return status; }
    public int getImageRes() { return imageRes; }
}