package com.example.masterai.api;

import com.example.masterai.model.Asset;
import com.example.masterai.model.AssetResponse;
import com.example.masterai.model.Generation;
import com.example.masterai.model.GenerationResponse;
import com.example.masterai.model.ImageResponse;
import com.example.masterai.model.Comment;
import com.example.masterai.model.LoginResponse;
import com.example.masterai.model.Notification;
import com.example.masterai.model.Post;
import com.example.masterai.model.PromptResponse;
import com.example.masterai.model.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/users/")
    Call<List<User>> getUsers();

    @POST("api/users/login/")
    Call<LoginResponse> login(@Body User user);

    @POST("api/users/register/")
    Call<User> register(@Body User user);

    @GET("api/users/{id}/")
    Call<User> getUserById(@Path("id") String id,@Query("current_user_id") String currentUserId);

    // API cho Post
    @GET("api/posts/feed/")
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
            @Part("user_id") RequestBody userId,
            @Part("prompt") RequestBody prompt,
            @Part("aspect_ratio") RequestBody ratio,
            @Part("resolution") RequestBody res,
            @Part MultipartBody.Part image
    );
    // 2. Lấy lịch sử sinh
    @GET("api/ai/generations/")
    Call<GenerationResponse> getGenerations(
            @Query("user_id") String userId,
            @Query("type") String type,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // 3. Lấy Assets
    @GET("api/ai/assets/")
    Call<AssetResponse> getAssets(
            @Query("user_id") String userId,
            @Query("type") String type
    );

    // 4. Thêm vào Asset (Tài nguyên)
    @FormUrlEncoded
    @POST("api/ai/add-asset/")
    Call<ResponseBody> addAsset(
            @Field("user_id") String userId,
            @Field("generation_id") String generationId
    );

    @POST("api/posts/{id}/like/")
    Call<Map<String, String>> toggleLike(@Path("id") String postId, @Body Map<String, String> body);

    @GET("api/posts/{id}/comments/")
    Call<List<Comment>> getComments(@Path("id") String postId);

    @POST("api/posts/{id}/comment/")
    Call<Comment> addComment(@Path("id") String postId, @Body Map<String, Object> body);

    @DELETE("api/posts/{id}/")
    Call<Map<String, String>> deletePost(@Path("id") String postId);

    @PATCH("api/posts/{id}/update/")
    Call<Post> updatePost(@Path("id") String postId, @Body Map<String, Object> body);

    // 🔔 NOTIFICATIONS
    @GET("api/notifications/")
    Call<List<Notification>> getNotifications(@Query("user_id") String userId);

    @POST("api/notifications/create/")
    Call<Notification> createNotification(@Body Notification notification);

    @PATCH("api/notifications/{id}/read/")
    Call<Notification> markAsRead(@Path("id") String notificationId);
}
