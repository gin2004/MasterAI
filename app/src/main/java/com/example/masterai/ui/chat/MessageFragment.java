package com.example.masterai.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.masterai.R;
import com.example.masterai.model.UserMessage;
import com.example.masterai.ui.adapter.UserMessageAdapter;

import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends Fragment {
    
    private RecyclerView rvMessages;
    private UserMessageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        
        rvMessages = view.findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        
        List<UserMessage> messages = new ArrayList<>();
        
        // Tạo dữ liệu mẫu bằng UserMessage
        UserMessage m1 = new UserMessage();
        m1.setTargetUserName("AI_Artist_01");
        m1.setLastMessage("Hey, did you see the new cyberpunk city?");
        m1.setLastMessageTimestamp("2023-10-27T10:30:00.000000Z");
        m1.setOnline(true);
        m1.setUnreadCount(2);
        messages.add(m1);

        UserMessage m2 = new UserMessage();
        m2.setTargetUserName("Creative_Mind");
        m2.setLastMessage("The portrait turned out great!");
        m2.setLastMessageTimestamp("2023-10-27T09:15:00.000000Z");
        m2.setOnline(false);
        messages.add(m2);

        UserMessage m3 = new UserMessage();
        m3.setTargetUserName("AnimeFan_AI");
        m3.setLastMessage("I love the colors in your latest work.");
        m3.setLastMessageTimestamp("2023-10-26T20:00:00.000000Z");
        m3.setOnline(true);
        messages.add(m3);
        
        adapter = new UserMessageAdapter(getContext(), messages, message -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("username", message.getTargetUserName());
            intent.putExtra("target_user_id", message.getTargetUserId());
            intent.putExtra("isOnline", message.isOnline());
            startActivity(intent);
        });
        rvMessages.setAdapter(adapter);
        
        return view;
    }
}
