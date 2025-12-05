//adapter: cầu nối giữa dữ liệu và giao diện hiển thị
// Mục đích file: File này dùng để hiển thị danh sách thanh toán
// function: 
// - PaymentAdapter(): Khởi tạo adapter với danh sách thanh toán và listener
// - onCreateViewHolder(): Tạo ViewHolder cho item thanh toán
// - onBindViewHolder(): Bind dữ liệu thanh toán vào ViewHolder
// - getItemCount(): Trả về số lượng thanh toán
// - PaymentViewHolder(): Khởi tạo ViewHolder và tìm các view con
// - bind(): Hiển thị thông tin thanh toán và xử lý sự kiện
// - getTypeText(): Chuyển đổi mã loại thanh toán thành text hiển thị
// - getStatusText(): Chuyển đổi mã trạng thái thành text hiển thị
// - getStatusColor(): Lấy màu sắc tương ứng với trạng thái
// - formatDate(): Định dạng ngày tháng hiển thị
package com.example.appquanlytimtro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.Payment;
import com.example.appquanlytimtro.utils.Constants;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {
    
    private List<Payment> payments;
    private OnPaymentClickListener listener;
    private String userRole;
    
    public interface OnPaymentClickListener {
        void onPaymentClick(Payment payment);
        void onPaymentAction(Payment payment, String action);
    }
    
    public PaymentAdapter(List<Payment> payments, OnPaymentClickListener listener, String userRole) {
        this.payments = payments;
        this.listener = listener;
        this.userRole = userRole;
    }
    
    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = payments.get(position);
        holder.bind(payment);
    }
    
    @Override
    public int getItemCount() {
        return payments.size();
    }
    
    class PaymentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAmount, tvType, tvStatus, tvDate, tvDescription;
        
        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvType = itemView.findViewById(R.id.tvType);
            tvStatus = itemView.findViewById(R.id.chipStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
        
        public void bind(Payment payment) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvAmount.setText(formatter.format(payment.getAmount()));
            
            tvType.setText(getTypeText(payment.getType()));
            
            tvStatus.setText(getStatusText(payment.getStatus()));
            tvStatus.setTextColor(getStatusColor(payment.getStatus()));
            
            String dateText = "Ngày tạo: " + formatDate(new java.util.Date());
            tvDate.setText(dateText);
            
            tvDescription.setText(payment.getDescription() != null ? payment.getDescription() : "Không có mô tả");
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaymentClick(payment);
                }
            });
        }
        
        private String getTypeText(String type) {
            switch (type) {
                case "deposit":
                    return "Tiền cọc";
                case "monthly_rent":
                    return "Tiền thuê hàng tháng";
                case "utilities":
                    return "Tiền điện nước";
                case "penalty":
                    return "Phí phạt";
                case "refund":
                    return "Hoàn tiền";
                default:
                    return type;
            }
        }
        
        private String getStatusText(String status) {
            switch (status) {
                case "pending":
                    return "Chờ thanh toán";
                case "completed":
                    return "Đã thanh toán";
                case "failed":
                    return "Thanh toán thất bại";
                case "cancelled":
                    return "Đã hủy";
                default:
                    return status;
            }
        }
        
        private int getStatusColor(String status) {
            switch (status) {
                case "pending":
                    return itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark);
                case "completed":
                    return itemView.getContext().getResources().getColor(android.R.color.holo_green_dark);
                case "failed":
                    return itemView.getContext().getResources().getColor(android.R.color.holo_red_dark);
                case "cancelled":
                    return itemView.getContext().getResources().getColor(android.R.color.darker_gray);
                default:
                    return itemView.getContext().getResources().getColor(android.R.color.darker_gray);
            }
        }
        
        
        private String formatDate(Date date) {
            if (date == null) return "";
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(date);
        }
    }
}
