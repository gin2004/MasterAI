package com.example.masterai;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.masterai.api.RetrofitClient;
import com.example.masterai.model.User;
import com.example.masterai.ui.ai.GenerateFragment;
import com.example.masterai.ui.comminity.CommunityFragment;
import com.example.masterai.ui.comminity.MessageFragment;
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


        initView();
        bottomNav = findViewById(R.id.bottomNav);

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
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

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