package com.example.masterai.utils;

import android.util.Log;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatWebSocketClient {
    private WebSocket webSocket;
    private final String SERVER_URL = "ws://192.168.99.102:8000/ws/chat/";
    private ChatMessageListener listener;

    // Interface để truyền dữ liệu về Activity/Fragment
    public interface ChatMessageListener {
        void onMessageReceived(String message, String senderId, String timestamp);
        void onConnectionClosed();
        void onPresenceReceived(String userId, boolean isOnline);
    }
    public ChatWebSocketClient(ChatMessageListener listener) {
        this.listener = listener;
    }

    // 1. Mở kết nối
    public void connect(String myId, String targetId) {
        OkHttpClient client = new OkHttpClient();

        //URL chuẩn với cấu trúc backend: ws://.../ws/chat/my_id/target_id/
        String url = SERVER_URL + myId + "/" + targetId + "/";
        Request request = new Request.Builder().url(url).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("WebSocket", "Kết nối thành công!");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WebSocket", "Nhận được: " + text);
                try {
                    JSONObject json = new JSONObject(text);
                    String type = json.getString("type");
                    if (type.equals("chat")) {
                        String message = json.optString("message", "");
                        String senderId = json.getString("sender_id");
                        String timestamp = json.getString("timestamp");
                        
                        // Nếu có imageUrl trong JSON nhận được từ server
                        // Bạn có thể mở rộng interface listener nếu cần xử lý riêng ảnh
                        if (listener != null) {
                            listener.onMessageReceived(message, senderId, timestamp);
                        }
                    } else if (type.equals("presence")) {
                        boolean isOnline = json.getBoolean("is_online");
                        String userId = json.getString("user_id");

                        if (listener != null) {
                            listener.onPresenceReceived(userId, isOnline);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("WebSocket", "Đã đóng kết nối");
                if (listener != null) listener.onConnectionClosed();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WebSocket", "Lỗi: " + t.getMessage());
            }
        });
    }
    
    // Gửi tin nhắn văn bản
    public void sendMessage(String message) {
        if (webSocket != null) {
            try {
                JSONObject json = new JSONObject();
                json.put("message", message);
                webSocket.send(json.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Gửi tin nhắn ảnh (Gửi URL ảnh)
    public void sendImage(String imageUrl) {
        if (webSocket != null) {
            try {
                JSONObject json = new JSONObject();
                json.put("message", "[Hình ảnh]");
                json.put("image_url", imageUrl);
                webSocket.send(json.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 3. Đóng kết nối khi thoát màn hình
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "User exited chat");
            webSocket = null;
        }
    }
}
