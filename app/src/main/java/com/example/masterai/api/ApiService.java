package com.example.masterai.api;

import com.example.masterai.model.ImageResponse;
import com.example.masterai.model.LoginResponse;
import com.example.masterai.model.Post;
import com.example.masterai.model.PromptResponse;
import com.example.masterai.model.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @GET("api/users/")
    Call<List<User>> getUsers();

    @POST("api/users/login/")
    Call<LoginResponse> login(@Body User user);

    @POST("api/users/register/")
    Call<User> register(@Body User user);

    // API cho Post
    @GET("api/posts/feed")
    Call<List<Post>> getPosts();

    @POST("api/posts/")
    Call<Post> createPost(@Body Post post);

    // API cho Ai Service
    // API Nâng cấp prompt
    @FormUrlEncoded
    @POST("api/ai/enhance-prompt/") // Thay bằng endpoint chính xác của bạn
    Call<PromptResponse> enhancePrompt(@Field("prompt") String prompt);

    // API Tạo ảnh (Hỗ trợ cả có ảnh và không có ảnh)
    @Multipart
    @POST("api/ai/generate-image/")
    Call<ImageResponse> generateImage(
            @Part("prompt") RequestBody prompt,
            @Part("aspect_ratio") RequestBody ratio,
            @Part("resolution") RequestBody res,
            @Part MultipartBody.Part image // Có thể null nếu không gửi ảnh
    );

}
