package com.example.masterai.ui.profile;

import android.os.Bundle;
import android.util.Log;
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
import com.example.masterai.databinding.FragmentProfileBinding;
import com.example.masterai.utils.UserManager;
import com.example.masterai.model.User;

public class ProfileFragment extends Fragment {

    private User currentUser;
    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        // Lấy thông tin user hiện tại từ UserManager
        currentUser = UserManager.getInstance(requireContext()).getUser();
        Log.d("ProfileFragment", "Current User: " + currentUser);
        initView();

        return binding.getRoot();
    }

    private void initView() {
        binding.setUserProfile(currentUser);
        //set avatar
        if (currentUser.getAvatarUrl() != null) {
            Glide.with(requireContext())
                    .load(currentUser.getAvatarUrl())
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .error(android.R.drawable.stat_notify_error)
                    .circleCrop()
                    .into(binding.ivProfileAvatar);
        }
    }
}
