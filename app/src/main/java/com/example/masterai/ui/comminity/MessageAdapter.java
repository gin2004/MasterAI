package com.example.masterai.ui.comminity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.masterai.R;
import com.example.masterai.model.Message;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Message message);
    }

    public MessageAdapter(List<Message> messageList, OnItemClickListener listener) {
        this.messageList = messageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.tvUsername.setText(message.getUsername());
        holder.tvLastMessage.setText(message.getLastMessage());
        holder.tvTime.setText(message.getTime());
        holder.vOnlineStatus.setVisibility(message.isOnline() ? View.VISIBLE : View.GONE);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(message);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivAvatar;
        View vOnlineStatus;
        TextView tvUsername, tvLastMessage, tvTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            vOnlineStatus = itemView.findViewById(R.id.vOnlineStatus);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}