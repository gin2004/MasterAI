package com.example.masterai.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.databinding.ActivityChatBinding;
import com.example.masterai.model.Message;
import com.example.masterai.model.StatusRequest;
import com.example.masterai.utils.ChatWebSocketClient;
import com.example.masterai.utils.UserManager;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private ActivityChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);


        // Lấy ID người dùng
        if (UserManager.getInstance(this).getUser() != null) {
            myId = UserManager.getInstance(this).getUser().getId();
        }
        targetId = getIntent().getStringExtra("target_user_id");

        initViews();
        handleIntent();
        setupRecyclerView();
        loadChatHistory();
        setupWebSocket();
        setupClickListeners();
    }

    private void setupWebSocket() {
        webSocketClient = new ChatWebSocketClient(new ChatWebSocketClient.ChatMessageListener() {
            @Override
            public void onMessageReceived(String message, String senderId, String timestamp) {
                runOnUiThread(() -> {
                    // Nếu senderId trùng với myId, nghĩa là đây là tin nhắn của chính mình
                    if (senderId != null && senderId.equals(myId)) {
                        return;
                    }
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

            @Override
            public void onPresenceReceived(String userId, boolean isOnline) {
                runOnUiThread(() -> {
                    // Cập nhật lại UI nếu cái ID online chính là người mình đang chat
                    if (userId != null && userId.equals(targetId)) {
                        tvChatStatus.setText(isOnline ? "Online" : "Offline");
                        tvChatStatus.setTextColor(getResources().getColor(
                                isOnline ? android.R.color.holo_green_light : android.R.color.darker_gray
                        ));
                    }
                });
            }
        });

        // Bắt đầu kết nối
        if (myId != null && targetId != null) {
            webSocketClient.connect(myId, targetId);
        }
    }

    private void initViews() {
        btnBack = binding.btnBack;
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

    private void loadChatHistory() {
        RetrofitClient.getApiService().getChatHistory(myId, targetId)
                .enqueue(new Callback<List<Message>>() {
                    @Override
                    public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            messageList.clear();
                            messageList.addAll(response.body());

                            // Cập nhật isSentByMe để Adapter biết vẽ bong bóng bên trái hay phải
                            for (Message msg : messageList) {
                                msg.setSentByMe(msg.getSenderId().equals(myId));
                            }

                            messageAdapter.notifyDataSetChanged();

                            // Cuộn xuống tin nhắn cuối cùng
                            if (messageList.size() > 0) {
                                rvChat.scrollToPosition(messageList.size() - 1);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Message>> call, Throwable t) {
                        Toast.makeText(ChatActivity.this, "Lỗi tải tin nhắn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleIntent() {
        String username = getIntent().getStringExtra("username");
        String image_url = getIntent().getStringExtra("image_url");
        boolean isOnline = getIntent().getBooleanExtra("isOnline", false);

        if (username != null) {
            tvChatUsername.setText(username);
        }
        
        tvChatStatus.setText(isOnline ? "Online" : "Offline");
        tvChatStatus.setTextColor(getResources().getColor(isOnline ? android.R.color.holo_green_light : android.R.color.darker_gray));
        Glide.with(this).load(image_url).placeholder(R.drawable.ic_user).circleCrop().into(binding.ivAvatar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Báo cho Server biết tôi đang Online
        if (myId != null) {
            StatusRequest request = new StatusRequest(myId, true);
            RetrofitClient.getApiService().updateOnlineStatus(request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {}
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {}
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Báo cho Server biết tôi đã ẩn App (Offline)
        if (myId != null) {
            StatusRequest request = new StatusRequest(myId, false);
            RetrofitClient.getApiService().updateOnlineStatus(request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {}
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {}
            });
        }
    }
}
