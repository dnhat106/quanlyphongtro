//adapter: cầu nối giữa dữ liệu và giao diện hiển thị
// Mục đích file: File này dùng để hiển thị danh sách các đặt phòng cho người dùng trong ứng dụng quản lý tìm trọ
// function: 
// - BookingAdapter(): Khởi tạo adapter với danh sách booking và listener
// - onCreateViewHolder(): Tạo ViewHolder mới cho item booking
// - onBindViewHolder(): Gắn dữ liệu booking vào ViewHolder
// - getItemCount(): Trả về số lượng booking đã lọc
// - updateBookings(): Cập nhật danh sách booking mới
// - filterByStatus(): Lọc booking theo trạng thái
// - BookingViewHolder(): Khởi tạo ViewHolder và tìm các view con
// - bind(): Hiển thị thông tin booking và thiết lập sự kiện click
// - setupActionButtons(): Thiết lập các nút hành động dựa trên trạng thái
// - getStatusText(): Chuyển đổi mã trạng thái thành text hiển thị
// - getStatusColor(): Lấy màu sắc tương ứng với trạng thái
package com.example.appquanlytimtro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.Booking;
import com.example.appquanlytimtro.utils.ImageUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookings;
    private List<Booking> filteredBookings;
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
        void onBookingStatusChange(Booking booking, String newStatus);
        void onPaymentClick(Booking booking);
    }

    public BookingAdapter(List<Booking> bookings, OnBookingClickListener listener) {
        this.bookings = bookings;
        this.filteredBookings = new ArrayList<>(bookings);
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = filteredBookings.get(position);
        holder.bind(booking, listener);
    }

    @Override
    public int getItemCount() {
        return filteredBookings.size();
    }

    public void updateBookings(List<Booking> newBookings) {
        
        List<Booking> bookingsCopy = new ArrayList<>(newBookings);
        
        this.bookings.clear();
        this.bookings.addAll(bookingsCopy);
        this.filteredBookings.clear();
        this.filteredBookings.addAll(bookingsCopy);
        
        notifyDataSetChanged();
    }

    public void filterByStatus(String status) {
        filteredBookings.clear();
        if (status == null) {
            filteredBookings.addAll(bookings);
        } else {
            for (Booking booking : bookings) {
                if (status.equals(booking.getStatus())) {
                    filteredBookings.add(booking);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivRoomImage;
        private TextView tvRoomTitle;
        private TextView tvRoomAddress;
        private TextView tvCheckInDate;
        private TextView tvCheckOutDate;
        private TextView tvTotalAmount;
        private TextView tvDuration;
        private Chip chipStatus;
        private TextView tvLandlordName;
        private LinearLayout layoutActionButtons;
        private MaterialButton btnPayment;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
            tvRoomTitle = itemView.findViewById(R.id.tvRoomTitle);
            tvRoomAddress = itemView.findViewById(R.id.tvRoomAddress);
            tvCheckInDate = itemView.findViewById(R.id.tvCheckInDate);
            tvCheckOutDate = itemView.findViewById(R.id.tvCheckOutDate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            tvLandlordName = itemView.findViewById(R.id.tvLandlordName);
            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
            btnPayment = itemView.findViewById(R.id.btnPayment);
        }

        public void bind(Booking booking, OnBookingClickListener listener) {
            if (booking.getRoom() != null) {
                tvRoomTitle.setText(booking.getRoom().getTitle());

                if (ivRoomImage != null && booking.getRoom().getImages() != null && !booking.getRoom().getImages().isEmpty()) {
                    String imageUrl = ImageUtils.resolveImageUrl(booking.getRoom().getImages().get(0).getUrl());
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
                } else if (ivRoomImage != null) {
                    ivRoomImage.setImageResource(R.drawable.ic_room_placeholder);
                }

                if (booking.getRoom().getAddress() != null) {
                    String address = booking.getRoom().getAddress().getStreet() + ", " +
                                   booking.getRoom().getAddress().getWard() + ", " +
                                   booking.getRoom().getAddress().getDistrict() + ", " +
                                   booking.getRoom().getAddress().getCity();
                    tvRoomAddress.setText(address);
                }
            }

            if (booking.getBookingDetails() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvCheckInDate.setText(sdf.format(booking.getBookingDetails().getCheckInDate()));
                tvCheckOutDate.setText(sdf.format(booking.getBookingDetails().getCheckOutDate()));
                tvDuration.setText(booking.getBookingDetails().getDuration() + " tháng");
            }

            if (booking.getPricing() != null) {
                NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
                tvTotalAmount.setText(formatter.format(booking.getPricing().getTotalAmount()) + " VNĐ");
            }

            if (booking.getLandlord() != null) {
                tvLandlordName.setText("Chủ trọ: " + booking.getLandlord().getFullName());
            }

            chipStatus.setText(getStatusText(booking.getStatus()));
            chipStatus.setChipBackgroundColorResource(getStatusColor(booking.getStatus()));

            setupActionButtons(booking, listener);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookingClick(booking);
                }
            });
        }

        private String getStatusText(String status) {
            switch (status) {
                case "pending": return "Chờ xác nhận";
                case "confirmed": return "Đã xác nhận";
                case "deposit_paid": return "Đã thanh toán";
                case "active": return "Đang hoạt động";
                case "completed": return "Đã hoàn thành";
                case "cancelled": return "Đã hủy";
                default: return status;
            }
        }

        private int getStatusColor(String status) {
            switch (status) {
                case "pending": return R.color.warning;
                case "confirmed": return R.color.info;
                case "deposit_paid": return R.color.success;
                case "active": return R.color.primary;
                case "completed": return R.color.success;
                case "cancelled": return R.color.error;
                default: return R.color.on_surface_variant;
            }
        }

        private void setupActionButtons(Booking booking, OnBookingClickListener listener) {
            String status = booking.getStatus();
            
            layoutActionButtons.setVisibility(View.GONE);
            btnPayment.setVisibility(View.GONE);
        
            if ("confirmed".equals(status)) {
                layoutActionButtons.setVisibility(View.VISIBLE);
                btnPayment.setVisibility(View.VISIBLE);
                btnPayment.setText("Thanh toán cọc");
            }

            btnPayment.setOnClickListener(v -> {
                if (listener != null && "confirmed".equals(status)) {
                    listener.onPaymentClick(booking);
                }
            });
        }
    }
}