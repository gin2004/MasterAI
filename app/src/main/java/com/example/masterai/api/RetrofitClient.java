package com.example.masterai.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://172.11.218.57:8000/";
    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            // 1. Cấu hình lại OkHttpClient để nới lỏng thời gian chờ
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60*2, TimeUnit.SECONDS) // Thời gian tối đa để kết nối tới server
                    .readTimeout(60*3, TimeUnit.SECONDS)    // Thời gian tối đa để ĐỢI server sinh ảnh/xử lý
                    .writeTimeout(60, TimeUnit.SECONDS)   // Thời gian tối đa để upload file từ điện thoại lên server
                    .build();


            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
