package com.example.masterai.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.utils.UserManager;
import com.example.masterai.model.User;

public class ProfileFragment extends Fragment {

    private ImageView ivProfileAvatar;
    private TextView tvProfileUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);

        // Lấy thông tin user hiện tại từ UserManager
        User currentUser = UserManager.getInstance().getUser();
        loadUserData(currentUser);

        return view;
    }

    public void loadUserData(User user) {
        if (user != null) {
            tvProfileUsername.setText(user.getUsername());
            
            // Load ảnh bằng Glide
            if (getContext() != null && user.getAvatarUrl() != null) {
                Glide.with(this)
                    .load(user.getAvatarUrl()) // URL từ backend
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .error(android.R.drawable.stat_notify_error)
                    .circleCrop()
                    .into(ivProfileAvatar);
            }
        }
    }
}
