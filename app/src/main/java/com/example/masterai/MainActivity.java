package com.example.masterai;

import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.masterai.ui.ai.GenerateFragment;
import com.example.masterai.ui.comminity.CommunityFragment;
import com.example.masterai.ui.comminity.MessageFragment;
import com.example.masterai.ui.comminity.PostFragment;
import com.example.masterai.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
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

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}