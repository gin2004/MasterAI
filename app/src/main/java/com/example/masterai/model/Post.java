package com.example.masterai.model;

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
    @SerializedName("comments")
    private List<Comment> comments;


    public Post() {
    }

    public Post(String userId, String content, String visibility) {
        this.userId = userId;
        this.content = content;
        this.visibility = visibility;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<Media> getMedia() { return media; }
    public void setMedia(List<Media> media) { this.media = media; }
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
}
