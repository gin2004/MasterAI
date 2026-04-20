package com.example.masterai;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.model.User;
import com.example.masterai.ui.ai.GenerateFragment;
import com.example.masterai.ui.comminity.CommunityFragment;
import com.example.masterai.ui.chat.MessageFragment;
import com.example.masterai.ui.comminity.PostFragment;
import com.example.masterai.ui.profile.ProfileFragment;
import com.example.masterai.utils.UserManager;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        initView();

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        if (savedInstanceState == null) {
            loadFragment(new CommunityFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            // Xử lý hiệu ứng phóng to icon
            updateBottomNavScale(itemId);

            if (itemId == R.id.btnCommunity) {
                loadFragment(new CommunityFragment());
                return true;
            } else if (itemId == R.id.btnGenerate) {
                loadFragment(new GenerateFragment());
                return true;
            } else if (itemId == R.id.btnPost) {
                loadFragment(new PostFragment());
                return true;
            } else if (itemId == R.id.btnMessage) {
                loadFragment(new MessageFragment());
                return true;
            } else if (itemId == R.id.btnProfile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        // Kích hoạt hiệu ứng mặc định cho item đầu tiên
        bottomNav.post(() -> updateBottomNavScale(R.id.btnCommunity));

    }

    private void initView() {
        currentUser = UserManager.getInstance(this).getUser();
        loadUser();

    }

    private void loadUser() {
        RetrofitClient.getApiService().getUserById(currentUser.getId(),null).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful() && response.body() != null){
                    User user = response.body();
                    UserManager.getInstance(MainActivity.this).setUser(user);
                    currentUser = user;
                    setUpBottomNav();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void setUpBottomNav() {
        if (currentUser == null || currentUser.getAvatarUrl() == null || bottomNav == null) return;

        bottomNav.post(() -> {
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNav.getChildAt(0);
            BottomNavigationItemView itemView = menuView.findViewById(R.id.btnProfile);

            if (itemView != null) {
                // Tắt tint để hiển thị màu thật của ảnh đại diện
                itemView.setIconTintList(null);

                Glide.with(this)
                        .asBitmap()
                        .load(currentUser.getAvatarUrl())
                        .placeholder(R.drawable.ic_nav_profile)
                        .circleCrop()
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                MenuItem item = bottomNav.getMenu().findItem(R.id.btnProfile);
                                if (item != null) {
                                    item.setIcon(new BitmapDrawable(getResources(), resource));
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                MenuItem item = bottomNav.getMenu().findItem(R.id.btnProfile);
                                if (item != null) item.setIcon(placeholder);
                            }
                        });
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void updateBottomNavScale(int selectedItemId) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNav.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            View itemView = menuView.getChildAt(i);
            if (itemView instanceof BottomNavigationItemView) {
                BottomNavigationItemView item = (BottomNavigationItemView) itemView;
                if (item.getId() == selectedItemId) {
                    item.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start();
                } else {
                    item.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                }
            }
        }
    }

    public void setBottomNavVisibility(int visibility) {
        if (bottomNav != null) {
            bottomNav.setVisibility(visibility);
        }
    }

    public void navigateToCommunity() {
        bottomNav.setSelectedItemId(R.id.btnCommunity);
    }

    public void navigateToProfile() {
        bottomNav.setSelectedItemId(R.id.btnProfile);
    }


    public void hideBottomNav() {
        bottomNav.animate().translationY(bottomNav.getHeight()+100).setDuration(100);
    }

    public void showBottomNav() {
        bottomNav.animate().translationY(0).setDuration(100);
    }
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
