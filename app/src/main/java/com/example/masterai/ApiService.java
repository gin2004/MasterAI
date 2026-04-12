package com.example.masterai;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api/users/")
    Call<List<User>> getUsers();

    @POST("api/login/")
    Call<User> login(@Body User user);

    @POST("api/register/")
    Call<User> register(@Body User user);
}
