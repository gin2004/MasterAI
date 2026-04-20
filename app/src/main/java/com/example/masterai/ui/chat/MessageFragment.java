package com.example.masterai.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.model.UserMessage;
import com.example.masterai.ui.adapter.UserMessageAdapter;
import com.example.masterai.utils.UserManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageFragment extends Fragment {
    
    private RecyclerView rvMessages;
    private UserMessageAdapter adapter;
    private List<UserMessage> userMessages = new ArrayList<>();
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        
        rvMessages = view.findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        
        if (UserManager.getInstance(getContext()).getUser() != null) {
            currentUserId = UserManager.getInstance(getContext()).getUser().getId();
        }

        adapter = new UserMessageAdapter(getContext(), userMessages, message -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("username", message.getTargetUserName());
            intent.putExtra("target_user_id", message.getTargetUserId());
            intent.putExtra("isOnline", message.isOnline());
            intent.putExtra("image_url", message.getTargetAvatarUrl());
            startActivity(intent);
        });
        rvMessages.setAdapter(adapter);
        
        loadInbox();
        
        return view;
    }

    private void loadInbox() {
        if (currentUserId == null) return;

        RetrofitClient.getApiService().getInbox(currentUserId).enqueue(new Callback<List<UserMessage>>() {
            @Override
            public void onResponse(Call<List<UserMessage>> call, Response<List<UserMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userMessages.clear();
                    userMessages.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<UserMessage>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải danh sách tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadInbox(); // Tải lại inbox khi quay lại fragment
    }
}
