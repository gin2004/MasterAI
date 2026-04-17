package com.example.masterai.ui.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.masterai.R;
import com.example.masterai.model.Asset;
import java.util.List;

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.ViewHolder> {
    private List<Asset> assetList;

    public AssetAdapter(List<Asset> assetList) {
        this.assetList = assetList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asset_sample, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Asset asset = assetList.get(position);
        holder.tvName.setText(asset.getName());
        holder.tvStatus.setText(asset.getStatus());
        holder.ivAsset.setImageResource(asset.getImageRes());
    }

    @Override
    public int getItemCount() {
        return assetList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAsset;
        TextView tvName, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAsset = itemView.findViewById(R.id.ivAsset);
            tvName = itemView.findViewById(R.id.tvAssetName);
            tvStatus = itemView.findViewById(R.id.tvAssetStatus);
        }
    }
}