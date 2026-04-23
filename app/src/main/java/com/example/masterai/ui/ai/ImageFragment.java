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
import com.example.masterai.utils.HintSliderUtil;
import com.example.masterai.utils.UserManager;
import com.example.masterai.utils.ViewsUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
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
    String user_id = null;
    private ImageResponse imageResponse;
    private String currentGenerationId;
    private HintSliderUtil hintSliderUtil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentImageBinding.inflate(inflater, container, false);
        initViews();

        user_id = UserManager.getInstance(requireContext()).getUser().getId();
        // Chỉ load data lần đầu tiên
        fetchGenerations(0);
        fetchAssets();
        
        return binding.getRoot();
    }

    private void initViews() {
        rvAssets = binding.rvAssets;
        rvGenerations = binding.rvGenerations;
        //set up bottom nav
        ViewsUtils.controlBottomNavigationView(rvGenerations, this);

        // Khởi tạo adapter với trạng thái loading mặc định
        generationAdapter = new GenerationAdapter(new ArrayList<>());
        assetAdapter = new AssetAdapter(new ArrayList<>());

        rvGenerations.setAdapter(generationAdapter);
        rvAssets.setAdapter(assetAdapter);

        assetAdapter.setOnItemClickListener(asset -> {
            if (asset.getPrompt() != null) {
                binding.etPrompt.setText(asset.getPrompt());
            }
        });
        
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
        //hint
        List<String> myHints = Arrays.asList(
                "Mô tả bức ảnh bạn muốn tạo...",
                "Ví dụ: 'Một con mèo phi hành gia trên sao Hỏa'",
                "Thử: 'Phong cảnh hoàng hôn theo phong cách anime'",
                "Bạn muốn tạo hình ảnh như thế nào hôm nay?",
                "Gợi ý: 'Chân dung cô gái cyberpunk, ánh đèn neon'"
        );
        hintSliderUtil = new HintSliderUtil(binding.tsHint, binding.etPrompt, myHints);
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

        ImageView loading = view.findViewById(R.id.loading);
        ViewsUtils.load(loading,R.drawable.gif_loading_image);
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
        MaterialButton btnSaveAsset = view.findViewById(R.id.btnSaveAsset);

        Glide.with(this).load(imageUrl).into(imgResult);

        btnBack.setOnClickListener(v -> resultDialog.dismiss());
        //lưu vào asset
        btnSaveAsset.setOnClickListener(v->{
            saveAsset();
        });
        

        resultDialog.show();
    }

    private void saveAsset() {
        if(currentGenerationId != null){
            RetrofitClient.getApiService().addAsset(user_id, currentGenerationId).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.isSuccessful()){
                        String json = null;
                        try {
                            json = response.body().string();
                            JSONObject jsonObject = new JSONObject(json);
                            if(jsonObject.has("message")){
                                Toast.makeText(requireContext(), jsonObject.optString("message"), Toast.LENGTH_SHORT).show();
                                fetchAssets(); // Cập nhật lại danh sách asset
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(requireContext(), "Không thể thêm", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                            ViewsUtils.clear();
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            ImageResponse data = response.body();
                            imageResponse = data;
                            currentGenerationId = data.generation_id;
                            if (data.media_url != null && !data.media_url.isEmpty()) {
                                imageLink = data.media_url;
                                showResultBottomSheet(data.media_url);
                            } else {
                                showErrorInBottomSheet("Ảnh đã tạo nhưng app không đọc được");
                            }
                        } else {
                            try {
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
                        showErrorInBottomSheet("Lỗi kết nối/Dữ liệu: " + t.getMessage());
                    }
                });
    }

    private void setupAssetsList(List<Asset> assets) {
        assetAdapter = new AssetAdapter(assets);
        assetAdapter.setLoading(false);
        assetAdapter.setOnItemClickListener(asset -> {
            if (asset.getPrompt() != null) {
                binding.etPrompt.setText(asset.getPrompt());
            }
        });
        rvAssets.setAdapter(assetAdapter);
    }

    private void setupGenerationsList(List<Generation> gens) {
        generationAdapter = new GenerationAdapter(gens);
        generationAdapter.setLoading(false);
        generationAdapter.setOnItemClickListener(generation -> {
            currentGenerationId = generation.getId();
            imageLink = generation.getMediaUrl();
            showResultBottomSheet(generation.getMediaUrl());
        });
        rvGenerations.setAdapter(generationAdapter);
    }
    private void fetchGenerations(int page) {
        String type = "image";
        RetrofitClient.getApiService().getGenerations(user_id, type, page, 10)
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
        String type = "image";

        RetrofitClient.getApiService().getAssets(user_id, type)
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

    @Override
    public void onResume() {
        super.onResume();
        // Bắt đầu chạy animation khi Activity hiển thị lên màn hình
        if (hintSliderUtil != null) {
            hintSliderUtil.startSliding();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Bắt buộc phải dừng khi ẩn Activity để tránh Memory Leak
        if (hintSliderUtil != null) {
            hintSliderUtil.stopSliding();
        }
    }
}