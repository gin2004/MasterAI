package com.example.masterai.ui.comminity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.model.Notification;
import com.example.masterai.model.User;
import com.example.masterai.utils.UserManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;

public class NotificationFragment extends Fragment {

    private static final String TAG = "NotificationFragment";

    private RecyclerView rvNotifications;
    private SwipeRefreshLayout swipeRefresh;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private User currentUser;

    // 🔥 WebSocket
    private WebSocket webSocket;
    private OkHttpClient client;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        currentUser = UserManager.getInstance(requireContext()).getUser();

        rvNotifications = view.findViewById(R.id.rvNotifications);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::fetchNotifications);
        }

        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NotificationAdapter(notificationList);
        rvNotifications.setAdapter(adapter);

        fetchNotifications();
        connectWebSocket(); // 🔥 realtime

        return view;
    }

    // =========================
    // 🔥 CONNECT WEBSOCKET
    // =========================
    private void connectWebSocket() {
        if (currentUser == null) return;

        String userId = currentUser.getId();

        // ⚠️ ĐỔI IP CHO ĐÚNG MÁY BẠN
        String wsUrl = "ws://192.168.1.10:8001/ws/notifications/" + userId + "/";

        client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(wsUrl)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                Log.d(TAG, "WebSocket connected");
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                Log.d(TAG, "Realtime notification raw: " + text);

                try {
                    JSONObject json = new JSONObject(text);
                    
                    // Lấy dữ liệu từ payload nếu có (một số backend lồng trong 'payload' hoặc 'data')
                    JSONObject data = json.has("notification") ? json.getJSONObject("notification") : json;

                    Notification notification = new Notification();
                    notification.setId(data.optString("id"));
                    
                    // Ưu tiên lấy field 'content', sau đó mới đến 'message' hoặc 'title'
                    String content = data.optString("content");
                    if (content.isEmpty()) {
                        String message = data.optString("message");
                        String title = data.optString("title");
                        if (!title.isEmpty() && !message.isEmpty()) {
                            content = title + ": " + message;
                        } else if (!message.isEmpty()) {
                            content = message;
                        } else {
                            content = title;
                        }
                    }
                    
                    notification.setContent(content);
                    notification.setCreatedAt(data.optString("created_at", "Vừa xong"));
                    notification.setSenderName(data.optString("sender_name"));
                    notification.setSenderAvatar(data.optString("sender_avatar"));
                    notification.setRead(false);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            notificationList.add(0, notification);
                            adapter.notifyItemInserted(0);
                            rvNotifications.scrollToPosition(0);
                        });
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Parse error", e);
                }
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, okhttp3.Response response) {
                Log.e(TAG, "WebSocket error", t);
            }
        });
    }

    // =========================
    // 📄 FETCH API
    // =========================
    private void fetchNotifications() {
        if (currentUser == null) {
            Log.e(TAG, "fetchNotifications: currentUser is null");
            return;
        }

        if (swipeRefresh != null) swipeRefresh.setRefreshing(true);

        RetrofitClient.getApiService().getNotifications(currentUser.getId()).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(@NonNull Call<List<Notification>> call, @NonNull retrofit2.Response<List<Notification>> response) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    notificationList.clear();
                    notificationList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Lỗi tải thông báo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Notification>> call, @NonNull Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                Log.e(TAG, "fetch error", t);
            }
        });
    }

    // =========================
    // ✅ MARK AS READ
    // =========================
    private void markAsRead(Notification notification, int position) {
        RetrofitClient.getApiService().markAsRead(notification.getId()).enqueue(new Callback<Notification>() {
            @Override
            public void onResponse(@NonNull Call<Notification> call, @NonNull retrofit2.Response<Notification> response) {
                if (response.isSuccessful()) {
                    notification.setRead(true);
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Notification> call, @NonNull Throwable t) {
                Log.e(TAG, "markAsRead error", t);
            }
        });
    }

    // =========================
    // 🔁 ADAPTER
    // =========================
    class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

        List<Notification> items;

        NotificationAdapter(List<Notification> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Notification item = items.get(position);

            holder.tvContent.setText(item.getContent());
            holder.tvTime.setText(item.getCreatedAt());
            holder.vDot.setVisibility(item.isRead() ? View.GONE : View.VISIBLE);

            if (item.getSenderAvatar() != null && !item.getSenderAvatar().isEmpty()) {
                Glide.with(holder.ivAvatar.getContext())
                        .load(item.getSenderAvatar())
                        .circleCrop()
                        .into(holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_nav_profile);
            }

            holder.itemView.setOnClickListener(v -> {
                if (!item.isRead()) {
                    markAsRead(item, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvContent, tvTime;
            View vDot;
            ImageView ivAvatar;

            ViewHolder(View v) {
                super(v);
                tvContent = v.findViewById(R.id.tvNotifContent);
                tvTime = v.findViewById(R.id.tvNotifTime);
                vDot = v.findViewById(R.id.vNotifDot);
                ivAvatar = v.findViewById(R.id.ivNotifAvatar);
            }
        }
    }

    // =========================
    // ❌ CLOSE SOCKET
    // =========================
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocket != null) {
            webSocket.close(1000, "Closed");
        }
    }
}
