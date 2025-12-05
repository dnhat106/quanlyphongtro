//adapter: cầu nối giữa dữ liệu và giao diện hiển thị
// Mục đích file: File này dùng để hiển thị danh sách hình ảnh phòng
// function: 
// - RoomImageAdapter(): Khởi tạo adapter với danh sách ảnh
// - onCreateViewHolder(): Tạo ViewHolder cho item hình ảnh
// - onBindViewHolder(): Bind dữ liệu hình ảnh vào ViewHolder và load hình ảnh bằng Glide
// - getItemCount(): Trả về số lượng hình ảnh
// - ImageViewHolder(): ViewHolder chứa ImageView để hiển thị hình ảnh
package com.example.appquanlytimtro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appquanlytimtro.R;

import java.util.List;

public class RoomImageAdapter extends RecyclerView.Adapter<RoomImageAdapter.ImageViewHolder> {
    
    private List<String> imageUrls;
    
    public RoomImageAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room_image, parent, false);
        return new ImageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_room_placeholder)
                .error(R.drawable.ic_room_placeholder)
                .into(holder.imageView);
    }
    
    @Override
    public int getItemCount() {
        return imageUrls.size();
    }
    
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivRoomImage);
        }
    }
}
