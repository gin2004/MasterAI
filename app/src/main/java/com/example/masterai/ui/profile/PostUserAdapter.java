package com.example.masterai.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.model.Post;

import java.util.List;

public class PostUserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_SKELETON = 1;

    private List<Post> postList;
    private OnPostClickListener listener;
    private boolean isLoading = true;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public PostUserAdapter(List<Post> postList, OnPostClickListener listener) {
        this.postList = postList;
        this.listener = listener;
    }

    public void setLoading(boolean loading) {
        this.isLoading = loading;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return isLoading ? VIEW_TYPE_SKELETON : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SKELETON) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_skeleton, parent, false);
            return new SkeletonViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_grid, parent, false);
            return new PostViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostViewHolder && !isLoading) {
            Post post = postList.get(position);
            PostViewHolder postHolder = (PostViewHolder) holder;

            if (post.getMedia() != null && !post.getMedia().isEmpty()) {
                Glide.with(postHolder.itemView.getContext())
                        .load(post.getMedia().get(0).getUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .centerCrop()
                        .into(postHolder.ivPostThumbnail);
                
                postHolder.ivMultiMediaIcon.setVisibility(post.getMedia().size() > 1 ? View.VISIBLE : View.GONE);
            } else {
                postHolder.ivPostThumbnail.setImageResource(R.drawable.bg_bottom_nav);
                postHolder.ivMultiMediaIcon.setVisibility(View.GONE);
            }

            postHolder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onPostClick(post);
            });
        }
    }

    @Override
    public int getItemCount() {
        return isLoading ? 9 : postList.size(); // Show 9 skeleton items for grid
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPostThumbnail, ivMultiMediaIcon;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPostThumbnail = itemView.findViewById(R.id.ivPostThumbnail);
            ivMultiMediaIcon = itemView.findViewById(R.id.ivMultiMediaIcon);
        }
    }

    public static class SkeletonViewHolder extends RecyclerView.ViewHolder {
        public SkeletonViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
