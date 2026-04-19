package com.example.masterai.ui.comminity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.ViewUtils;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.model.Comment;
import com.example.masterai.model.User;
import com.example.masterai.utils.PostUtils;
import com.example.masterai.utils.ViewsUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private Map<String, User> userCache = new HashMap<>();

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.tvContent.setText(comment.getContent());
        holder.tvTime.setText(PostUtils.getTimeAgo(comment.getCreatedAt()));

        // Load user info for comment
        loadUserInfo(holder, comment.getUserId());
    }

    private void loadUserInfo(CommentViewHolder holder, String userId) {
        holder.tvUserName.setText("Loading...");
        if (userCache.containsKey(userId)) {
            displayUserInfo(holder, userCache.get(userId));
        } else {
            RetrofitClient.getApiService().getUserById(userId,null).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        userCache.put(userId, user);
                        displayUserInfo(holder, user);
                    } else {
                        holder.tvUserName.setText("User " + userId);
                    }
                }
                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    holder.tvUserName.setText("User " + userId);
                }
            });
        }
    }

    private void displayUserInfo(CommentViewHolder holder, User user) {
        holder.tvUserName.setText(user.getUsername());
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(user.getAvatarUrl())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .circleCrop()
                .into(holder.ivAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUserName, tvContent, tvTime;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}