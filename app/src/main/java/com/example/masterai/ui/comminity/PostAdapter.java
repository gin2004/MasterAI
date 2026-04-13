package com.example.masterai.ui.comminity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.model.Media;
import com.example.masterai.model.Post;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;

    public PostAdapter(List<Post> posts) {
        this.posts = posts;
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
        holder.tvName.setText("User " + post.getUserId());
        holder.tvContent.setText(post.getContent());
        holder.tvTime.setText(post.getCreatedAt() != null ? post.getCreatedAt() : "Just now");

        // Load post image if exists
        if (post.getMedia() != null && !post.getMedia().isEmpty()) {
            Media media = post.getMedia().get(0);
            holder.ivPostImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                .load(media.getUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivPostImage);
        } else {
            holder.ivPostImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivPostImage;
        TextView tvName, tvTime, tvContent;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
