package com.example.masterai.ui.ai;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.databinding.FragmentAvatarBinding;
import com.example.masterai.databinding.FragmentImageBinding;
import com.example.masterai.model.Asset;
import com.example.masterai.model.Generation;
import com.example.masterai.model.ImageResponse;
import com.example.masterai.model.PromptResponse;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AvatarFragment extends Fragment {
    private RecyclerView rvAssets, rvGenerations;
    private FragmentAvatarBinding binding;
    private BottomSheetDialog loadingDialog;
    private String imageLink;
    private android.net.Uri selectedImageUri = null;

    // 2. Đăng ký Launcher "bắt" ảnh
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // Người dùng đã chọn ảnh thành công
                    selectedImageUri = uri;

                    // Hiển thị ảnh lên giao diện
                    binding.imgPreview.setVisibility(View.VISIBLE);
                    binding.imgPreview.setImageURI(uri);

                    // (Tuỳ chọn) Đổi text của nút thành "Đổi ảnh khác"
                    binding.btnSelectImage.setText("Đổi ảnh khác");
                } else {
                    // Người dùng mở thư viện ra nhưng bấm Hủy/Back
                    Toast.makeText(requireContext(), "Bạn chưa chọn ảnh nào", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        binding = FragmentAvatarBinding.inflate(inflater, container, false);
        initViews();
        setupAssetsList();
        setupGenerationsList();

        return binding.getRoot();
    }

    private void initViews() {
        rvAssets = binding.rvAssets;
        rvGenerations = binding.rvGenerations;
        RadioGroup radioGroup = binding.radioGroupResolution;
        RadioButton radio1K = binding.radio1K;
        RadioButton radio2K = binding.radio2K;

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId == R.id.radio1K) {
                radio1K.setBackgroundResource(R.drawable.bg_radio_selected);
                radio1K.setTextColor(getResources().getColor(android.R.color.white));

                radio2K.setBackgroundResource(R.drawable.bg_radio_unselected);
                radio2K.setTextColor(Color.parseColor("#A0A0A0"));

            } else if (checkedId == R.id.radio2K) {
                radio2K.setBackgroundResource(R.drawable.bg_radio_selected);
                radio2K.setTextColor(getResources().getColor(android.R.color.white));

                radio1K.setBackgroundResource(R.drawable.bg_radio_unselected);
                radio1K.setTextColor(Color.parseColor("#A0A0A0"));
            }
        });
        binding.enhanceButton.setOnClickListener(v -> {
            enhancePrompt();
        });
        binding.btnGenerate.setOnClickListener(v -> {
            startWorkflow();
        });
        binding.imgResult.setOnClickListener(v -> {
            if (imageLink != null && !imageLink.isEmpty()) {
                showResultBottomSheet(imageLink);
            }
        });

    }

    private void enhancePrompt() {
        String userPrompt = binding.etPrompt.getText().toString().trim();
        if (userPrompt.isEmpty()) return;

        // 1. Khóa nút và đổi text để báo hiệu đang xử lý
        binding.enhanceButton.setEnabled(false);
        binding.enhanceButton.setText("Đang tối ưu...");

        RetrofitClient.getApiService().enhancePrompt(userPrompt).enqueue(new Callback<PromptResponse>() {
            @Override
            public void onResponse(Call<PromptResponse> call, Response<PromptResponse> response) {
                // 2. Mở lại nút
                binding.enhanceButton.setEnabled(true);
                binding.enhanceButton.setText("Nâng cấp lệnh");

                if (response.isSuccessful() && response.body() != null) {
                    String enhanced = response.body().enhanced_prompt;
                    binding.etPrompt.setText(enhanced);
                } else {
                    Toast.makeText(requireContext(), "Lỗi server khi nâng cấp", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PromptResponse> call, Throwable t) {
                // Mở lại nút nếu lỗi mạng
                binding.enhanceButton.setEnabled(true);
                binding.enhanceButton.setText("Nâng cấp lệnh");
                Log.e("DEBUG_TRUTH", "Đứt cáp mạng thật: " + t.getMessage());
                Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void startWorkflow() {
        String prompt = binding.etPrompt.getText().toString().trim();
        if (prompt.isEmpty() || selectedImageUri == null) {
            Toast.makeText(requireContext(), "Vui lòng nhập câu lệnh hoặc tải ảnh lên", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingBottomSheet();

        // Lấy giá trị Resolution dựa trên RadioButton đang được chọn
        String selectedRes = "1K";
        if (binding.radio2K.isChecked()) {
            selectedRes = "2K";
        }

        RequestBody rbPrompt = RequestBody.create(MediaType.parse("text/plain"), prompt);
        RequestBody rbRatio = RequestBody.create(MediaType.parse("text/plain"), "1:1");
        RequestBody rbRes = RequestBody.create(MediaType.parse("text/plain"), selectedRes);

        //  Đóng gói File Ảnh
        MultipartBody.Part imagePart = null;
        imagePart = prepareFilePart("image", selectedImageUri);

        RetrofitClient.getApiService().generateImage(rbPrompt, rbRatio, rbRes, null)
                .enqueue(new Callback<ImageResponse>() {
                    @Override
                    public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                        // Tắt loading ngay lập tức khi có phản hồi
                        if (loadingDialog != null && loadingDialog.isShowing()) {
                            loadingDialog.dismiss();
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            ImageResponse data = response.body();

                            // In ra Logcat để xem Gson có đọc được dữ liệu không
                            Log.d("DEBUG_API", "Success flag: " + data.success + " | URL: " + data.media_url);

                            if (data.media_url != null && !data.media_url.isEmpty()) {
                                imageLink = data.media_url;
                                showResultBottomSheet(data.media_url);
                            } else {
                                showErrorInBottomSheet("Ảnh đã tạo nhưng app không đọc được Link URL (Biến bị null).");
                            }
                        } else {
                            try {
                                String err = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                                Log.e("DEBUG_API", "HTTP Error: " + response.code() + " - " + err);
                                showErrorInBottomSheet("Lỗi xử lý từ server: " + response.code());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ImageResponse> call, Throwable t) {
                        if (loadingDialog != null && loadingDialog.isShowing()) {
                            loadingDialog.dismiss();
                        }

                        // Đây là dòng quan trọng nhất để xem lỗi mạng hay lỗi JSON
                        Log.e("DEBUG_API", "Retrofit Failure: " + t.getMessage());
                        showErrorInBottomSheet("Lỗi kết nối/Dữ liệu: " + t.getMessage());
                    }
                });
    }

    private void setupAssetsList() {
        List<Asset> assets = new ArrayList<>();
        assets.add(new Asset("Fox", "10", R.drawable.ic_launcher_background));
        assets.add(new Asset("Veridian E...", "Free", R.drawable.ic_launcher_background));
        assets.add(new Asset("Grim Cyp...", "Free", R.drawable.ic_launcher_background));
        assets.add(new Asset("Trielia", "Free", R.drawable.ic_launcher_background));
        assets.add(new Asset("Alieh", "Free", R.drawable.ic_launcher_background));
        assets.add(new Asset("Cyber Punk", "20", R.drawable.ic_launcher_background));

        AssetAdapter adapter = new AssetAdapter(assets);
        rvAssets.setAdapter(adapter);
    }

    private void setupGenerationsList() {
        List<Generation> gens = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            gens.add(new Generation("Gen " + i, R.drawable.ic_launcher_background));
        }

        GenerationAdapter adapter = new GenerationAdapter(gens);
        rvGenerations.setAdapter(adapter);
    }

    private void showResultBottomSheet(String imageUrl) {
        BottomSheetDialog resultDialog = new BottomSheetDialog(requireContext(), R.style.FullScreenBottomSheetDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_result_bottom_sheet, null);
        resultDialog.setContentView(view);

        // Make it full screen
        FrameLayout bottomSheet = resultDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
            ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            bottomSheet.setLayoutParams(layoutParams);
        }

        ImageView imgResult = view.findViewById(R.id.imgResult);
        ImageView btnBack = view.findViewById(R.id.btnBack);

        Glide.with(this).load(imageUrl).into(imgResult);

        btnBack.setOnClickListener(v -> resultDialog.dismiss());

        resultDialog.show();
    }

    private void showErrorInBottomSheet(String error) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            TextView tvStatus = loadingDialog.findViewById(R.id.tvStatus);
            TextView tvError = loadingDialog.findViewById(R.id.tvError);
            LinearProgressIndicator progressBar = loadingDialog.findViewById(R.id.progressBar);
            MaterialButton btnClose = loadingDialog.findViewById(R.id.btnClose);

            if (tvStatus != null) tvStatus.setText("Lỗi xảy ra");
            if (tvError != null) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText(error);
            }
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (btnClose != null) {
                btnClose.setVisibility(View.VISIBLE);
                btnClose.setOnClickListener(v -> loadingDialog.dismiss());
            }
        }
    }

    private void showLoadingBottomSheet() {
        loadingDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_loading_bottom_sheet, null);
        loadingDialog.setContentView(view);
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }


    private MultipartBody.Part prepareFilePart(String partName, android.net.Uri fileUri) {
        if (fileUri == null) return null;
        try {
            // 1. Mở luồng đọc file từ Uri
            InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri);
            if (inputStream == null) return null;

            // 2. Chuyển đổi InputStream thành mảng byte
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] bytes = byteBuffer.toByteArray();
            inputStream.close();

            // 3. Lấy định dạng file (MimeType) - Ví dụ: image/jpeg, image/png
            String mimeType = requireContext().getContentResolver().getType(fileUri);
            if (mimeType == null) mimeType = "image/jpeg"; // Giá trị mặc định an toàn

            // 4. Tạo RequestBody từ mảng byte
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);

            // 5. Trả về MultipartBody.Part (Tên part phải khớp với request.FILES.get('image') trên Django)
            return MultipartBody.Part.createFormData(partName, "upload_image.jpg", requestFile);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}