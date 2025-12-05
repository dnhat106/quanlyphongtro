//adapter: cầu nối giữa dữ liệu và giao diện hiển thị
// Mục đích file: File này dùng để quản lý danh sách booking cho chủ trọ
// function: 
// - LandlordBookingAdapter(): Khởi tạo adapter với danh sách booking và listener
// - onCreateViewHolder(): Tạo ViewHolder cho item booking
// - onBindViewHolder(): Bind dữ liệu booking vào ViewHolder
// - getItemCount(): Trả về số lượng booking
// - LandlordBookingViewHolder(): Khởi tạo ViewHolder và tìm các view con
// - bind(): Hiển thị thông tin booking và thiết lập sự kiện click
// - setupActionButtons(): Thiết lập các nút hành động dựa trên trạng thái booking
// - getStatusText(): Chuyển đổi mã trạng thái thành text hiển thị
// - getStatusColor(): Lấy màu sắc tương ứng với trạng thái
package com.example.appquanlytimtro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.Booking;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LandlordBookingAdapter extends RecyclerView.Adapter<LandlordBookingAdapter.LandlordBookingViewHolder> {

    private List<Booking> bookings;
    private OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onConfirmBooking(Booking booking);
        void onCancelBooking(Booking booking);
        void onViewBookingDetails(Booking booking);
        void onAcceptBooking(Booking booking);
        void onRejectBooking(Booking booking);
        void onMarkPaid(Booking booking);
    }

    public LandlordBookingAdapter(List<Booking> bookings, OnBookingActionListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LandlordBookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_landlord_booking, parent, false);
        return new LandlordBookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LandlordBookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking, listener);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class LandlordBookingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRoomTitle;
        private TextView tvTenantName;
        private TextView tvCheckInDate;
        private TextView tvCheckOutDate;
        private TextView tvTotalAmount;
        private TextView tvDuration;
        private Chip chipStatus;
        private MaterialButton btnConfirm;
        private MaterialButton btnCancel;
        private MaterialButton btnViewDetails;
        private MaterialButton btnAccept;
        private MaterialButton btnReject;
        private MaterialButton btnMarkPaid;

        public LandlordBookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomTitle = itemView.findViewById(R.id.tvRoomTitle);
            tvTenantName = itemView.findViewById(R.id.tvTenantName);
            tvCheckInDate = itemView.findViewById(R.id.tvCheckInDate);
            tvCheckOutDate = itemView.findViewById(R.id.tvCheckOutDate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnMarkPaid = itemView.findViewById(R.id.btnMarkPaid);
        }

        public void bind(Booking booking, OnBookingActionListener listener) {
            // Room info
            if (booking.getRoom() != null) {
                tvRoomTitle.setText(booking.getRoom().getTitle());
            }

            // Tenant info
            if (booking.getTenant() != null) {
                tvTenantName.setText("Khách thuê: " + booking.getTenant().getFullName());
            }

            // Booking details
            if (booking.getBookingDetails() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvCheckInDate.setText(sdf.format(booking.getBookingDetails().getCheckInDate()));
                tvCheckOutDate.setText(sdf.format(booking.getBookingDetails().getCheckOutDate()));
                tvDuration.setText(booking.getBookingDetails().getDuration() + " tháng");
            }

            // Pricing
            if (booking.getPricing() != null) {
                NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
                tvTotalAmount.setText(formatter.format(booking.getPricing().getTotalAmount()) + " VNĐ");
            }

            // Status
            chipStatus.setText(getStatusText(booking.getStatus()));
            chipStatus.setChipBackgroundColorResource(getStatusColor(booking.getStatus()));

            // Action buttons based on status
            setupActionButtons(booking, listener);
        }

        private void setupActionButtons(Booking booking, OnBookingActionListener listener) {
            String status = booking.getStatus();
            
            // Reset button visibility
            btnConfirm.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
            btnAccept.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
            btnMarkPaid.setVisibility(View.GONE);
            btnViewDetails.setVisibility(View.VISIBLE);

            switch (status) {
                case "pending":
                    // Show accept/reject buttons for pending bookings
                    btnAccept.setVisibility(View.VISIBLE);
                    btnReject.setVisibility(View.VISIBLE);
                    btnAccept.setText("Chấp nhận");
                    btnReject.setText("Từ chối");
                    break;
                case "confirmed":
                    // Show mark paid button for confirmed bookings
                    btnMarkPaid.setVisibility(View.VISIBLE);
                    btnMarkPaid.setText("Đã thanh toán");
                    btnCancel.setVisibility(View.VISIBLE);
                    btnCancel.setText("Hủy");
                    break;
                case "deposit_paid":
                    // Show cancel button for deposit paid bookings
                    btnCancel.setVisibility(View.VISIBLE);
                    btnCancel.setText("Hủy");
                    break;
                case "active":
                case "completed":
                    // No action buttons for these statuses
                    break;
                case "cancelled":
                    // No action buttons for cancelled bookings
                    break;
            }

            // Set click listeners
            btnConfirm.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConfirmBooking(booking);
                }
            });

            btnCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelBooking(booking);
                }
            });

            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewBookingDetails(booking);
                }
            });

            btnAccept.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptBooking(booking);
                }
            });

            btnReject.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRejectBooking(booking);
                }
            });

            btnMarkPaid.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMarkPaid(booking);
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
    }
}