package com.example.masterai.ui.activity;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.bumptech.glide.Glide;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.databinding.ActivityProfileBinding;
import com.example.masterai.model.FollowRequest;
import com.example.masterai.model.FollowResponse;
import com.example.masterai.model.User;
import com.example.masterai.ui.profile.ProfileFragment;
import com.example.masterai.ui.profile.ProfileLikeFragment;
import com.example.masterai.ui.profile.ProfileUserPostFragment;
import com.example.masterai.utils.UserManager;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private static String userId;
    private User userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getIntent().getStringExtra("user_id");
        if (userId == null) {
            finish();
            return;
        }

        setupViewPager();
        loadUserProfile();
        
        binding.btnBack.setOnClickListener(v -> finish());
        setUpFollow();

    }

    private void setUpFollow() {
        binding.btnFollow.setOnClickListener(v -> {
            if (userProfile != null) {
                toggleFollow();
            }
        });
    }

    private void toggleFollow() {

        int countFollower = Integer.parseInt(binding.followerCount.getText().toString());
        if(userProfile.isFollowed()){
            binding.btnFollow.setText("Theo dõi");
            binding.followerCount.setText(String.valueOf(countFollower - 1));
            userProfile.setFollowed(false);
        } else {
            binding.btnFollow.setText("Đã theo dõi");
            binding.followerCount.setText(String.valueOf(countFollower+1));
            userProfile.setFollowed(true);
        }
        performFollowToggle(userProfile.getId());
    }

    private void performFollowToggle(String targetUserId) {
        // Lấy ID của người dùng đang đăng nhập trên máy
        String myUserId = UserManager.getInstance(this).getUser().getId();

        FollowRequest request = new FollowRequest(myUserId);

        RetrofitClient.getApiService().toggleFollow(targetUserId, request)
                .enqueue(new Callback<FollowResponse>() {
                    @Override
                    public void onResponse(Call<FollowResponse> call, Response<FollowResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            boolean isNowFollowing = response.body().isFollowing();

                            Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FollowResponse> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "Lỗi mạng!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserProfile() {
        User currentUser = UserManager.getInstance(this).getUser();
        String currentUserId = currentUser != null ? currentUser.getId() : null;

        RetrofitClient.getApiService().getUserById(userId, currentUserId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    userProfile = user;
                    binding.setUserProfile(user);
                    if (user.getAvatarUrl() != null) {
                        Glide.with(ProfileActivity.this)
                                .load(user.getAvatarUrl())
                                .placeholder(android.R.drawable.ic_menu_report_image)
                                .circleCrop()
                                .into(binding.ivProfileAvatar);
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Lỗi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupViewPager() {
        ProfilePagerAdapter adapter = new ProfilePagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        int []icon = {android.R.drawable.ic_dialog_dialer, android.R.drawable.gallery_thumb};
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) ->
                tab.setIcon(icon[position])
        ).attach();
    }

    private static class ProfilePagerAdapter extends FragmentStateAdapter {
        public ProfilePagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return ProfileUserPostFragment.newInstance(userId);
            }
            return new ProfileLikeFragment(userId);
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
