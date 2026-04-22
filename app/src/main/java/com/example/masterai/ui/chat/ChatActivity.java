package com.example.masterai.ui.chat;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.databinding.ActivityChatBinding;
import com.example.masterai.model.ImageResponse;
import com.example.masterai.model.Message;
import com.example.masterai.model.StatusRequest;
import com.example.masterai.utils.ChatWebSocketClient;
import com.example.masterai.utils.UserManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private ImageButton btnBack, btnPickImage;
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

    // Launcher để chọn ảnh từ bộ nhớ
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);

        // Khởi tạo ActivityResultLauncher
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                // Xử lý ảnh sau khi chọn
                uploadAndSendImage(uri);
            }
        });

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
                    if (senderId != null && senderId.equals(myId)) {
                        return;
                    }
                    Message newMessage = new Message(senderId, message, null, Message.TYPE_TEXT, false);
                    messageList.add(newMessage);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    rvChat.scrollToPosition(messageList.size() - 1);
                });
            }

            @Override
            public void onConnectionClosed() {
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Đã ngắt kết nối", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onPresenceReceived(String userId, boolean isOnline) {
                runOnUiThread(() -> {
                    if (userId != null && userId.equals(targetId)) {
                        tvChatStatus.setText(isOnline ? "Online" : "Offline");
                        tvChatStatus.setTextColor(getResources().getColor(
                                isOnline ? android.R.color.holo_green_light : android.R.color.darker_gray
                        ));
                    }
                });
            }
        });

        if (myId != null && targetId != null) {
            webSocketClient.connect(myId, targetId);
        }
    }

    private void initViews() {
        btnBack = binding.btnBack;
        btnPickImage = binding.btnPickImage;
        tvChatUsername = findViewById(R.id.tvChatUsername);
        tvChatStatus = findViewById(R.id.tvChatStatus);
        rvChat = findViewById(R.id.rvChat);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());
        btnPickImage.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
        
        // Thêm listener để cuộn xuống khi focus vào EditText
        edtMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollToBottom();
            }
        });
    }

    private void uploadAndSendImage(Uri uri) {
        // 1. Hiển thị ảnh tạm thời lên UI (Local Uri)
        Message tempMessage = new Message(myId, "[Đang gửi ảnh...]", uri.toString(), Message.TYPE_IMAGE, true);
        messageList.add(tempMessage);
        int position = messageList.size() - 1;
        messageAdapter.notifyItemInserted(position);
        scrollToBottom();

        // 2. Chuyển Uri thành File để upload
        File file = getFileFromUri(uri);
        if (file == null) {
            Toast.makeText(this, "Không thể mở tệp ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        // 3. Gọi API Upload
        RetrofitClient.getApiService().uploadChatImage(body).enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
//                    String serverImageUrl = response.body().getImageUrl();
                    String serverImageUrl = "https://i.pravatar.cc/150?u=" + targetId;
                    // 4. Gửi URL ảnh qua WebSocket
                    if (webSocketClient != null) {
                        webSocketClient.sendImage(serverImageUrl);
                    }
                    
                    // Cập nhật lại tin nhắn tạm thời thành URL server (nếu cần)
                    tempMessage.setText("[Hình ảnh]");
                    tempMessage.setImageUrl(serverImageUrl);
                    messageAdapter.notifyItemChanged(position);
                } else {
                    Toast.makeText(ChatActivity.this, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                Log.e("Upload", "Error: " + t.getMessage());
                Toast.makeText(ChatActivity.this, "Lỗi kết nối khi upload", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method để lấy File từ Uri (Xử lý được cả Uri từ MediaStore)
    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.close();
            inputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
            scrollToBottom();

            edtMessage.setText("");
        }
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        String targetAvatarUrl = "https://i.pravatar.cc/150?u=" + targetId;
        messageAdapter = new MessageAdapter(this, messageList, targetAvatarUrl);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Luôn bắt đầu từ cuối danh sách
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(messageAdapter);

        // Tự động cuộn xuống khi bàn phím hiện lên làm thay đổi kích thước RecyclerView
        rvChat.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, 
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) { // Kích thước bị thu hẹp (bàn phím hiện)
                    scrollToBottom();
                }
            }
        });
    }

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            rvChat.postDelayed(() -> rvChat.smoothScrollToPosition(messageAdapter.getItemCount() - 1), 100);
        }
    }

    private void loadChatHistory() {
        RetrofitClient.getApiService().getChatHistory(myId, targetId)
                .enqueue(new Callback<List<Message>>() {
                    @Override
                    public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            messageList.clear();
                            messageList.addAll(response.body());

                            for (Message msg : messageList) {
                                msg.setSentByMe(msg.getSenderId().equals(myId));
                            }

                            messageAdapter.notifyDataSetChanged();

                            if (messageList.size() > 0) {
                                scrollToBottom();
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
