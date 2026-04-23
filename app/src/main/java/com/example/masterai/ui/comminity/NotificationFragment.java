package com.example.masterai.ui.comminity;

import android.content.Context;
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
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;

public class NotificationFragment extends Fragment {

    private static final String TAG = "NotificationFragment";

    private RecyclerView rvNotifications;
    private SwipeRefreshLayout swipeRefresh;
    private ShimmerFrameLayout shimmerContainer;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private User currentUser;

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
        shimmerContainer = view.findViewById(R.id.shimmer_view_container);

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                fetchNotifications(false);
            });
        }

        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notificationList);
        rvNotifications.setAdapter(adapter);

        showSkeleton(true);
        fetchNotifications(true);
        connectWebSocket();

        return view;
    }

    private void showSkeleton(boolean show) {
        if (shimmerContainer != null) {
            if (show) {
                shimmerContainer.startShimmer();
                shimmerContainer.setVisibility(View.VISIBLE);
                rvNotifications.setVisibility(View.GONE);
            } else {
                shimmerContainer.stopShimmer();
                shimmerContainer.setVisibility(View.GONE);
                rvNotifications.setVisibility(View.VISIBLE);
            }
        }
    }

    private void connectWebSocket() {
        if (currentUser == null) return;
        String userId = currentUser.getId();
        String wsUrl = "ws://192.168.99.102:8001/ws/notifications/" + userId + "/";

        client = new OkHttpClient();
        Request request = new Request.Builder().url(wsUrl).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                try {
                    JSONObject json = new JSONObject(text);
                    JSONObject data = json.optJSONObject("data");
                    if (data == null) data = json;

                    Notification notification = new Notification();
                    notification.setId(data.optString("id"));
                    notification.setMessage(data.optString("message"));
                    notification.setType(data.optString("type"));
                    notification.setRead(false);
                    
                    String now = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
                    notification.setCreatedAt(data.optString("created_at", now));

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
        });
    }

    private void fetchNotifications(boolean isFirstLoad) {
        if (currentUser == null) return;

        RetrofitClient.getApiService().getNotifications(currentUser.getId()).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(@NonNull Call<List<Notification>> call, @NonNull retrofit2.Response<List<Notification>> response) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                showSkeleton(false);

                if (response.isSuccessful() && response.body() != null) {
                    notificationList.clear();
                    notificationList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Notification>> call, @NonNull Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                showSkeleton(false);
                Log.e(TAG, "fetch error", t);
            }
        });
    }

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
            public void onFailure(Call<Notification> call, Throwable t) {
                Log.e(TAG, "markAsRead error", t);
            }
        });
    }

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

            String displayName = (item.getSenderName() != null && !item.getSenderName().isEmpty()) 
                    ? item.getSenderName() : "MasterAI";
            
            String content = item.getContent();
            if (content == null) content = "";

            SpannableString spannable = new SpannableString(displayName + " " + content);
            spannable.setSpan(
                    new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    0, displayName.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            holder.tvContent.setText(spannable);
            holder.tvTime.setText(formatTimeAgo(item.getCreatedAt()));

            // Fix lỗi màu đen: Chuyển sang tông màu sáng
            if (item.isRead()) {
                holder.cardView.setStrokeColor(Color.TRANSPARENT);
                holder.vDot.setVisibility(View.GONE);
                holder.cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5")); 
            } else {
                holder.cardView.setStrokeColor(Color.parseColor("#00BFFF"));
                holder.vDot.setVisibility(View.VISIBLE);
                holder.cardView.setCardBackgroundColor(Color.WHITE);
            }

            Glide.with(holder.ivAvatar.getContext())
                    .load(item.getSenderAvatar())
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .into(holder.ivAvatar);

            holder.itemView.setOnClickListener(v -> {
                handleNotificationClick(v.getContext(), item);
                if (!item.isRead()) {
                    markAsRead(item, position);
                }
            });
        }

        private String formatTimeAgo(String dateStr) {
            if (dateStr == null || dateStr.isEmpty()) return "";
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                if (dateStr.contains(".")) {
                    sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault());
                }
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                long time = sdf.parse(dateStr).getTime();
                return android.text.format.DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS).toString();
            } catch (Exception e) {
                return dateStr;
            }
        }

        private void handleNotificationClick(Context context, Notification item) {
            // Navigation logic
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvContent, tvTime;
            View vDot;
            ImageView ivAvatar;
            MaterialCardView cardView;

            ViewHolder(View v) {
                super(v);
                tvContent = v.findViewById(R.id.tvNotifContent);
                tvTime = v.findViewById(R.id.tvNotifTime);
                vDot = v.findViewById(R.id.vNotifDot);
                ivAvatar = v.findViewById(R.id.ivNotifAvatar);
                cardView = (MaterialCardView) v;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocket != null) {
            webSocket.close(1000, "Closed");
        }
    }
}
