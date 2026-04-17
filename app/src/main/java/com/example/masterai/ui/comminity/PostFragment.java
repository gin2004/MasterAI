package com.example.masterai.ui.comminity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.model.Media;
import com.example.masterai.model.Post;
import com.example.masterai.model.User;
import com.example.masterai.utils.UserManager;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostFragment extends Fragment {

    private EditText etPostContent;
    private MaterialButton btnSubmitPost;
    private ImageButton btnUploadImage;
    private ImageView ivImagePreview;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivImagePreview.setVisibility(View.VISIBLE);
                    Glide.with(this).load(selectedImageUri).into(ivImagePreview);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        etPostContent = view.findViewById(R.id.etPostContent);
        btnSubmitPost = view.findViewById(R.id.btnSubmitPost);
        btnUploadImage = view.findViewById(R.id.btnUploadImage);
        ivImagePreview = view.findViewById(R.id.ivImagePreview);

        btnUploadImage.setOnClickListener(v -> openGallery());
        btnSubmitPost.setOnClickListener(v -> createPost());

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void createPost() {
        String content = etPostContent.getText().toString().trim();

        if (TextUtils.isEmpty(content) && selectedImageUri == null) {
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

        // Nếu có ảnh, thêm vào list media (Lưu ý: Ở đây chỉ gửi URI/Mock URL, 
        // thực tế cần upload file lên server trước hoặc dùng Multipart)
        if (selectedImageUri != null) {
            List<Media> mediaList = new ArrayList<>();
            Media media = new Media();
            media.setUrl(selectedImageUri.toString()); // Tạm thời gửi URI
            media.setMediaType("image");
            media.setSource("upload");
            mediaList.add(media);
            post.setMedia(mediaList);
        }

        RetrofitClient.getApiService().createPost(post).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
