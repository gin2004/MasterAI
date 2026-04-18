package com.example.masterai.ui.comminity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.model.Post;
import com.example.masterai.model.User;
import com.example.masterai.utils.PostUtils;
import com.example.masterai.utils.UserManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
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
        holder.tvTime.setText(PostUtils.getTimeAgo(post.getCreatedAt()));
        holder.tvLikeCount.setText(String.valueOf(post.getLikeCount()));
        holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));

        User currentUser = UserManager.getInstance(holder.itemView.getContext()).getUser();
        boolean isMyPost = currentUser != null && currentUser.getId().equals(post.getUserId());

        if (isMyPost) {
            holder.llMyPostActions.setVisibility(View.VISIBLE);
        } else {
            holder.llMyPostActions.setVisibility(View.GONE);

        }

        loadUserInfo(holder, post.getUserId(), isMyPost);
        setupMediaRecyclerView(holder, post);

        holder.btnLike.setOnClickListener(v -> toggleLike(post, holder));

        holder.btnComment.setOnClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            CommentFragment commentFragment = new CommentFragment();
            Bundle bundle = new Bundle();
            bundle.putString("post_id", post.getId());
            commentFragment.setArguments(bundle);
            activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, commentFragment)
                .addToBackStack(null)
                .commit();
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                .setTitle("Xóa bài đăng")
                .setMessage("Bạn có chắc chắn muốn xóa bài đăng này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        deletePost(post.getId(), currentPosition, v);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
        });

        holder.btnEdit.setOnClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            EditPostFragment editPostFragment = EditPostFragment.newInstance(post);
            activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editPostFragment)
                .addToBackStack(null)
                .commit();
        });
    }

    private void deletePost(String postId, int position, View view) {
        RetrofitClient.getApiService().deletePost(postId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    posts.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, posts.size());
                    Toast.makeText(view.getContext(), "Post deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(view.getContext(), "Failed to delete post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(view.getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleLike(Post post, PostViewHolder holder) {
        User currentUser = UserManager.getInstance(holder.itemView.getContext()).getUser();
        if (currentUser == null) return;
        Map<String, String> body = new HashMap<>();
        body.put("user_id", currentUser.getId());
        holder.tvLikeCount.setText(String.valueOf(post.getLikeCount()+1));

        RetrofitClient.getApiService().toggleLike(post.getId(), body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().get("message");
                    int currentLikes = post.getLikeCount();
                    if ("liked".equals(message)) {
                        post.setLikeCount(currentLikes + 1);
                        holder.ivLike.setColorFilter(holder.itemView.getContext().getColor(R.color.purple_primary));
                    } else {
                        post.setLikeCount(currentLikes - 1);
                        holder.ivLike.setColorFilter(holder.itemView.getContext().getColor(R.color.gray_text));
                    }
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {}
        });
    }

    private void loadUserInfo(PostViewHolder holder, String userId, boolean isMyPost) {
        if (userCache.containsKey(userId)) {
            displayUserInfo(holder, userCache.get(userId), isMyPost);
        } else {
            RetrofitClient.getApiService().getUserById(userId,null).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        userCache.put(userId, user);
                        displayUserInfo(holder, user, isMyPost);
                    }
                }
                @Override
                public void onFailure(Call<User> call, Throwable t) {}
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayUserInfo(PostViewHolder holder, User user, boolean isMyPost) {
        holder.tvName.setText("@"+user.getUsername());
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(user.getAvatarUrl()).circleCrop().into(holder.ivAvatar);
        }

        if (!isMyPost) {
            holder.btnFollow.setVisibility(user.isFollowed() ? View.GONE : View.VISIBLE);
        }
    }

    private void setupMediaRecyclerView(PostViewHolder holder, Post post) {
        if (post.getMedia() != null && !post.getMedia().isEmpty()) {
            holder.rvPostMedia.setVisibility(View.VISIBLE);
            MediaAdapter mediaAdapter = new MediaAdapter(post.getMedia());
            holder.rvPostMedia.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            holder.rvPostMedia.setAdapter(mediaAdapter);
        } else {
            holder.rvPostMedia.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return posts.size(); }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivLike, btnEdit, btnDelete;
        TextView tvName, tvTime, tvContent, tvLikeCount, tvCommentCount;
        RecyclerView rvPostMedia;
        View btnComment, btnLike, btnFollow, llMyPostActions;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivLike = itemView.findViewById(R.id.ivLike);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            rvPostMedia = itemView.findViewById(R.id.rvPostMedia);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnLike = itemView.findViewById(R.id.btnLike);

            llMyPostActions = itemView.findViewById(R.id.llMyPostActions);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
