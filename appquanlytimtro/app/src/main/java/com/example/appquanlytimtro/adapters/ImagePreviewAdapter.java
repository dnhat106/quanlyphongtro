//adapter: cầu nối giữa dữ liệu và giao diện hiển thị
// Mục đích file: File này dùng để hiển thị danh sách hình ảnh đã chọn
// function: 
// - ImagePreviewAdapter(): Khởi tạo adapter với danh sách ảnh và listener
// - onCreateViewHolder(): Tạo ViewHolder cho item hình ảnh
// - onBindViewHolder(): Bind dữ liệu hình ảnh vào ViewHolder và xử lý sự kiện xóa
// - getItemCount(): Trả về số lượng hình ảnh
// - ImageViewHolder(): ViewHolder chứa ImageView và nút xóa
package com.example.appquanlytimtro.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appquanlytimtro.R;

import java.util.List;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder> {
    
    private List<Uri> imageUris;
    private OnImageRemoveListener removeListener;
    
    public interface OnImageRemoveListener {
        void onRemove(int position);
    }
    
    public ImagePreviewAdapter(List<Uri> imageUris, OnImageRemoveListener removeListener) {
        this.imageUris = imageUris;
        this.removeListener = removeListener;
    }
    
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_preview, parent, false);
        return new ImageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        
        Glide.with(holder.itemView.getContext())
                .load(imageUri)
                .centerCrop()
                .placeholder(R.drawable.ic_image)
                .into(holder.imageView);
        
        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemove(position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return imageUris.size();
    }
    
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btnRemove;
        
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}

