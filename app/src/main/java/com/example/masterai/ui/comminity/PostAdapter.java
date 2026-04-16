package com.example.masterai.ui.comminity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.model.Post;
import com.example.masterai.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    // Cache to avoid multiple API calls for the same user
    private Map<String, User> userCache = new HashMap<>();

    public PostAdapter(List<Post> posts) {
        this.posts = posts != null ? posts : new ArrayList<>();
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        
        holder.tvContent.setText(post.getContent());
        holder.tvTime.setText(post.getCreatedAt() != null ? post.getCreatedAt() : "Just now");
        holder.tvLikeCount.setText(String.valueOf(post.getLikeCount()));
        holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));

        // Initial default name while loading
        holder.tvName.setText("Loading...");

        // Get user info (from cache or API)
        String userId = post.getUserId();
        if (userCache.containsKey(userId)) {
            displayUserInfo(holder, userCache.get(userId));
        } else {
            RetrofitClient.getApiService().getUserById(userId).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        userCache.put(userId, user);
                        displayUserInfo(holder, user);
                    } else {
                        holder.tvName.setText("User " + userId);
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    holder.tvName.setText("User " + userId);
                }
            });
        }

        // Set up Media RecyclerView
        if (post.getMedia() != null && !post.getMedia().isEmpty()) {
            holder.rvPostMedia.setVisibility(View.VISIBLE);
            MediaAdapter mediaAdapter = new MediaAdapter(post.getMedia());
            holder.rvPostMedia.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            holder.rvPostMedia.setAdapter(mediaAdapter);
        } else {
            holder.rvPostMedia.setVisibility(View.GONE);
        }

        View.OnClickListener commentClickListener = v -> {
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            CommentFragment commentFragment = new CommentFragment();
            activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, commentFragment)
                .addToBackStack(null)
                .commit();
        };

        holder.btnComment.setOnClickListener(commentClickListener);
    }

    private void displayUserInfo(PostViewHolder holder, User user) {
        holder.tvName.setText(user.getUsername());
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
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvTime, tvContent, tvLikeCount, tvCommentCount;
        RecyclerView rvPostMedia;
        View btnComment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            rvPostMedia = itemView.findViewById(R.id.rvPostMedia);
            btnComment = itemView.findViewById(R.id.btnComment);
        }
    }
}
