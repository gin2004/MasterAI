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

public class MainActivity extends AppCompatActivity {

    private View bottomNav;

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

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new CommunityFragment());
        }

        // Setup Bottom Navigation listeners
        findViewById(R.id.btnCommunity).setOnClickListener(v -> loadFragment(new CommunityFragment()));
        findViewById(R.id.btnGenerate).setOnClickListener(v -> loadFragment(new GenerateFragment()));
        findViewById(R.id.btnProfile).setOnClickListener(v -> loadFragment(new ProfileFragment()));
        
        // Bắt sự kiện cho btnPost và btnMessage
        View btnPost = findViewById(R.id.btnPost);
        if (btnPost != null) {
            btnPost.setOnClickListener(v -> loadFragment(new PostFragment()));
        }
        
        View btnMessage = findViewById(R.id.btnMessage);
        if (btnMessage != null) {
            btnMessage.setOnClickListener(v -> loadFragment(new MessageFragment()));
        }
    }

    public void setBottomNavVisibility(int visibility) {
        if (bottomNav != null) {
            bottomNav.setVisibility(visibility);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}