package com.example.masterai.utils;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static androidx.core.content.ContextCompat.startActivity;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class AIUtils {

    // 1. Biến instance duy nhất
    private static AIUtils instance;

    // 2. Context của Application (Sống cùng vòng đời của App, không lo leak memory)
    private final Application application;
    private static Context context;

    // 3. Constructor private để ngăn chặn khởi tạo từ bên ngoài bằng từ khóa 'new'
    private AIUtils(Application application) {
        this.application = application;
    }

    // 4. Hàm khởi tạo (Chỉ gọi 1 lần duy nhất lúc mở App)
    public static void init(Application application) {
        if (instance == null) {
            instance = new AIUtils(application);
        }
    }

    // 5. Hàm lấy instance để sử dụng
    public static AIUtils getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ViewUtils chưa được khởi tạo");
        }
        return instance;
    }
    public void shareImageAndText(String imageUrl, String titlePrompt) {
        Toast.makeText(application, "Đang chuẩn bị ảnh để chia sẻ...", Toast.LENGTH_SHORT).show();

        // Dùng Glide để tải ảnh từ URL Cloudinary về dạng Bitmap
        Glide.with(application)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Tải xong thì chuyển qua hàm share
                        executeShareIntent(resource, titlePrompt);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private void executeShareIntent(Bitmap bitmap, String title) {
        try {
            // 1. Lưu Bitmap vào thư mục Cache
            File cachePath = new File(application.getCacheDir(), "images");
            cachePath.mkdirs(); // Tạo thư mục nếu chưa có
            File newFile = new File(cachePath, "avatar_share.png"); // Ghi đè file mỗi lần share cho nhẹ máy

            FileOutputStream stream = new FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // 2. Lấy URI an toàn từ FileProvider
            Uri contentUri = FileProvider.getUriForFile(
                    application,
                    application.getPackageName() + ".fileprovider",
                    newFile);

            // 3. Mở bảng chọn App (Facebook, Zalo...)
            if (contentUri != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Cấp quyền đọc file cho app nhận

                // Đính kèm Ảnh
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

                // Đính kèm Text (Tiêu đề / Prompt)
                String shareText = "Test app: Xem Avatar AI tôi vừa tạo nè!\nPrompt: " + title;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

                // Khởi chạy Intent
                Intent chooser = Intent.createChooser(shareIntent, "Chia sẻ Avatar qua...");
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// Phải có khi dùng Application Context

                application.startActivity(chooser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(application, "Lỗi khi chia sẻ ảnh!", Toast.LENGTH_SHORT).show();
        }
    }
    public String parseDate(String input){

        String result = input;
        DateTimeFormatter inputFormatter = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(input, inputFormatter);

            // 2. Định dạng lại theo ý muốn
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            result = dateTime.format(outputFormatter);
        }

        return result;
    }

}
