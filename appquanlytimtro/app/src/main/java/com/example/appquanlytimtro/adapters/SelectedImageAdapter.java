//adapter: cầu nối giữa dữ liệu và giao diện hiển thị
// Mục đích file: File này dùng để hiển thị danh sách hình ảnh đã chọn với nút xóa
// function: 
// - SelectedImageAdapter(): Khởi tạo adapter với danh sách ảnh và listener
// - onCreateViewHolder(): Tạo ViewHolder cho item hình ảnh
// - onBindViewHolder(): Bind dữ liệu hình ảnh vào ViewHolder và xử lý sự kiện xóa
// - getItemCount(): Trả về số lượng hình ảnh
// - ImageViewHolder(): ViewHolder chứa ImageView và nút xóa
package com.example.appquanlytimtro.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appquanlytimtro.R;

import java.util.List;

public class SelectedImageAdapter extends RecyclerView.Adapter<SelectedImageAdapter.ImageViewHolder> {
    
    private List<Uri> imageUris;
    private OnImageRemoveListener listener;
    
    public interface OnImageRemoveListener {
        void onImageRemove(int position);
    }
    
    public SelectedImageAdapter(List<Uri> imageUris, OnImageRemoveListener listener) {
        this.imageUris = imageUris;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_image, parent, false);
        return new ImageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        
        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(imageUri)
                .centerCrop()
                .into(holder.imageView);
        
        // Set remove button click listener
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageRemove(position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return imageUris.size();
    }
    
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView btnRemove;
        
        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivSelectedImage);
            btnRemove = itemView.findViewById(R.id.ivRemoveImage);
        }
    }
}
