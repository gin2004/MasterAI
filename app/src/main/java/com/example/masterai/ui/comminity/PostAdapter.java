package com.example.masterai.ui.comminity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.model.Media;
import com.example.masterai.model.Post;
import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;

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
        
        // Hiển thị Username (nếu backend trả về object user lồng trong post thì lấy username, 
        // ở đây đang lấy userId tạm thời theo model hiện tại)
        holder.tvName.setText("User " + post.getUserId());
        
        // Hiển thị nội dung bài đăng
        holder.tvContent.setText(post.getContent());
        
        // Hiển thị thời gian (định dạng từ backend)
        holder.tvTime.setText(post.getCreatedAt() != null ? post.getCreatedAt() : "Vừa xong");

        // Hiển thị số lượt like và comment
        holder.tvLikeCount.setText(String.valueOf(post.getLikeCount()));
        holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));

        // Load ảnh bài đăng nếu có
        if (post.getMedia() != null && !post.getMedia().isEmpty()) {
            Media media = post.getMedia().get(0);
            holder.ivPostImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                .load(media.getUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.ivPostImage);
        } else {
            holder.ivPostImage.setVisibility(View.GONE);
        }

        // Xử lý sự kiện khi ấn vào Comment
        View.OnClickListener commentClickListener = v -> {
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            CommentFragment commentFragment = new CommentFragment();
            activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, commentFragment)
                .addToBackStack(null)
                .commit();
        };

        holder.btnComment.setOnClickListener(commentClickListener);
        holder.tvCommentCount.setOnClickListener(commentClickListener);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivPostImage;
        TextView tvName, tvTime, tvContent, tvLikeCount, tvCommentCount;
        View btnComment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            btnComment = itemView.findViewById(R.id.btnComment);
        }
    }
}
