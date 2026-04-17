package com.example.masterai.model;

import java.util.List;

public class AssetResponse {
    private boolean success;
    private List<Asset> data;
    private int total_items; // Khớp với JSON: "total_items": 1


    public boolean isSuccess() { return success; }
    public List<Asset> getData() { return data; }
    public int getTotalItems() { return total_items; }
}
