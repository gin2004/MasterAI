package com.example.masterai.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Media implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("url")
    private String url;

    @SerializedName("media_type")
    private String mediaType; // 'image', 'avatar', 'voice'

    @SerializedName("source")
    private String source; // 'upload', 'ai'

    @SerializedName("order")
    private int order;

    public Media(String url, String mediaType, String source, int order) {
        this.url = url;
        this.mediaType = mediaType;
        this.source = source;
        this.order = order;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getUrl() { return url; }
    public String getMediaType() { return mediaType; }
    public String getSource() { return source; }
    public int getOrder() { return order; }
}
