package com.example.masterai.ui.ai;

import static com.example.masterai.utils.ViewsUtils.load;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.databinding.DialogAudioPlayerBottomSheetBinding;
import com.example.masterai.databinding.DialogLoadingBottomSheetBinding;
import com.example.masterai.databinding.FragmentVoiceBinding;
import com.example.masterai.model.AudioResponse;
import com.example.masterai.model.Generation;
import com.example.masterai.model.GenerationResponse;
import com.example.masterai.utils.HintSliderUtil;
import com.example.masterai.utils.UserManager;
import com.example.masterai.utils.ViewsUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoiceFragment extends Fragment {

    private FragmentVoiceBinding binding;
    private BottomSheetDialog loadingDialog;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable updateSeekBar;
    private AudioAdapter audioAdapter;
    private List<Generation> audioList = new ArrayList<>();
    private HintSliderUtil hintSliderUtil;

    // Search state
    private String currentSearchQuery = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVoiceBinding.inflate(inflater, container, false);
        initView();
        return binding.getRoot();
    }

    private void initView() {
        ViewsUtils.controlBottomNavWithScrollView(binding.nestedScrollView,this);

        //hint
        List<String> myHints = Arrays.asList(
                "Mô tả bài nhạc bạn muốn tạo...",
                "Ví dụ: 'Nhạc chill lofi để học bài, nhẹ nhàng'",
                "Thử: 'Beat hip hop sôi động, phong cách trap'",
                "Bạn muốn tạo nhạc với cảm xúc gì?",
                "Gợi ý: 'Nhạc piano buồn, không lời, cinematic'"
        );
        hintSliderUtil = new HintSliderUtil(binding.tsHint, binding.etPrompt, myHints);

        // Search listener
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                searchAudioGenerations();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupListeners();
        fetchAudioGenerations();
    }

    private void setupRecyclerView() {
        audioAdapter = new AudioAdapter((item, position) -> {
            showAudioPlayerBottomSheet(item.getMediaUrl());
        });
        binding.rvGenerations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvGenerations.setAdapter(audioAdapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (currentSearchQuery.isEmpty()) {
                fetchAudioGenerations();
            } else {
                searchAudioGenerations();
            }
        });

        binding.btnGenerate.setOnClickListener(v -> {
            String promptText = binding.etPrompt.getText().toString().trim();
            if (promptText.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập nội dung cần tạo nhạc", Toast.LENGTH_SHORT).show();
                binding.etPrompt.requestFocus();
                return;
            }
            generateAudioFromAPI(promptText);
        });
    }

    private void searchAudioGenerations() {
        String userId = UserManager.getInstance(getContext()).getUser().getId();
        RetrofitClient.getApiService().searchGenerations(
                userId,
                "audio",
                currentSearchQuery,
                "newest",
                null,
                null
        ).enqueue(new Callback<GenerationResponse>() {
            @Override
            public void onResponse(Call<GenerationResponse> call, Response<GenerationResponse> response) {
                binding.swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    audioList = response.body().data;
                    audioAdapter.setAudioList(audioList);
                }
            }

            @Override
            public void onFailure(Call<GenerationResponse> call, Throwable t) {
                binding.swipeRefresh.setRefreshing(false);
                Log.e("API_ERROR", "Search Audio: " + t.getMessage());
            }
        });
    }

    private void fetchAudioGenerations() {
        showShimmer(true);
        String userId = UserManager.getInstance(getContext()).getUser().getId();

        RetrofitClient.getApiService().getGenerations(userId, "audio", 1, 50).enqueue(new Callback<GenerationResponse>() {
            @Override
            public void onResponse(Call<GenerationResponse> call, Response<GenerationResponse> response) {
                showShimmer(false);
                binding.swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    audioList = response.body().data;
                    audioAdapter.setAudioList(audioList);
                } else {
                    Toast.makeText(getContext(), "Không thể lấy lịch sử nhạc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenerationResponse> call, Throwable t) {
                showShimmer(false);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showShimmer(boolean show) {
        if (show) {
            binding.layoutShimmer.shimmerViewContainer.setVisibility(View.VISIBLE);
            binding.layoutShimmer.shimmerViewContainer.startShimmer();
            binding.rvGenerations.setVisibility(View.GONE);
        } else {
            binding.layoutShimmer.shimmerViewContainer.stopShimmer();
            binding.layoutShimmer.shimmerViewContainer.setVisibility(View.GONE);
            binding.rvGenerations.setVisibility(View.VISIBLE);
        }
    }

    private void generateAudioFromAPI(String prompt) {
        showLoadingDialog();
        String userId = UserManager.getInstance(getContext()).getUser().getId();
        RetrofitClient.getApiService().generateAudio(userId, prompt).enqueue(new Callback<AudioResponse>() {
            @Override
            public void onResponse(Call<AudioResponse> call, Response<AudioResponse> response) {
                hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showAudioPlayerBottomSheet(response.body().getMediaUrl());
                    // Tự động làm mới danh sách sau khi tạo thành công
                    fetchAudioGenerations();
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Lỗi không xác định";
                    Toast.makeText(getContext(), "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AudioResponse> call, Throwable t) {
                hideLoadingDialog();
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoadingDialog() {
        loadingDialog = new BottomSheetDialog(requireContext());
        DialogLoadingBottomSheetBinding loadingBinding = DialogLoadingBottomSheetBinding.inflate(getLayoutInflater());
        loadingBinding.tvStatus.setText("Đang tạo âm thanh...");
        ViewsUtils.load(loadingBinding.loading,R.drawable.gif_music);
        ViewsUtils.play();
        loadingDialog.setContentView(loadingBinding.getRoot());
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            ViewsUtils.clear();
            loadingDialog.dismiss();
        }
    }

    private void showAudioPlayerBottomSheet(String audioUrl) {
        BottomSheetDialog audioDialog = new BottomSheetDialog(requireContext());
        DialogAudioPlayerBottomSheetBinding audioBinding = DialogAudioPlayerBottomSheetBinding.inflate(getLayoutInflater());
        audioDialog.setContentView(audioBinding.getRoot());
        ViewsUtils.load(audioBinding.animationMusic, R.drawable.gif_music_play);


        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());

        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Không thể tải âm thanh", Toast.LENGTH_SHORT).show();
        }

        audioBinding.btnPlayPause.setEnabled(false);
        mediaPlayer.setOnPreparedListener(mp -> {
            audioBinding.btnPlayPause.setEnabled(true);
            audioBinding.seekBar.setMax(mp.getDuration());
            audioBinding.tvTotalTime.setText(formatTime(mp.getDuration()));
            mp.start();
            ViewsUtils.play();
            audioBinding.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            startSeekBarUpdate(audioBinding);
        });

        audioBinding.btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                ViewsUtils.pause();
                audioBinding.btnPlayPause.setImageResource(R.drawable.ic_play_white);
            } else {
                mediaPlayer.start();
                ViewsUtils.play();
                audioBinding.btnPlayPause.setImageResource(R.drawable.ic_pause);
                startSeekBarUpdate(audioBinding);
            }
        });

        audioBinding.btnRewind.setOnClickListener(v -> {
            int currentPos = mediaPlayer.getCurrentPosition();
            mediaPlayer.seekTo(Math.max(currentPos - 10000, 0));
        });

        audioBinding.btnForward.setOnClickListener(v -> {
            int currentPos = mediaPlayer.getCurrentPosition();
            mediaPlayer.seekTo(Math.min(currentPos + 10000, mediaPlayer.getDuration()));
        });

        audioBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    audioBinding.tvCurrentTime.setText(formatTime(progress));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            audioBinding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            audioBinding.seekBar.setProgress(0);
            audioBinding.tvCurrentTime.setText("00:00");
            handler.removeCallbacks(updateSeekBar);
        });

        audioDialog.setOnDismissListener(dialog -> {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
                ViewsUtils.clear();
            }
            handler.removeCallbacks(updateSeekBar);
        });

        audioDialog.show();
    }

    private void startSeekBarUpdate(DialogAudioPlayerBottomSheetBinding audioBinding) {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPos = mediaPlayer.getCurrentPosition();
                    audioBinding.seekBar.setProgress(currentPos);
                    audioBinding.tvCurrentTime.setText(formatTime(currentPos));
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateSeekBar);
    }

    private String formatTime(int millis) {
        int minutes = (millis / 1000) / 60;
        int seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBar);
        binding = null;
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