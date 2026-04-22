package com.example.masterai.ui.comminity;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.masterai.R;
import java.util.List;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder> {

    private List<Uri> imageUris;

    public ImagePreviewAdapter(List<Uri> imageUris) {
        this.imageUris = imageUris;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = imageUris.get(position);
        Glide.with(holder.itemView.getContext())
                .load(uri)
                .centerCrop() // Thêm để ảnh hiển thị gọn trong khung
                .into(holder.ivPreview);

        holder.btnRemove.setOnClickListener(v -> {
            // Lấy vị trí hiện tại thực tế của holder
            int currentPosition = holder.getBindingAdapterPosition();

            // Kiểm tra tránh lỗi IndexOutOfBoundsException
            if (currentPosition != RecyclerView.NO_POSITION) {
                imageUris.remove(currentPosition);

                // Dùng notifyItemRemoved sẽ tạo hiệu ứng xóa mượt mà hơn notifyDataSetChanged
                notifyItemRemoved(currentPosition);
                notifyItemRangeChanged(currentPosition, imageUris.size());

                // Gọi callback nếu bạn cần thông báo cho Fragment (ví dụ: ẩn RecyclerView nếu hết ảnh)
                if (onImageRemovedListener != null) {
                    onImageRemovedListener.onRemoved();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPreview;
        View btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPreview = itemView.findViewById(R.id.ivPreview);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }public interface OnImageRemovedListener {
        void onRemoved();
    }
    private OnImageRemovedListener onImageRemovedListener;
    public void setOnImageRemovedListener(OnImageRemovedListener listener) {
        this.onImageRemovedListener = listener;
    }

}
