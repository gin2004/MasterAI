package com.example.masterai.ui.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.model.Asset;
import com.example.masterai.utils.AIUtils;

import java.util.List;

public class AssetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Asset> assetList;
    private boolean isLoading = true;
    private final int SKELETON_COUNT = 6;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Asset asset);
    }

    public AssetAdapter(List<Asset> assetList) {
        this.assetList = assetList;
    }

    public AssetAdapter(List<Asset> assetList, OnItemClickListener listener) {
        this.assetList = assetList;
        this.listener = listener;
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asset_skeleton, parent, false);
            return new SkeletonViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asset_sample, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder && !isLoading) {
            ViewHolder viewHolder = (ViewHolder) holder;
            Asset asset = assetList.get(position);
            Glide.with(holder.itemView.getContext())
                    .load(asset.getMediaUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(viewHolder.ivAsset);
            viewHolder.tvType.setText(asset.getType());
            viewHolder.tvCreateAt.setText(AIUtils.getInstance().parseDate(asset.getCreatedAt()));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(asset);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return isLoading ? SKELETON_COUNT : (assetList != null ? assetList.size() : 0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAsset;
        TextView tvType, tvCreateAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAsset = itemView.findViewById(R.id.ivAsset);
            tvType = itemView.findViewById(R.id.tvAssetType);
            tvCreateAt = itemView.findViewById(R.id.createAt);
        }
    }

    public static class SkeletonViewHolder extends RecyclerView.ViewHolder {
        public SkeletonViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}