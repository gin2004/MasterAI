package com.example.masterai.ui.ai;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.masterai.R;
import com.example.masterai.model.Asset;
import com.example.masterai.model.Generation;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class GenerateFragment extends Fragment {

    private MaterialButton btnTabImage, btnTabAvatar, btnTabVoice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generate, container, false);

        initViews(view);
        setupTabs();


        return view;
    }

    private void initViews(View view) {


        btnTabImage = view.findViewById(R.id.btnTabImage);
        btnTabAvatar = view.findViewById(R.id.btnTabAvatar);
        btnTabVoice = view.findViewById(R.id.btnTabVoice);


    }

    private void setupTabs() {
        loadFragment(new ImageFragment());
        btnTabImage.setOnClickListener(v -> {
            resetTabs();
            setSelected(btnTabImage);
            loadFragment(new ImageFragment());
        });

        btnTabAvatar.setOnClickListener(v -> {
            resetTabs();
            setSelected(btnTabAvatar);
            loadFragment(new AvatarFragment());
        });

        btnTabVoice.setOnClickListener(v -> {
            resetTabs();
            setSelected(btnTabVoice);
            loadFragment(new VoiceFragment());
        });
    }

    private void resetTabs() {
        MaterialButton[] tabs = {btnTabImage, btnTabAvatar, btnTabVoice};
        for (MaterialButton btn : tabs) {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
            btn.setTextColor(Color.parseColor("#757575"));
            btn.setIconTint(ColorStateList.valueOf(Color.parseColor("#757575")));
        }
    }


    private void loadFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
    private void setSelected(MaterialButton btn) {
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#A066FF")));
        btn.setTextColor(Color.WHITE);
        btn.setIconTint(ColorStateList.valueOf(Color.WHITE));
    }
}