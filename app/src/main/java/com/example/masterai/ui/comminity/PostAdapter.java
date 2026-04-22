package com.example.masterai.ui.comminity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.masterai.MainActivity;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.model.Notification;
import com.example.masterai.model.Post;
import com.example.masterai.model.User;
import com.example.masterai.ui.activity.ProfileActivity;
import com.example.masterai.utils.PostUtils;
import com.example.masterai.utils.UserManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
            holder.btnMore.setVisibility(View.VISIBLE);
        } else {
            holder.btnMore.setVisibility(View.GONE);
        }

        loadUserInfo(holder, post.getUserId(), isMyPost);
        setupMediaRecyclerView(holder, post);

        holder.btnLike.setOnClickListener(v -> toggleLike(post, holder));

        holder.btnComment.setOnClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            CommentFragment commentFragment = new CommentFragment();
            Bundle bundle = new Bundle();
            bundle.putString("post_id", post.getId());
            bundle.putString("post_user_id", post.getUserId());
            commentFragment.setArguments(bundle);
            activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, commentFragment)
                .addToBackStack(null)
                .commit();
        });

        holder.btnMore.setOnClickListener(v -> showPostOptions(v, post, position));

        // Xử lý sự kiện click vào Avatar hoặc Username
        View.OnClickListener profileClickListener = v -> {
            if (currentUser != null && post.getUserId().equals(currentUser.getId())) {
                if (v.getContext() instanceof MainActivity) {
                    ((MainActivity) v.getContext()).navigateToProfile();
                }
            } else {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("user_id", post.getUserId());
                v.getContext().startActivity(intent);
            }
        };

        holder.ivAvatar.setOnClickListener(profileClickListener);
        holder.tvName.setOnClickListener(profileClickListener);
    }

    private void showPostOptions(View view, Post post, int position) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(view.getContext());
        View bottomSheetView = LayoutInflater.from(view.getContext()).inflate(R.layout.bottom_sheet_post_options, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        View llEdit = bottomSheetView.findViewById(R.id.llEdit);
        View llDelete = bottomSheetView.findViewById(R.id.llDelete);

        llEdit.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            AppCompatActivity activity = (AppCompatActivity) view.getContext();
            EditPostFragment editPostFragment = EditPostFragment.newInstance(post);
            activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editPostFragment)
                .addToBackStack(null)
                .commit();
        });

        llDelete.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            deletePost(post.getId(), position, view);
        });

        bottomSheetDialog.show();
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
        body.put("username", currentUser.getUsername()); // Thêm dòng này
        body.put("avatar", currentUser.getAvatarUrl());     // Thêm dòng này
        
        int initialLikes = post.getLikeCount();

        RetrofitClient.getApiService().toggleLike(post.getId(), body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().get("message");
                    if ("liked".equals(message)) {
                        post.setLikeCount(initialLikes + 1);
                        holder.ivLike.setColorFilter(holder.itemView.getContext().getColor(R.color.purple_primary));
                        if (!currentUser.getId().equals(post.getUserId())) {
                            sendNotification(post.getUserId(), currentUser.getUsername() + " đã thích bài viết của bạn", "like");
                        }
                    } else {
                        post.setLikeCount(initialLikes - 1);
                        holder.ivLike.setColorFilter(holder.itemView.getContext().getColor(R.color.gray_text));
                    }
                    holder.tvLikeCount.setText(String.valueOf(post.getLikeCount()));
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {}
        });
    }

    private void sendNotification(String recipientId, String content, String type) {
        Notification notification = new Notification(recipientId, content, type);
        RetrofitClient.getApiService().createNotification(notification).enqueue(new Callback<Notification>() {
            @Override
            public void onResponse(Call<Notification> call, Response<Notification> response) {}
            @Override
            public void onFailure(Call<Notification> call, Throwable t) {}
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

        if (holder.btnFollow != null) {
            if (isMyPost) {
                holder.btnFollow.setVisibility(View.GONE);
            } else {
                holder.btnFollow.setVisibility(user.isFollowed() ? View.GONE : View.VISIBLE);
            }
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
        ImageView ivAvatar, ivLike, btnMore;
        TextView tvName, tvTime, tvContent, tvLikeCount, tvCommentCount;
        RecyclerView rvPostMedia;
        View btnComment, btnLike, btnFollow;

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
            btnFollow = itemView.findViewById(R.id.btnFollow);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
