//adapter: cầu nối giữa dữ liệu và giao diện hiển thị
// Mục đích file: File này dùng để quản lý danh sách phòng cho chủ trọ
// function: 
// - LandlordRoomAdapter(): Khởi tạo adapter với danh sách phòng và listener
// - onCreateViewHolder(): Tạo ViewHolder cho item phòng
// - onBindViewHolder(): Bind dữ liệu phòng vào ViewHolder
// - getItemCount(): Trả về số lượng phòng
// - updateRooms(): Cập nhật danh sách phòng mới
// - RoomViewHolder(): Khởi tạo ViewHolder và tìm các view con
// - bind(): Hiển thị thông tin phòng và xử lý sự kiện
// - getStatusText(): Chuyển đổi mã trạng thái thành text hiển thị
// - getStatusColor(): Lấy màu sắc tương ứng với trạng thái
package com.example.appquanlytimtro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.Room;
import com.example.appquanlytimtro.utils.ImageUtils;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class LandlordRoomAdapter extends RecyclerView.Adapter<LandlordRoomAdapter.RoomViewHolder> {
    
    private List<Room> rooms;
    private OnRoomActionListener listener;
    
    public interface OnRoomActionListener {
        void onEditRoom(Room room);
        void onDeleteRoom(Room room);
    }
    
    public LandlordRoomAdapter(List<Room> rooms, OnRoomActionListener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_landlord_room, parent, false);
        return new RoomViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.bind(room);
    }
    
    @Override
    public int getItemCount() {
        return rooms.size();
    }
    
    public void updateRooms(List<Room> newRooms) {
        this.rooms.clear();
        this.rooms.addAll(newRooms);
        notifyDataSetChanged();
    }
    
    class RoomViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivRoomImage;
        private TextView tvTitle, tvAddress, tvPrice, tvArea, tvViews;
        private Chip chipStatus, chipRoomType;
        private ImageButton btnEdit, btnDelete;
        
        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvViews = itemView.findViewById(R.id.tvViews);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            chipRoomType = itemView.findViewById(R.id.chipRoomType);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            
            
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onEditRoom(rooms.get(position));
                    }
                }
            });
            
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteRoom(rooms.get(position));
                    }
                }
            });
        }
        
        public void bind(Room room) {
            tvTitle.setText(room.getTitle());
            
            if (room.getAddress() != null) {
                String address = "";
                if (room.getAddress().getStreet() != null && !room.getAddress().getStreet().isEmpty()) {
                    address += room.getAddress().getStreet() + ", ";
                }
                if (room.getAddress().getWard() != null && !room.getAddress().getWard().isEmpty()) {
                    address += room.getAddress().getWard() + ", ";
                }
                if (room.getAddress().getDistrict() != null && !room.getAddress().getDistrict().isEmpty()) {
                    address += room.getAddress().getDistrict() + ", ";
                }
                if (room.getAddress().getCity() != null && !room.getAddress().getCity().isEmpty()) {
                    address += room.getAddress().getCity();
                }
                if (address.endsWith(", ")) {
                    address = address.substring(0, address.length() - 2);
                }
                tvAddress.setText(address);
            }
            
            if (room.getPrice() != null) {
                NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
                String price = formatter.format(room.getPrice().getMonthly()) + " VNĐ/tháng";
                tvPrice.setText(price);
            }
            
            tvArea.setText(room.getArea() + " m²");
            
            tvViews.setText(room.getViews() + " lượt xem");
            
            if (room.getRoomType() != null) {
                chipRoomType.setText(room.getRoomType());
            }
            
            String status = room.getStatus();
            if (status == null) status = "active";
            
            switch (status.toLowerCase()) {
                case "active":
                    chipStatus.setText("Đang hoạt động");
                    chipStatus.setChipBackgroundColorResource(R.color.success);
                    break;
                case "inactive":
                    chipStatus.setText("Tạm dừng");
                    chipStatus.setChipBackgroundColorResource(R.color.warning);
                    break;
                case "rented":
                    chipStatus.setText("Đã cho thuê");
                    chipStatus.setChipBackgroundColorResource(R.color.info);
                    break;
                case "maintenance":
                    chipStatus.setText("Bảo trì");
                    chipStatus.setChipBackgroundColorResource(R.color.error);
                    break;
                default:
                    chipStatus.setText("Không xác định");
                    chipStatus.setChipBackgroundColorResource(R.color.surface_variant);
                    break;
            }
            
            
            if (room.getImages() != null && !room.getImages().isEmpty()) {
                String imageUrl = ImageUtils.resolveImageUrl(room.getImages().get(0).getUrl());

                if (imageUrl != null) {
                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_room_placeholder)
                            .error(R.drawable.ic_room_placeholder)
                            .centerCrop()
                            .into(ivRoomImage);
                } else {
                    ivRoomImage.setImageResource(R.drawable.ic_room_placeholder);
                }
            } else {
                ivRoomImage.setImageResource(R.drawable.ic_room_placeholder);
            }
        }
    }
}

