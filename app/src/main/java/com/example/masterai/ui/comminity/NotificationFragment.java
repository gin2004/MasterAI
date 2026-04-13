package com.example.masterai.ui.comminity;

import android.os.Bundle;
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
import com.example.masterai.R;
import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        }

        RecyclerView rvNotifications = view.findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        List<NotificationItem> notifications = new ArrayList<>();
        notifications.add(new NotificationItem("Sarah Miller liked your post", "2m ago", true));
        notifications.add(new NotificationItem("David Chen commented: 'Amazing work!'", "15m ago", true));
        notifications.add(new NotificationItem("Alex Johnson started following you", "1h ago", false));
        notifications.add(new NotificationItem("Emily White liked your photo", "3h ago", false));
        notifications.add(new NotificationItem("Your AI generation is ready to view", "5h ago", false));

        rvNotifications.setAdapter(new NotificationAdapter(notifications));

        return view;
    }

    static class NotificationItem {
        String content;
        String time;
        boolean isUnread;

        NotificationItem(String content, String time, boolean isUnread) {
            this.content = content;
            this.time = time;
            this.isUnread = isUnread;
        }
    }

    static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        List<NotificationItem> items;

        NotificationAdapter(List<NotificationItem> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotificationItem item = items.get(position);
            holder.tvContent.setText(item.content);
            holder.tvTime.setText(item.time);
            holder.vDot.setVisibility(item.isUnread ? View.VISIBLE : View.GONE);
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvContent, tvTime;
            View vDot;
            ViewHolder(View v) {
                super(v);
                tvContent = v.findViewById(R.id.tvNotifContent);
                tvTime = v.findViewById(R.id.tvNotifTime);
                vDot = v.findViewById(R.id.vNotifDot);
            }
        }
    }
}