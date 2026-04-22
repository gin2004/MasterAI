package com.example.masterai.ui.ai;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.masterai.R;
import com.google.android.material.button.MaterialButton;

public class GenerateFragment extends Fragment {

    private MaterialButton btnTabImage, btnTabAvatar, btnTabVoice;
    private Fragment imageFragment, avatarFragment, voiceFragment;
    private Fragment activeFragment;
    private FragmentManager fragmentManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generate, container, false);

        initViews(view);
        fragmentManager = getChildFragmentManager();
        setupTabs();

        return view;
    }

    private void initViews(View view) {
        btnTabImage = view.findViewById(R.id.btnTabImage);
        btnTabAvatar = view.findViewById(R.id.btnTabAvatar);
        btnTabVoice = view.findViewById(R.id.btnTabVoice);
    }

    private void setupTabs() {
        // Tìm lại các fragment cũ nếu FragmentManager đã giữ chúng
        imageFragment = fragmentManager.findFragmentByTag("1");
        avatarFragment = fragmentManager.findFragmentByTag("2");
        voiceFragment = fragmentManager.findFragmentByTag("3");

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (imageFragment == null) {
            imageFragment = new ImageFragment();
            transaction.add(R.id.fragmentContainer, imageFragment, "1");
        }
        if (avatarFragment == null) {
            avatarFragment = new AvatarFragment();
            transaction.add(R.id.fragmentContainer, avatarFragment, "2").hide(avatarFragment);
        }
        if (voiceFragment == null) {
            voiceFragment = new VoiceFragment();
            transaction.add(R.id.fragmentContainer, voiceFragment, "3").hide(voiceFragment);
        }
        
        transaction.commit();

        // Mặc định tab Image
        activeFragment = imageFragment;
        resetTabs();
        setSelected(btnTabImage);

        btnTabImage.setOnClickListener(v -> {
            resetTabs();
            setSelected(btnTabImage);
            switchFragment(imageFragment);
        });

        btnTabAvatar.setOnClickListener(v -> {
            resetTabs();
            setSelected(btnTabAvatar);
            switchFragment(avatarFragment);
        });

        btnTabVoice.setOnClickListener(v -> {
            resetTabs();
            setSelected(btnTabVoice);
            switchFragment(voiceFragment);
        });
    }

    private void switchFragment(Fragment fragment) {
        if (activeFragment != fragment && fragment != null) {
            fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit();
            activeFragment = fragment;
        }
    }

    private void resetTabs() {
        MaterialButton[] tabs = {btnTabImage, btnTabAvatar, btnTabVoice};
        for (MaterialButton btn : tabs) {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
            btn.setTextColor(Color.parseColor("#757575"));
            btn.setIconTint(ColorStateList.valueOf(Color.parseColor("#757575")));
        }
    }

    private void setSelected(MaterialButton btn) {
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00BFFF")));
        btn.setTextColor(Color.WHITE);
        btn.setIconTint(ColorStateList.valueOf(Color.WHITE));
    }
}