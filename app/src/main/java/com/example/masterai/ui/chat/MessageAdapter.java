package com.example.masterai.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.databinding.ItemMessageReceivedBinding;
import com.example.masterai.databinding.ItemMessageSentBinding;
import com.example.masterai.model.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Message> messageList;
    private String targetAvatarUrl; // URL avatar của người chat cùng

    // Định nghĩa loại View
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public MessageAdapter(Context context, List<Message> messageList, String targetAvatarUrl) {
        this.context = context;
        this.messageList = messageList;
        this.targetAvatarUrl = targetAvatarUrl;
    }

    @Override
    public int getItemViewType(int position) {
        // Trả về type dựa trên biến isSentByMe trong model
        if (messageList.get(position).isSentByMe()) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            ItemMessageSentBinding binding = ItemMessageSentBinding.inflate(LayoutInflater.from(context), parent, false);
            return new SentViewHolder(binding);
        } else {
            ItemMessageReceivedBinding binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(context), parent, false);
            return new ReceivedViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedViewHolder) {
            ((ReceivedViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    // ==========================================
    // VIEWHOLDER: TIN NHẮN GỬI ĐI (Bên Phải)
    // ==========================================
    class SentViewHolder extends RecyclerView.ViewHolder {
        private ItemMessageSentBinding binding;

        public SentViewHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Message message) {
            // Định dạng thời gian
            binding.tvTime.setText(formatTime(message.getTimestamp()));

            // Xử lý hiển thị Chữ hoặc Ảnh
            if (message.getType() == Message.TYPE_TEXT) {
                binding.tvMessage.setVisibility(View.VISIBLE);
                binding.ivMessageImage.setVisibility(View.GONE);
                binding.tvMessage.setText(message.getText());
            } else if (message.getType() == Message.TYPE_IMAGE) {
                binding.tvMessage.setVisibility(View.GONE);
                binding.ivMessageImage.setVisibility(View.VISIBLE);

                Glide.with(context)
                        .load(message.getImageUrl())
                        .override(600, 600) // Resize để tránh giật lag
                        .into(binding.ivMessageImage);
            }
        }
    }

    // ==========================================
    // VIEWHOLDER: TIN NHẮN NHẬN VỀ (Bên Trái)
    // ==========================================
    class ReceivedViewHolder extends RecyclerView.ViewHolder {
        private ItemMessageReceivedBinding binding;

        public ReceivedViewHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Message message) {
            binding.tvTime.setText(formatTime(message.getTimestamp()));

            // Load Avatar người gửi
            Glide.with(context)
                    .load(targetAvatarUrl)
                    .placeholder(R.drawable.ic_user)
                    .circleCrop()
                    .into(binding.ivAvatar);

            // Xử lý hiển thị Chữ hoặc Ảnh
            if (message.getType() == Message.TYPE_TEXT) {
                binding.tvMessage.setVisibility(View.VISIBLE);
                binding.ivMessageImage.setVisibility(View.GONE);
                binding.tvMessage.setText(message.getText());
            } else if (message.getType() == Message.TYPE_IMAGE) {
                binding.tvMessage.setVisibility(View.GONE);
                binding.ivMessageImage.setVisibility(View.VISIBLE);

                Glide.with(context)
                        .load(message.getImageUrl())
                        .override(600, 600)
                        .into(binding.ivMessageImage);
            }
        }
    }

    // Helper: Chuyển long timestamp thành HH:mm
    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}