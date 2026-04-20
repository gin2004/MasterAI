package com.example.masterai.ui.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.model.Generation;
import java.util.List;

public class GenerationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Generation> generationList;
    private boolean isLoading = true;
    private final int SKELETON_COUNT = 6;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Generation generation);
    }

    public GenerationAdapter(List<Generation> generationList) {
        this.generationList = generationList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setLoading(boolean loading) {
        this.isLoading = loading;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return isLoading ? 0 : 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_generation_skeleton, parent, false);
            return new SkeletonViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_generation, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder && !isLoading) {
            Generation generation = generationList.get(position);
            Glide.with(holder.itemView.getContext())
                    .load(generation.getMediaUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(((ViewHolder) holder).ivGeneration);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(generation);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return isLoading ? SKELETON_COUNT : (generationList != null ? generationList.size() : 0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGeneration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGeneration = itemView.findViewById(R.id.ivGeneration);
        }
    }

    public static class SkeletonViewHolder extends RecyclerView.ViewHolder {
        public SkeletonViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}