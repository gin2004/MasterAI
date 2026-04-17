package com.example.masterai.model;

public class Generation {
    private String title;
    private int imageRes;

    public Generation(String title, int imageRes) {
        this.title = title;
        this.imageRes = imageRes;
    }

    public String getTitle() { return title; }
    public int getImageRes() { return imageRes; }
}