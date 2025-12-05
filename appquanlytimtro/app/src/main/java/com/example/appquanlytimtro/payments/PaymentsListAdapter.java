//adapter: cầu nối giữa dữ liệu và giao diện hiển thị
// Mục đích file: File này dùng để hiển thị danh sách thanh toán
// function: 
// - PaymentsListAdapter(): Khởi tạo adapter với danh sách thanh toán
// - onCreateViewHolder(): Tạo ViewHolder cho item thanh toán
// - onBindViewHolder(): Bind dữ liệu thanh toán vào ViewHolder
// - getItemCount(): Trả về số lượng thanh toán
// - VH(): ViewHolder chứa các view con
// - bind(): Hiển thị thông tin thanh toán
package com.example.appquanlytimtro.payments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;

import java.util.List;
import java.util.Map;

public class PaymentsListAdapter extends RecyclerView.Adapter<PaymentsListAdapter.VH> {
    private final List<Map<String, Object>> items;

    public PaymentsListAdapter(List<Map<String, Object>> items) { this.items = items; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Map<String, Object> p = items.get(pos);
        h.tvType.setText(String.valueOf(p.get("type")));
        h.tvAmount.setText(String.valueOf(p.get("amount")));
        h.tvStatus.setText(String.valueOf(p.get("status")));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvType, tvAmount, tvStatus;
        VH(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.chipStatus);
        }
    }
}


