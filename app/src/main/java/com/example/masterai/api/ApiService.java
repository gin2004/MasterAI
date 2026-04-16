package com.example.masterai.api;

import com.example.masterai.model.Comment;
import com.example.masterai.model.LoginResponse;
import com.example.masterai.model.Post;
import com.example.masterai.model.User;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/users/")
    Call<List<User>> getUsers();

    @POST("api/users/login/")
    Call<LoginResponse> login(@Body User user);

    @POST("api/users/register/")
    Call<User> register(@Body User user);

    @GET("api/users/{id}/")
    Call<User> getUserById(@Path("id") String id);

    // API cho Post
    @GET("api/posts/feed/")
    Call<List<Post>> getPosts();

    @POST("api/posts/")
    Call<Post> createPost(@Body Post post);

    @POST("api/posts/{id}/like/")
    Call<Map<String, String>> toggleLike(@Path("id") String postId, @Body Map<String, String> body);

    @GET("api/posts/{id}/comments/")
    Call<List<Comment>> getComments(@Path("id") String postId);

    @POST("api/posts/{id}/comment/")
    Call<Comment> addComment(@Path("id") String postId, @Body Map<String, Object> body);
}
