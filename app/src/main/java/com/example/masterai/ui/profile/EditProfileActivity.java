package com.example.masterai.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.masterai.api.ApiService;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.databinding.ActivityEditProfileBinding;
import com.example.masterai.model.User;
import com.example.masterai.utils.UserManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private User currentUser;
    private ApiService apiService;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Glide.with(this)
                            .load(selectedImageUri)
                            .circleCrop()
                            .into(binding.ivEditAvatar);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = UserManager.getInstance(this).getUser();
        apiService = RetrofitClient.getApiService();

        initView();
        setupEvents();
    }

    private void initView() {
        if (currentUser != null) {
            binding.etName.setText(currentUser.getUsername());
            binding.etEmail.setText(currentUser.getEmail());
            binding.etBio.setText(currentUser.getBio());

            if (currentUser.getAvatarUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getAvatarUrl())
                        .placeholder(com.example.masterai.R.drawable.ic_user)
                        .circleCrop()
                        .into(binding.ivEditAvatar);
            }
        }
    }

    private void setupEvents() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.tvSave.setOnClickListener(v -> {
            saveProfile();
        });

        binding.tvChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String bio = binding.etBio.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Cập nhật Avatar nếu có chọn ảnh mới
        if (selectedImageUri != null) {
            uploadAvatar(selectedImageUri);
        }

        // 2. Cập nhật thông tin text
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", name);
        updates.put("email", email);
        updates.put("bio", bio);

        apiService.updateProfile(currentUser.getId(), updates).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserManager.getInstance(EditProfileActivity.this).setUser(response.body());
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadAvatar(Uri uri) {
        try {
            File file = getFileFromUri(uri);
            if (file == null) return;

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);

            apiService.updateAvatar(currentUser.getId(), body).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("EditProfile", "Avatar updated successfully");
                        UserManager.getInstance(EditProfileActivity.this).setUser(response.body());
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e("EditProfile", "Avatar update failed: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = new File(getCacheDir(), "temp_avatar.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }
}
