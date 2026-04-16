package com.example.masterai.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Thay 192.168.1.50 bằng IP thực tế của máy chạy Django (Máy B)
    // Đảm bảo Máy A và Máy B dùng chung một mạng Wi-Fi/LAN
    private static final String BASE_URL = "http://192.168.1.21:8000/";
    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
