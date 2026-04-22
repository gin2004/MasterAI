package com.example.masterai.ui.comminity;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), uris -> {
                if (!uris.isEmpty()) {
                    selectedImageUris.addAll(uris);
                    updateImagesVisibility();
                }
            });

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

    private void updatePost() {
        String content = etPostContent.getText().toString().trim();

        if (TextUtils.isEmpty(content) && selectedImageUris.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập nội dung hoặc chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpdatePost.setEnabled(false);

        // 1. Chuẩn bị dữ liệu text
        RequestBody rbContent = createPartFromString(content);
        RequestBody rbVisibility = createPartFromString("public");

        // 2. PHÂN LOẠI ẢNH
        List<MultipartBody.Part> keptMediaParts = new ArrayList<>();
        List<MultipartBody.Part> newFileParts = new ArrayList<>();

        for (Uri uri : selectedImageUris) {
            String uriString = uri.toString();

            if (uriString.startsWith("http")) {
                // ĐÂY LÀ ẢNH CŨ: Gửi URL về để Backend giữ lại
                keptMediaParts.add(MultipartBody.Part.createFormData("kept_media", uriString));
            } else {
                // ĐÂY LÀ ẢNH MỚI: Nén và tạo Part để upload
                MultipartBody.Part part = prepareFilePart("files", uri);
                if (part != null) {
                    newFileParts.add(part);
                }
            }
        }

        // 3. Gọi API
        RetrofitClient.getApiService().updatePost(postToEdit.getId(), rbContent, rbVisibility, keptMediaParts, newFileParts)
                .enqueue(new Callback<Post>() {
                    @Override
                    public void onResponse(Call<Post> call, Response<Post> response) {
                        btnUpdatePost.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Cập nhật bài viết thành công!", Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        } else {
                            // In lỗi 413 hoặc lỗi khác để debug
                            Log.e("EDIT_ERR", "Code: " + response.code());
                            Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Post> call, Throwable t) {
                        btnUpdatePost.setEnabled(true);
                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private RequestBody createPartFromString(String descriptionString) {
        if (descriptionString == null) descriptionString = "";
        return RequestBody.create(MultipartBody.FORM, descriptionString);
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        try {
            // Kiểm tra nếu là URL ảnh cũ (dành cho EditPostFragment)
            if (fileUri.toString().startsWith("http")) return null;

            android.content.ContentResolver contentResolver = requireContext().getContentResolver();
            String fileName = "upload_" + System.currentTimeMillis() + ".jpg";

            InputStream inputStream = contentResolver.openInputStream(fileUri);
            if (inputStream == null) return null;

            // Gọi hàm getBytes đã có nén ảnh ở trên
            byte[] bytes = getBytes(inputStream);

            // Đóng stream để giải phóng tài nguyên
            inputStream.close();

            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("image/jpeg"), // Sau khi nén bằng JPEG ở getBytes
                    bytes
            );

            return MultipartBody.Part.createFormData(partName, fileName, requestFile);
        } catch (Exception e) {
            Log.e("UPLOAD_ERR", "Lỗi nén ảnh: " + e.getMessage());
            return null;
        }
    }
    public byte[] getBytes(InputStream inputStream) throws IOException {
        // 1. Giải mã InputStream thành Bitmap
        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);

        // 2. RESIZE: Nếu ảnh rộng hơn 1080px, thu nhỏ nó lại
        int maxWidth = 1080;
        if (bitmap.getWidth() > maxWidth) {
            int newHeight = (int) (bitmap.getHeight() * ((float) maxWidth / bitmap.getWidth()));
            bitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
        }

        // 3. NÉN CHẤT LƯỢNG: Giảm xuống 70% (mức tối ưu cho di động)
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream);

        return outputStream.toByteArray();
    }
}
