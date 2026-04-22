package com.example.masterai.ui.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.masterai.R;
import com.example.masterai.model.Generation;
import com.example.masterai.utils.AIUtils;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {

    private List<Generation> audioList = new ArrayList<>();
    private final OnAudioClickListener listener;

    // Interface để xử lý sự kiện Play/Pause ở bên ngoài Adapter
    public interface OnAudioClickListener {
        void onPlayClick(Generation item, int position);
    }

    public AudioAdapter(OnAudioClickListener listener) {
        this.listener = listener;
    }

    public void setAudioList(List<Generation> audioList) {
        this.audioList = audioList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_audio, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        Generation item = audioList.get(position);

        holder.tvPrompt.setText(item.getPrompt());

        // Bạn có thể format lại chuỗi thời gian nếu cần
        holder.tvCreatedAt.setText(AIUtils.getInstance().parseDate(item.getCreatedAt()));

        // Xử lý sự kiện click nút Play
        holder.btnPlay.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlayClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioList != null ? audioList.size() : 0;
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgAudioCover;
        TextView tvPrompt, tvCreatedAt;
        ImageButton btnPlay;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAudioCover = itemView.findViewById(R.id.imgAudioCover);
            tvPrompt = itemView.findViewById(R.id.tvPrompt);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            btnPlay = itemView.findViewById(R.id.btnPlay);
        }
    }
}