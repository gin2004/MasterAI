package com.example.masterai;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Post implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("content")
    private String content;

    @SerializedName("visibility")
    private String visibility; // 'public', 'private', 'friends'

    @SerializedName("like_count")
    private int likeCount;

    @SerializedName("comment_count")
    private int commentCount;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("media")
    private List<Media> media;

    public Post(String userId, String content, String visibility) {
        this.userId = userId;
        this.content = content;
        this.visibility = visibility;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getContent() { return content; }
    public String getVisibility() { return visibility; }
    public int getLikeCount() { return likeCount; }
    public int getCommentCount() { return commentCount; }
    public String getCreatedAt() { return createdAt; }
    public List<Media> getMedia() { return media; }

    public void setMedia(List<Media> media) { this.media = media; }
}
