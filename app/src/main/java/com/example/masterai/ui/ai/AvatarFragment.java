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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.databinding.FragmentAvatarBinding;
import com.example.masterai.model.Asset;
import com.example.masterai.model.AssetResponse;
import com.example.masterai.model.Generation;
import com.example.masterai.model.GenerationResponse;
import com.example.masterai.model.ImageResponse;
import com.example.masterai.model.PromptResponse;
import com.example.masterai.utils.AIUtils;
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

import okhttp3.MultipartBody;

public class AvatarFragment extends Fragment {
    private RecyclerView rvAssets, rvGenerations;
    private FragmentAvatarBinding binding;
    private BottomSheetDialog loadingDialog;
    private String imageLink;
    private android.net.Uri selectedImageUri = null;
    
    private GenerationAdapter generationAdapter;
    private AssetAdapter assetAdapter;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.cardPreview.setVisibility(View.VISIBLE);
                    Glide.with(requireContext())
                            .load(uri)
                            .into(binding.imgPreview);
                    binding.btnUpload.setText("Đổi ảnh");
                } else {
                    Toast.makeText(requireContext(), "Bạn chưa chọn ảnh nào", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAvatarBinding.inflate(inflater, container, false);
        initViews();
        
        fetchGenerations(0);
        fetchAssets();

        return binding.getRoot();
    }

    private void initViews() {
        rvAssets = binding.rvAssets;
        rvGenerations = binding.rvGenerations;

        // Khởi tạo adapter với trạng thái loading mặc định
        generationAdapter = new GenerationAdapter(new ArrayList<>());
        assetAdapter = new AssetAdapter(new ArrayList<>());
        
        rvGenerations.setAdapter(generationAdapter);
        rvAssets.setAdapter(assetAdapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            generationAdapter.setLoading(true);
            assetAdapter.setLoading(true);
            fetchGenerations(0);
            fetchAssets();
        });

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
        binding.btnUpload.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

    }

    private void enhancePrompt() {
        String userPrompt = binding.etPrompt.getText().toString().trim();
        if (userPrompt.isEmpty()) return;

        binding.enhanceButton.setEnabled(false);
        binding.enhanceButton.setText("Đang tối ưu...");

        RetrofitClient.getApiService().enhancePrompt(userPrompt).enqueue(new Callback<PromptResponse>() {
            @Override
            public void onResponse(Call<PromptResponse> call, Response<PromptResponse> response) {
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

        String selectedRes = "1K";
        if (binding.radio2K.isChecked()) {
            selectedRes = "2K";
        }
        //getUserId
        String userId = "c7f8bca5-6201-41ad-911b-e47636f85d27";

        RequestBody rbPrompt = RequestBody.create(MediaType.parse("text/plain"), prompt);
        RequestBody rbRatio = RequestBody.create(MediaType.parse("text/plain"), "1:1");
        RequestBody rbRes = RequestBody.create(MediaType.parse("text/plain"), selectedRes);
        RequestBody user_id = RequestBody.create(MediaType.parse("text/plain"), userId);

        MultipartBody.Part imagePart = prepareFilePart("image", selectedImageUri);

        RetrofitClient.getApiService().generateImage(user_id,rbPrompt, rbRatio, rbRes,imagePart)
                .enqueue(new Callback<ImageResponse>() {
                    @Override
                    public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                        if (loadingDialog != null && loadingDialog.isShowing()) {
                            loadingDialog.dismiss();
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            ImageResponse data = response.body();
                            if (data.media_url != null && !data.media_url.isEmpty()) {
                                imageLink = data.media_url;
                                showResultBottomSheet(data.media_url);
                            } else {
                                showErrorInBottomSheet("Ảnh đã tạo nhưng app không đọc được Link");
                            }
                        } else {
                            showErrorInBottomSheet("Lỗi xử lý từ server: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ImageResponse> call, Throwable t) {
                        if (loadingDialog != null && loadingDialog.isShowing()) {
                            loadingDialog.dismiss();
                        }
                        showErrorInBottomSheet("Lỗi kết nối/Dữ liệu: " + t.getMessage());
                    }
                });
    }


    private void showResultBottomSheet(String imageUrl) {
        BottomSheetDialog resultDialog = new BottomSheetDialog(requireContext(), R.style.FullScreenBottomSheetDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_result_bottom_sheet, null);
        resultDialog.setContentView(view);

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
        LinearLayout share = view.findViewById(R.id.share);

        Glide.with(this).load(imageUrl).into(imgResult);
        btnBack.setOnClickListener(v -> resultDialog.dismiss());
        share.setOnClickListener(v -> {
            AIUtils.getInstance().shareImageAndText(imageUrl, binding.etPrompt.getText().toString());
        });
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
            InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri);
            if (inputStream == null) return null;

            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] bytes = byteBuffer.toByteArray();
            inputStream.close();

            String mimeType = requireContext().getContentResolver().getType(fileUri);
            if (mimeType == null) mimeType = "image/jpeg";

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
            return MultipartBody.Part.createFormData(partName, "upload_image.jpg", requestFile);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void fetchGenerations(int page) {
        String userId = "c7f8bca5-6201-41ad-911b-e47636f85d27";
        String type = "avatar";

        RetrofitClient.getApiService().getGenerations(userId, type, page, 10)
                .enqueue(new Callback<GenerationResponse>() {
                    @Override
                    public void onResponse(Call<GenerationResponse> call, Response<GenerationResponse> response) {
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Generation> list = response.body().data;
                            generationAdapter = new GenerationAdapter(list);
                            generationAdapter.setLoading(false);
                            rvGenerations.setAdapter(generationAdapter);
                        }
                    }
                    @Override
                    public void onFailure(Call<GenerationResponse> call, Throwable t) {
                        binding.swipeRefresh.setRefreshing(false);
                        generationAdapter.setLoading(false);
                        Log.e("API_ERROR", "Generations: " + t.getMessage());
                    }
                });
    }
    private void fetchAssets() {
        String userId = "c7f8bca5-6201-41ad-911b-e47636f85d27";
        String type = "avatar";

        RetrofitClient.getApiService().getAssets(userId, type)
                .enqueue(new Callback<AssetResponse>() {
                    @Override
                    public void onResponse(Call<AssetResponse> call, Response<AssetResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AssetResponse assetRes = response.body();
                            assetAdapter = new AssetAdapter(assetRes.getData());
                            assetAdapter.setLoading(false);
                            rvAssets.setAdapter(assetAdapter);
                            
                            binding.countAsset.setText(String.valueOf(assetRes.getTotalItems()));
                            if (assetRes.getTotalItems() == 0) {
                                binding.rvAssets.setVisibility(View.GONE);
                            } else {
                                binding.rvAssets.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AssetResponse> call, Throwable t) {
                        Log.e("API_ERROR", "Assets: " + t.getMessage());
                    }
                });
    }

}