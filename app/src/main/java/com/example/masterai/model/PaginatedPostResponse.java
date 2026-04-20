package com.example.masterai.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaginatedPostResponse<T> {
    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<T> results;

    public PaginatedPostResponse() {
    }

    // Getters and Setters
    public int getCount() { return count; }
    public String getNext() { return next; }
    public String getPrevious() { return previous; }
    public List<T> getResults() { return results; }
}
