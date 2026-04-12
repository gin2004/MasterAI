package com.example.masterai;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api/users")
    Call<List<User>> getUsers();

    // Sử dụng @Body để gửi JSON object gồm username và password
    @POST("api/users/login/")
    Call<User> login(@Body User user);

    @POST("api/users/register/")
    Call<User> register(@Body User user);
}
