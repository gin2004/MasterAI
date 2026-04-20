package com.example.masterai.ui.comminity;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.util.ArrayList;
import java.util.List;
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

        User currentUser = UserManager.getInstance(requireContext()).getUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để đăng bài", Toast.LENGTH_SHORT).show();
            return;
        }

        Post post = new Post();
        post.setUserId(currentUser.getId());
        post.setContent(content);
        post.setVisibility("public");

        // Chuyển đổi danh sách Uri thành danh sách Media
        List<Media> mediaList = new ArrayList<>();
        for (Uri uri : selectedImageUris) {
            Media media = new Media();
            // Cấp quyền đọc bền vững cho URI nếu cần (thường dùng khi lưu vào DB)
            try {
                requireContext().getContentResolver().takePersistableUriPermission(uri, 
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException e) {
                // Photo picker thường không hỗ trợ persistable permissions cho mọi provider
                // nhưng nó tự cấp quyền tạm thời cho lifecycle của app
            }
            media.setUrl(uri.toString()); 
            media.setMediaType("image");
            media.setSource("upload");
            mediaList.add(media);
        }
        post.setMedia(mediaList);

        RetrofitClient.getApiService().createPost(post).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).navigateToCommunity();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
