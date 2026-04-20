package com.example.masterai.ui.comminity;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.masterai.MainActivity;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.model.Media;
import com.example.masterai.model.Post;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPostFragment extends Fragment {

    private EditText etPostContent;
    private View btnUpdatePost;
    private ImageButton btnUploadImage, btnBack;
    private RecyclerView rvImages;
    private ImagePreviewAdapter imagePreviewAdapter;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private Post postToEdit;

    public static EditPostFragment newInstance(Post post) {
        EditPostFragment fragment = new EditPostFragment();
        Bundle args = new Bundle();
        args.putSerializable("post", post);
        fragment.setArguments(args);
        return fragment;
    }

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        ClipData clipData = result.getData().getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri uri = clipData.getItemAt(i).getUri();
                            requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            selectedImageUris.add(uri);
                        }
                    } else if (result.getData().getData() != null) {
                        Uri uri = result.getData().getData();
                        requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        selectedImageUris.add(uri);
                    }
                    updateImagesVisibility();
                }
            }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postToEdit = (Post) getArguments().getSerializable("post");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_post, container, false);

        etPostContent = view.findViewById(R.id.etPostContent);
        btnUpdatePost = view.findViewById(R.id.btnUpdatePost);
        btnUploadImage = view.findViewById(R.id.btnUploadImage);
        btnBack = view.findViewById(R.id.btnBack);
        rvImages = view.findViewById(R.id.rvImages);

        if (postToEdit != null) {
            etPostContent.setText(postToEdit.getContent());
            if (postToEdit.getMedia() != null) {
                for (Media m : postToEdit.getMedia()) {
                    selectedImageUris.add(Uri.parse(m.getUrl()));
                }
            }
        }

        rvImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imagePreviewAdapter = new ImagePreviewAdapter(selectedImageUris);
        rvImages.setAdapter(imagePreviewAdapter);

        btnUploadImage.setOnClickListener(v -> openGallery());
        btnUpdatePost.setOnClickListener(v -> updatePost());
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        updateImagesVisibility();

        // Ẩn BottomNavigationView
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hiện lại BottomNavigationView khi thoát khỏi fragment này
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavVisibility(View.VISIBLE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickImageLauncher.launch(intent);
    }

    private void updateImagesVisibility() {
        if (selectedImageUris.isEmpty()) {
            rvImages.setVisibility(View.GONE);
        } else {
            rvImages.setVisibility(View.VISIBLE);
            imagePreviewAdapter.notifyDataSetChanged();
        }
    }

    private void updatePost() {
        String content = etPostContent.getText().toString().trim();

        if (TextUtils.isEmpty(content) && selectedImageUris.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập nội dung hoặc chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("content", content);

        List<Map<String, String>> mediaData = new ArrayList<>();
        for (Uri uri : selectedImageUris) {
            Map<String, String> m = new HashMap<>();
            m.put("url", uri.toString());
            m.put("media_type", "image");
            m.put("source", "upload");
            mediaData.add(m);
        }
        body.put("media", mediaData);

        RetrofitClient.getApiService().updatePost(postToEdit.getId(), body).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
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
