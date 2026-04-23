package com.example.masterai.ui.comminity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
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
        String wsUrl = "ws://172.11.218.57:8001/ws/notifications/" + userId + "/";

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
                try {
                    JSONObject json = new JSONObject(text);
                    // Backend thường gửi: { "type": "send_notification", "data": { ... } }
                    JSONObject data = json.getJSONObject("data");

                    Notification notification = new Notification();
                    notification.setId(data.optString("id"));

                    // Logic gộp content
                    String message = data.optString("message");
                    String title = data.optString("title");
                    notification.setContent(title + ": " + message);

                    // 🔥 Lấy Object Sender từ JSON lồng nhau
                    if (data.has("sender") && !data.isNull("sender")) {
                        JSONObject senderJson = data.getJSONObject("sender");
                        Notification.Sender senderObj = new Notification.Sender();
                        senderObj.setUsername(senderJson.optString("username"));
                        senderObj.setAvatar(senderJson.optString("avatar"));
                        notification.setSender(senderObj);
                    }

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

            // 1. Xử lý nội dung in đậm tên người gửi (chuẩn Facebook)
            String displayName = (item.getSenderName() != null) ? item.getSenderName() : "Hệ thống";
            String message = " " + item.getMessage();

            SpannableString spannable = new SpannableString(displayName + message);
            spannable.setSpan(
                    new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    0, displayName.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            holder.tvContent.setText(spannable);

            // 2. Xử lý thời gian (Sử dụng hàm format bên dưới)
            holder.tvTime.setText(formatTimeAgo(item.getCreatedAt()));

            // 3. Xử lý trạng thái Đã đọc / Chưa đọc
            if (item.isRead()) {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT); // Đã đọc thì nền trắng/trong suốt
                holder.vDot.setVisibility(View.GONE);
            } else {
                holder.itemView.setBackgroundColor(Color.parseColor("#E7F3FF")); // Chưa đọc thì nền xanh nhạt
                holder.vDot.setVisibility(View.VISIBLE);
            }

            // 4. Load Avatar (Giữ nguyên logic Glide của bạn nhưng thêm placeholder tốt hơn)
            Glide.with(holder.ivAvatar.getContext())
                    .load(item.getSenderAvatar())
                    .circleCrop()
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .into(holder.ivAvatar);

            // 5. Click xử lý
            holder.itemView.setOnClickListener(v -> {
                // Lấy context trực tiếp từ view bị click (v)
                Context context = v.getContext();

                // Truyền context vào hàm xử lý click
                handleNotificationClick(context, item);

                if (!item.isRead()) {
                    markAsRead(item, position);
                }
            });
        }

        private String formatTimeAgo(String dateStr) {
            try {
                // Format ISO 8601 từ Django trả về
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                long time = sdf.parse(dateStr).getTime();
                long now = System.currentTimeMillis();

                return android.text.format.DateUtils.getRelativeTimeSpanString(time, now, android.text.format.DateUtils.MINUTE_IN_MILLIS).toString();
            } catch (Exception e) {
                return dateStr; // Trả về mặc định nếu lỗi
            }
        }

        private void handleNotificationClick(Context context, Notification item) {
            String type = item.getType();

            if ("comment".equals(type) || "like".equals(type)) {
                // Ví dụ điều hướng
                 Intent intent = new Intent(context, PostDetailActivity.class);
                 context.startActivity(intent);
            }
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
