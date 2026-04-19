package com.example.masterai.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.masterai.R;
import com.example.masterai.model.Message;
import com.example.masterai.utils.ChatWebSocketClient;
import com.example.masterai.utils.UserManager;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvChatUsername, tvChatStatus;
    private RecyclerView rvChat;
    private EditText edtMessage;
    private ImageButton btnSend;
    private String myId;
    private String targetId; // ID của người bạn đang chat cùng
    private ChatWebSocketClient webSocketClient;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Lấy ID người dùng
        if (UserManager.getInstance(this).getUser() != null) {
            myId = UserManager.getInstance(this).getUser().getId();
        }
        targetId = getIntent().getStringExtra("target_user_id");

        initViews();
        handleIntent();
        setupRecyclerView();
        loadMockMessages();
        setupWebSocket();
        setupClickListeners();
    }

    private void setupWebSocket() {
        webSocketClient = new ChatWebSocketClient(new ChatWebSocketClient.ChatMessageListener() {
            @Override
            public void onMessageReceived(String message, String senderId, String timestamp) {
                runOnUiThread(() -> {
                    // Thêm tin nhắn mới vào danh sách khi nhận được từ server
                    Message newMessage = new Message(senderId, message, null, Message.TYPE_TEXT, false);
                    messageList.add(newMessage);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    rvChat.scrollToPosition(messageList.size() - 1);
                });
            }

            @Override
            public void onConnectionClosed() {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Đã ngắt kết nối", Toast.LENGTH_SHORT).show();
                });
            }
        });

        // Bắt đầu kết nối
        if (myId != null && targetId != null) {
            webSocketClient.connect(myId, targetId);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvChatUsername = findViewById(R.id.tvChatUsername);
        tvChatStatus = findViewById(R.id.tvChatStatus);
        rvChat = findViewById(R.id.rvChat);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = edtMessage.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            if (webSocketClient != null) {
                webSocketClient.sendMessage(text);
            }

            Message message = new Message(myId, text, null, Message.TYPE_TEXT, true);
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            rvChat.scrollToPosition(messageList.size() - 1);

            edtMessage.setText("");
        }
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        String targetAvatarUrl = "https://i.pravatar.cc/150?u=" + targetId;
        messageAdapter = new MessageAdapter(this, messageList, targetAvatarUrl);
        
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(messageAdapter);
    }

    private void loadMockMessages() {
        messageList.add(new Message(myId, "Chào bạn!", null, Message.TYPE_TEXT, true));
        messageList.add(new Message(targetId, "Chào! Rất vui được gặp bạn.", null, Message.TYPE_TEXT, false));
        
        messageAdapter.notifyDataSetChanged();
        rvChat.scrollToPosition(messageList.size() - 1);
    }

    private void handleIntent() {
        String username = getIntent().getStringExtra("username");
        boolean isOnline = getIntent().getBooleanExtra("isOnline", false);

        if (username != null) {
            tvChatUsername.setText(username);
        }
        
        tvChatStatus.setText(isOnline ? "Online" : "Offline");
        tvChatStatus.setTextColor(getResources().getColor(isOnline ? android.R.color.holo_green_light : android.R.color.darker_gray));
    }
}
