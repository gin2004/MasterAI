package com.example.masterai.ui.comminity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.example.masterai.MainActivity;
import com.example.masterai.R;

public class ChatDetailFragment extends Fragment {

    private static final String ARG_USERNAME = "username";
    private static final String ARG_IS_ONLINE = "is_online";

    private String username;
    private boolean isOnline;

    public static ChatDetailFragment newInstance(String username, boolean isOnline) {
        ChatDetailFragment fragment = new ChatDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        args.putBoolean(ARG_IS_ONLINE, isOnline);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(ARG_USERNAME);
            isOnline = getArguments().getBoolean(ARG_IS_ONLINE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_detail, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        TextView tvChatUsername = view.findViewById(R.id.tvChatUsername);
        TextView tvChatStatus = view.findViewById(R.id.tvChatStatus);

        tvChatUsername.setText(username);
        tvChatStatus.setText(isOnline ? "Online" : "Offline");
        tvChatStatus.setTextColor(getResources().getColor(isOnline ? android.R.color.holo_green_light : android.R.color.darker_gray));

        // Ẩn Bottom Navigation khi vào màn hình chat
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hiện lại Bottom Navigation khi thoát màn hình chat
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavVisibility(View.VISIBLE);
        }
    }
}