package com.example.masterai.ui.comminity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.model.Media;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private List<Media> mediaList;

    public MediaAdapter(List<Media> mediaList) {
        this.mediaList = mediaList;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Media media = mediaList.get(position);
        
        if ("image".equals(media.getMediaType()) || "avatar".equals(media.getMediaType())) {
            holder.ivMediaImage.setVisibility(View.VISIBLE);
            holder.llMediaVoice.setVisibility(View.GONE);
            
            Glide.with(holder.itemView.getContext())
                .load(media.getUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivMediaImage);

            holder.ivMediaImage.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), FullScreenImageActivity.class);
                intent.putExtra("image_url", media.getUrl());
                v.getContext().startActivity(intent);
            });
                
        } else if ("voice".equals(media.getMediaType())) {
            holder.ivMediaImage.setVisibility(View.GONE);
            holder.llMediaVoice.setVisibility(View.VISIBLE);
            
            // Handle voice play/pause logic here if needed
        }
    }

    @Override
    public int getItemCount() {
        return mediaList != null ? mediaList.size() : 0;
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMediaImage;
        View llMediaVoice;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMediaImage = itemView.findViewById(R.id.ivMediaImage);
            llMediaVoice = itemView.findViewById(R.id.llMediaVoice);
        }
    }
}
