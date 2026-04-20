package com.example.masterai.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.bumptech.glide.Glide;
import com.example.masterai.databinding.FragmentProfileBinding;
import com.example.masterai.utils.UserManager;
import com.example.masterai.model.User;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProfileFragment extends Fragment {

    private static User currentUser;
    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        currentUser = UserManager.getInstance(requireContext()).getUser();
        
        initView();
        setupViewPager();
        
        return binding.getRoot();
    }

    private void initView() {
        if (currentUser != null) {
            binding.setUserProfile(currentUser);
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

    private void setupViewPager() {
        ProfilePagerAdapter adapter = new ProfilePagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        int []icon = {android.R.drawable.ic_dialog_dialer, android.R.drawable.gallery_thumb};
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) ->
                tab.setIcon(icon[position])
        ).attach();
    }

    private static class ProfilePagerAdapter extends FragmentStateAdapter {
        public ProfilePagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return ProfileUserPostFragment.newInstance(currentUser.getId());
            }
            return ProfileLikeFragment.newInstance(currentUser.getId());
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
