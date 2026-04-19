package com.example.masterai.ui.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.databinding.ItemUserMessageBinding;
import com.example.masterai.model.UserMessage;
import com.example.masterai.utils.UserManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class UserMessageAdapter extends RecyclerView.Adapter<UserMessageAdapter.ViewHolder> {

    private Context context;
    private List<UserMessage> list;
    private String myUserId;
    private OnItemClickListener listener;

    // Interface để lắng nghe sự kiện click
    public interface OnItemClickListener {
        void onItemClick(UserMessage message);
    }

    public UserMessageAdapter(Context context, List<UserMessage> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;

        // Lấy ID của mình để biết ai là người gửi tin nhắn cuối cùng
        if (UserManager.getInstance(context).getUser() != null) {
            this.myUserId = UserManager.getInstance(context).getUser().getId();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng ViewBinding tự sinh ra từ file item_user_message.xml
        ItemUserMessageBinding binding = ItemUserMessageBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserMessage item = list.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    // --- ViewHolder Class ---
    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemUserMessageBinding binding;

        public ViewHolder(ItemUserMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Bắt sự kiện click vào cả dòng
            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(list.get(position));
                }
            });
        }

        public void bind(UserMessage item) {
            // 1. Gán Tên
            binding.tvUserName.setText(item.getTargetUserName());

            // 2. Load Avatar bằng Glide
            Glide.with(context)
                    .load(item.getTargetAvatarUrl())
                    .placeholder(R.drawable.ic_user) // Thay bằng icon mặc định của bạn
                    .circleCrop()
                    .into(binding.ivAvatar);

            // 3. Trạng thái Online (Hiện/Ẩn chấm xanh)
            binding.vOnlineStatus.setVisibility(item.isOnline() ? View.VISIBLE : View.GONE);

            // 4. Xử lý hiển thị Tin nhắn cuối
            String displayMsg = "";
            if (item.getLastSenderId() != null && item.getLastSenderId().equals(myUserId)) {
                displayMsg = "Bạn: " + item.getLastMessage();
            } else {
                displayMsg = item.getLastMessage();
            }
            binding.tvLastMessage.setText(displayMsg);

            // 5. Xử lý đếm tin nhắn chưa đọc (Badge đỏ)
            if (item.getUnreadCount() > 0) {
                binding.tvUnreadBadge.setVisibility(View.VISIBLE);
                binding.tvUnreadBadge.setText(String.valueOf(item.getUnreadCount()));

                // In đậm tên và tin nhắn nếu có tin nhắn mới
                binding.tvUserName.setTypeface(null, Typeface.BOLD);
                binding.tvLastMessage.setTypeface(null, Typeface.BOLD);
                binding.tvLastMessage.setTextColor(context.getResources().getColor(android.R.color.black));
            } else {
                binding.tvUnreadBadge.setVisibility(View.GONE);

                // Trở về font thường nếu đã đọc
                binding.tvUserName.setTypeface(null, Typeface.NORMAL);
                binding.tvLastMessage.setTypeface(null, Typeface.NORMAL);
                binding.tvLastMessage.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            }

            // 6. Định dạng thời gian (Helper)
            binding.tvTime.setText(formatTime(item.getLastMessageTimestamp()));
        }
    }

    // --- Helper Format Thời Gian ---
    private String formatTime(String isoTime) {
        if (isoTime == null || isoTime.isEmpty()) return "";
        try {
            // Chuyển từ UTC ISO 8601 sang Date
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = isoFormat.parse(isoTime);

            // Format lại thành "HH:mm" (ví dụ: 14:30). Bạn có thể nâng cấp logic ở đây để hiện "Hôm qua" nếu khác ngày.
            SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
}