package com.example.masterai.ui.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.masterai.R;
import com.example.masterai.model.Generation;
import java.util.List;

public class GenerationAdapter extends RecyclerView.Adapter<GenerationAdapter.ViewHolder> {
    private List<Generation> generationList;

    public GenerationAdapter(List<Generation> generationList) {
        this.generationList = generationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_generation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Generation generation = generationList.get(position);
        holder.ivGeneration.setImageResource(generation.getImageRes());
    }

    @Override
    public int getItemCount() {
        return generationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGeneration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGeneration = itemView.findViewById(R.id.ivGeneration);
        }
    }
}