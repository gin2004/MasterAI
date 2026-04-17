package com.example.masterai;
import android.app.Application;

import com.example.masterai.utils.AIUtils;

public class AIApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo ViewUtils ngay khi App chạy
        AIUtils.init(this);
    }
}
