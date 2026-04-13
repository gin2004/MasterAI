package com.example.masterai.ui.comminity;

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
import com.example.masterai.model.Message;
import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends Fragment {
    
    private RecyclerView rvMessages;
    private MessageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        
        rvMessages = view.findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("AI_Artist_01", "Hey, did you see the new cyberpunk city?", "10:30 AM", "", true));
        messages.add(new Message("Creative_Mind", "The portrait turned out great!", "9:15 AM", "", false));
        messages.add(new Message("AnimeFan_AI", "I love the colors in your latest work.", "Yesterday", "", true));
        messages.add(new Message("Master_Bot", "Welcome to MasterAI! How can I help you?", "Monday", "", true));
        messages.add(new Message("Arch_Future", "Let's collaborate on the next project.", "Oct 20", "", false));
        
        adapter = new MessageAdapter(messages, message -> {
            // Chuyển sang màn hình chi tiết chat
            ChatDetailFragment detailFragment = ChatDetailFragment.newInstance(
                    message.getUsername(), 
                    message.isOnline()
            );
            
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvMessages.setAdapter(adapter);
        
        return view;
    }
}