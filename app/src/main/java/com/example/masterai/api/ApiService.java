package com.example.masterai.api;

import com.example.masterai.model.LoginResponse;
import com.example.masterai.model.Post;
import com.example.masterai.model.User;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api/users/")
    Call<List<User>> getUsers();

    @POST("api/users/login/")
    Call<LoginResponse> login(@Body User user);

    @POST("api/users/register/")
    Call<User> register(@Body User user);

    // API cho Post
    @GET("api/posts/")
    Call<List<Post>> getPosts();

    @POST("api/posts/")
    Call<Post> createPost(@Body Post post);
}
