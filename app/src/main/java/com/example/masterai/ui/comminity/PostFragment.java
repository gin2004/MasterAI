package com.example.masterai.ui.comminity;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.masterai.MainActivity;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.model.Media;
import com.example.masterai.model.Post;
import com.example.masterai.model.User;
import com.example.masterai.utils.UserManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostFragment extends Fragment {

    private EditText etPostContent;
    private MaterialButton btnSubmitPost;
    private ImageButton btnUploadImage;
    private RecyclerView rvImages;
    private ShapeableImageView imgAvatar;
    private TextView tvUsername;
    private ImagePreviewAdapter imagePreviewAdapter;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private User currentUser;

    // Sử dụng Photo Picker (Android 13+) để tránh lỗi SecurityException
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), uris -> {
                if (!uris.isEmpty()) {
                    selectedImageUris.addAll(uris);
                    updateImagesVisibility();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        etPostContent = view.findViewById(R.id.etPostContent);
        btnSubmitPost = view.findViewById(R.id.btnSubmitPost);
        btnUploadImage = view.findViewById(R.id.btnUploadImage);
        rvImages = view.findViewById(R.id.rvImages);
        imgAvatar = view.findViewById(R.id.ivAvatar);
        tvUsername = view.findViewById(R.id.tvUsername);

        // Thiết lập RecyclerView cho ảnh preview
        rvImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imagePreviewAdapter = new ImagePreviewAdapter(selectedImageUris);
        rvImages.setAdapter(imagePreviewAdapter);

        btnUploadImage.setOnClickListener(v -> openGallery());
        btnSubmitPost.setOnClickListener(v -> createPost());

        currentUser = UserManager.getInstance(requireContext()).getUser();
        if (currentUser != null) {
            tvUsername.setText("@"+currentUser.getUsername());
            Glide.with(this).load(currentUser.getAvatarUrl()).into(imgAvatar);
        }

        return view;
    }

    private void openGallery() {
        // Mở Photo Picker để chọn nhiều ảnh/video
        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void updateImagesVisibility() {
        if (selectedImageUris.isEmpty()) {
            rvImages.setVisibility(View.GONE);
        } else {
            rvImages.setVisibility(View.VISIBLE);
            imagePreviewAdapter.notifyDataSetChanged();
        }
    }

    private void createPost() {
        String content = etPostContent.getText().toString().trim();

        if (TextUtils.isEmpty(content) && selectedImageUris.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập nội dung hoặc chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để đăng bài", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- MỚI: Vô hiệu hóa nút để tránh gửi trùng ---
        btnSubmitPost.setEnabled(false);
        Log.d("UPLOAD_DEBUG", "Bắt đầu chuẩn bị dữ liệu...");

        // 1. Chuẩn bị các dữ liệu text
        RequestBody rbUserId = createPartFromString(currentUser.getId());
        RequestBody rbContent = createPartFromString(content);
        RequestBody rbVisibility = createPartFromString("public");

        // 2. Chuẩn bị danh sách files
        List<MultipartBody.Part> fileParts = new ArrayList<>();
        for (Uri uri : selectedImageUris) {
            MultipartBody.Part part = prepareFilePart("files", uri);
            if (part != null) {
                fileParts.add(part);
            }
        }

        Log.d("UPLOAD_DEBUG", "Số lượng file chuẩn bị xong: " + fileParts.size());

        // 3. Gọi API
        RetrofitClient.getApiService().createPost(rbUserId, rbContent, rbVisibility, fileParts)
                .enqueue(new Callback<Post>() {
                    @Override
                    public void onResponse(Call<Post> call, Response<Post> response) {
                        btnSubmitPost.setEnabled(true); // Kích hoạt lại nút
                        if (response.isSuccessful()) {
                            Log.d("UPLOAD_DEBUG", "Thành công!");
                            Toast.makeText(getContext(), "Đăng bài thành công!", Toast.LENGTH_SHORT).show();

                            etPostContent.setText("");
                            selectedImageUris.clear();
                            updateImagesVisibility();

                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).navigateToCommunity();
                            }
                        } else {
                            // MỚI: Log chi tiết lỗi từ server
                            try {
                                String errorBody = response.errorBody().string() != null ? response.errorBody().string() : "No error body";
                                Log.e("UPLOAD_DEBUG", "Lỗi server (" + response.code() + "): " + errorBody);
                            } catch (IOException e) { e.printStackTrace(); }

                            Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Post> call, Throwable t) {
                        btnSubmitPost.setEnabled(true);
                        Log.e("UPLOAD_DEBUG", "Kết nối thất bại: " + t.getMessage());
                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        try {
            ContentResolver contentResolver = requireContext().getContentResolver();
            // Lấy tên file thực tế
            String fileName = "upload_" + System.currentTimeMillis() + ".jpg";

            InputStream inputStream = contentResolver.openInputStream(fileUri);
            if (inputStream == null) return null;

            // Đọc bytes
            byte[] bytes = getBytes(inputStream);

            RequestBody requestFile = RequestBody.create(
                    MediaType.parse(contentResolver.getType(fileUri)),
                    bytes
            );

            return MultipartBody.Part.createFormData(partName, fileName, requestFile);
        } catch (Exception e) {
            Log.e("UPLOAD_ERR", "Error preparing file: " + e.getMessage());
            return null;
        }
    }

    // Hàm hỗ trợ đọc bytes không cần thư viện ngoài
    public byte[] getBytes(InputStream inputStream) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Nén chất lượng xuống 70-80% là đủ đẹp để hiển thị trên app
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        return outputStream.toByteArray();
    }
    private RequestBody createPartFromString(String descriptionString) {
        if (descriptionString == null) descriptionString = "";
        return RequestBody.create(MultipartBody.FORM, descriptionString);
    }
}
