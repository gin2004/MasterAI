package com.example.masterai.ui.ai;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.example.masterai.databinding.FragmentImageBinding;
import com.example.masterai.model.Asset;
import com.example.masterai.model.AssetResponse;
import com.example.masterai.model.Generation;
import com.example.masterai.model.GenerationResponse;
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


public class ImageFragment extends Fragment {

    private RecyclerView rvAssets, rvGenerations;
    private FragmentImageBinding binding;
    private BottomSheetDialog loadingDialog;
    private String imageLink;
    private GenerationAdapter generationAdapter;
    private AssetAdapter assetAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentImageBinding.inflate(inflater, container, false);
        initViews();
        
        // Chỉ load data lần đầu tiên
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
            fetchGenerations(0);
            fetchAssets();
            generationAdapter.setLoading(true);
            assetAdapter.setLoading(true);
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
        binding.imgResult.setOnClickListener(v->{
            if(imageLink!=null && !imageLink.isEmpty()){
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

    private void showLoadingBottomSheet() {
        loadingDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_loading_bottom_sheet, null);
        loadingDialog.setContentView(view);
        loadingDialog.setCancelable(false);
        loadingDialog.show();
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

    private void startWorkflow() {
        String prompt = binding.etPrompt.getText().toString().trim();
        if (prompt.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập câu lệnh", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingBottomSheet();

        // Lấy giá trị Resolution dựa trên RadioButton đang được chọn
        String selectedRes = "1K";
        if (binding.radio2K.isChecked()) {
            selectedRes = "2K";
        }
        // lấy tỉ lệ
        String ratio= binding.spinnerSelectModel.getSelectedItem().toString();
        // lấy user id
        String user_id = "c7f8bca5-6201-41ad-911b-e47636f85d27";

        RequestBody rbPrompt = RequestBody.create(MediaType.parse("text/plain"), prompt);
        RequestBody rbRatio = RequestBody.create(MediaType.parse("text/plain"), ratio);
        RequestBody rbRes = RequestBody.create(MediaType.parse("text/plain"), selectedRes);
        RequestBody userId = RequestBody.create(MediaType.parse("text/plain"), user_id);

        RetrofitClient.getApiService().generateImage(userId,rbPrompt, rbRatio, rbRes, null)
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

    private void setupAssetsList(List<Asset> assets) {
        assetAdapter = new AssetAdapter(assets);
        assetAdapter.setLoading(false);
        rvAssets.setAdapter(assetAdapter);
    }

    private void setupGenerationsList(List<Generation> gens) {
        generationAdapter = new GenerationAdapter(gens);
        generationAdapter.setLoading(false);
        rvGenerations.setAdapter(generationAdapter);
    }
    private void fetchGenerations(int page) {
        String userId = "c7f8bca5-6201-41ad-911b-e47636f85d27"; // ID User từ Auth
        String type = "image";

        // Gọi API lấy lịch sử (Page mặc định là 1, limit là 10)
        RetrofitClient.getApiService().getGenerations(userId, type, page, 10)
                .enqueue(new Callback<GenerationResponse>() {
                    @Override
                    public void onResponse(Call<GenerationResponse> call, Response<GenerationResponse> response) {
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Generation> list = response.body().data;
                            setupGenerationsList(list);
                        }
                    }
                    @Override
                    public void onFailure(Call<GenerationResponse> call, Throwable t) {
                        binding.swipeRefresh.setRefreshing(false);
                        Log.e("API_ERROR", "Generations: " + t.getMessage());
                    }
                });
    }
    private void fetchAssets() {
        String userId = "c7f8bca5-6201-41ad-911b-e47636f85d27";
        String type = "image";

        RetrofitClient.getApiService().getAssets(userId, type)
                .enqueue(new Callback<AssetResponse>() {
                    @Override
                    public void onResponse(Call<AssetResponse> call, Response<AssetResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AssetResponse assetRes = response.body();

                            setupAssetsList(assetRes.getData());

                            // 2. Cập nhật số lượng lên Badge (TextView cạnh tiêu đề My Assets)
                            binding.countAsset.setText(String.valueOf(assetRes.getTotalItems()));

                            // Nếu không có asset nào, có thể ẩn danh sách hoặc hiện thông báo trống
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