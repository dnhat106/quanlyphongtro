//adapter: cầu nối giữa dữ liệu và giao diện hiển thị
// Mục đích file: File này dùng để hiển thị danh sách item thanh toán
// function: 
// - PaymentItemAdapter(): Khởi tạo adapter với danh sách item thanh toán và listener
// - onCreateViewHolder(): Tạo ViewHolder cho item thanh toán
// - onBindViewHolder(): Bind dữ liệu item thanh toán vào ViewHolder
// - getItemCount(): Trả về số lượng item thanh toán
// - PaymentItemViewHolder(): Khởi tạo ViewHolder và tìm các view con
// - bind(): Hiển thị thông tin item thanh toán và xử lý sự kiện
package com.example.appquanlytimtro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.PaymentItem;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentItemAdapter extends RecyclerView.Adapter<PaymentItemAdapter.PaymentItemViewHolder> {
    
    private List<PaymentItem> paymentItems;
    private OnPaymentItemClickListener listener;
    
    public interface OnPaymentItemClickListener {
        void onPaymentItemClick(PaymentItem paymentItem);
    }
    
    public PaymentItemAdapter(List<PaymentItem> paymentItems, OnPaymentItemClickListener listener) {
        this.paymentItems = paymentItems;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public PaymentItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment, parent, false);
        return new PaymentItemViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PaymentItemViewHolder holder, int position) {
        PaymentItem item = paymentItems.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return paymentItems.size();
    }
    
    public class PaymentItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAmount;
        private com.google.android.material.chip.Chip chipStatus;
        private TextView tvPaymentMethod;
        private TextView tvDate;
        private TextView tvType;
        private TextView tvDescription;
        
        public PaymentItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvType = itemView.findViewById(R.id.tvType);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaymentItemClick(paymentItems.get(getAdapterPosition()));
                }
            });
        }
        
            public void bind(PaymentItem item) {
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            tvAmount.setText(formatter.format(item.getAmount()) + " VNĐ");
            
            chipStatus.setText(item.getStatusText());
            if (item.isBooking()) {
                chipStatus.setChipBackgroundColorResource(R.color.warning);
            } else {
                switch (item.getStatus()) {
                    case "completed":
                        chipStatus.setChipBackgroundColorResource(R.color.success);
                        break;
                    case "pending":
                        chipStatus.setChipBackgroundColorResource(R.color.warning);
                        break;
                    case "failed":
                        chipStatus.setChipBackgroundColorResource(R.color.error);
                        break;
                    default:
                        chipStatus.setChipBackgroundColorResource(R.color.text_hint);
                        break;
                }
            }
            
            tvPaymentMethod.setText(item.getPaymentMethodText());
            
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(item.getInitiatedAt());
                tvDate.setText(outputFormat.format(date));
            } catch (Exception e) {
                tvDate.setText(item.getInitiatedAt());
            }
            
            tvType.setText(item.isBooking() ? "Đặt phòng" : "Thanh toán");
            
            String description = "";
            if (item.isBooking()) {
                description = "Đặt phòng chưa thanh toán";
                if (item.getPayer() != null) {
                    description += " - " + item.getPayer().getFullName();
                }
            } else {
                description = "Thanh toán " + item.getType();
                if (item.getPayer() != null && item.getRecipient() != null) {
                    description += " - " + item.getPayer().getFullName() + " → " + item.getRecipient().getFullName();
                }
            }
            tvDescription.setText(description);
        }
    }
}
