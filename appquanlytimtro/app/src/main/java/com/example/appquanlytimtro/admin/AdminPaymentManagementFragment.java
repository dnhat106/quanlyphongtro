//fragment: màn hình quản lý thanh toán cho admin
// Mục đích file: File này dùng để quản lý tất cả các thanh toán trong hệ thống cho admin
// function: 
// - onCreateView(): Khởi tạo view và setup các component
// - loadUserData(): Tải thông tin user hiện tại
// - initViews(): Khởi tạo các view components
// - setupRecyclerView(): Thiết lập RecyclerView và adapter
// - handlePaymentAction(): Xử lý các hành động thanh toán
// - confirmPayment(): Xác nhận thanh toán
// - cancelPayment(): Hủy thanh toán
// - refundPayment(): Hoàn tiền
// - setupSwipeRefresh(): Thiết lập chức năng pull-to-refresh
// - loadPayments(): Tải danh sách thanh toán từ API
// - setupFilterChips(): Thiết lập các chip lọc
// - applyFilter(): Áp dụng bộ lọc
// - calculateSummary(): Tính toán tổng kết
// - showLoading(): Hiển thị/ẩn loading indicator
// - showEmptyState(): Hiển thị/ẩn trạng thái empty
package com.example.appquanlytimtro.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.PaymentItemAdapter;
import com.example.appquanlytimtro.models.Payment;
import com.example.appquanlytimtro.models.PaymentItem;
import com.example.appquanlytimtro.models.Booking;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPaymentManagementFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvTotalPaid, tvPendingAmount, tvTotalAmount;
    private LinearLayout emptyState;
    private ChipGroup chipGroupFilter;
    
    private PaymentItemAdapter paymentItemAdapter;
    private List<PaymentItem> paymentItems;
    private List<PaymentItem> allPaymentItems; // Lưu tất cả payment items để filter
    private RetrofitClient retrofitClient;
    private User currentUser;
    private String currentFilter = "all"; // all, pending, completed, failed

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_payment_list, container, false);
        
        retrofitClient = RetrofitClient.getInstance(getContext());
        loadUserData();
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadPayments();
        
        return view;
    }
    
    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, User.class);
        }
    }
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        tvTotalPaid = view.findViewById(R.id.tvTotalPaid);
        tvPendingAmount = view.findViewById(R.id.tvPendingAmount);
        emptyState = view.findViewById(R.id.emptyState);
        
        // Tìm ChipGroup trong layout
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
        
        paymentItems = new ArrayList<>();
        allPaymentItems = new ArrayList<>();
        
        // Setup filter chips cho admin
        setupFilterChips();
    }
    
    private void setupRecyclerView() {
        paymentItems = new ArrayList<>();
        paymentItemAdapter = new PaymentItemAdapter(paymentItems, new PaymentItemAdapter.OnPaymentItemClickListener() {
            @Override
            public void onPaymentItemClick(PaymentItem paymentItem) {
                // Handle payment item click
            }
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(paymentItemAdapter);
    }
    
    private void handlePaymentAction(PaymentItem paymentItem, String action) {
        if (!paymentItem.isPayment()) {
            Toast.makeText(getContext(), "Chỉ có thể thực hiện thao tác trên thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }
        
        switch (action) {
            case "confirm":
                confirmPayment(paymentItem);
                break;
            case "cancel":
                cancelPayment(paymentItem);
                break;
            case "refund":
                refundPayment(paymentItem);
                break;
            default:
                Toast.makeText(getContext(), "Action: " + action + " for payment: " + paymentItem.getId(), Toast.LENGTH_SHORT).show();
                break;
        }
    }
    
    private void confirmPayment(PaymentItem paymentItem) {
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().confirmPayment(token, paymentItem.getId()).enqueue(new Callback<com.example.appquanlytimtro.models.ApiResponse<Payment>>() {
            @Override
            public void onResponse(Call<com.example.appquanlytimtro.models.ApiResponse<Payment>> call, Response<com.example.appquanlytimtro.models.ApiResponse<Payment>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "Xác nhận thanh toán thành công", Toast.LENGTH_SHORT).show();
                    loadPayments(); // Reload data
                } else {
                    Toast.makeText(getContext(), "Lỗi xác nhận thanh toán", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<com.example.appquanlytimtro.models.ApiResponse<Payment>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void cancelPayment(PaymentItem paymentItem) {
        // Implement cancel payment logic
        Toast.makeText(getContext(), "Hủy thanh toán: " + paymentItem.getId(), Toast.LENGTH_SHORT).show();
    }
    
    private void refundPayment(PaymentItem paymentItem) {
        // Implement refund payment logic
        Toast.makeText(getContext(), "Hoàn tiền: " + paymentItem.getId(), Toast.LENGTH_SHORT).show();
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadPayments);
    }
    
    private void loadPayments() {
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        // Admin có thể xem tất cả payments trong hệ thống
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("limit", "100"); // Lấy tối đa 100 payments
        params.put("page", "1");
        params.put("sort", "createdAt"); // Sắp xếp theo ngày tạo
        params.put("order", "desc"); // Mới nhất trước
        
        
        retrofitClient.getApiService().getPayments(token, params).enqueue(new Callback<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Response<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                
                if (response.isSuccessful() && response.body() != null) {
                    
                    if (response.body().isSuccess()) {
                        java.util.Map<String, Object> data = response.body().getData();
                    
                    if (data != null) {
                        try {
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            
                            // Parse payments
                            List<Payment> paymentList = null;
                            if (data.containsKey("payments")) {
                                String paymentsJson = gson.toJson(data.get("payments"));
                                paymentList = gson.fromJson(paymentsJson, new com.google.gson.reflect.TypeToken<List<Payment>>(){}.getType());
                            }
                            
                            // Parse unpaid bookings
                            List<Booking> unpaidBookingList = null;
                            if (data.containsKey("unpaidBookings")) {
                                String bookingsJson = gson.toJson(data.get("unpaidBookings"));
                                unpaidBookingList = gson.fromJson(bookingsJson, new com.google.gson.reflect.TypeToken<List<Booking>>(){}.getType());
                            }
                            
                            
                            // Tạo PaymentItem list từ payments và bookings
                            List<PaymentItem> allItems = new ArrayList<>();
                            
                            // Thêm payments
                            if (paymentList != null) {
                                for (Payment payment : paymentList) {
                                    allItems.add(new PaymentItem(payment));
                                }
                            }
                            
                            // Thêm unpaid bookings
                            if (unpaidBookingList != null) {
                                for (Booking booking : unpaidBookingList) {
                                    allItems.add(new PaymentItem(booking));
                                }
                            }
                            
                            // Lưu tất cả items để filter
                            allPaymentItems.clear();
                            allPaymentItems.addAll(allItems);
                            
                            // Apply current filter
                            applyFilter(currentFilter);
                            
                            // Calculate summary
                            calculateSummary(allItems);
                            
                            // Show/hide empty state
                            if (paymentItems.isEmpty()) {
                                showEmptyState(true);
                            } else {
                                showEmptyState(false);
                            }
                            
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Lỗi xử lý dữ liệu thanh toán", Toast.LENGTH_SHORT).show();
                            showEmptyState(true);
                        }
                    } else {
                        showEmptyState(true);
                    }
                    } else {
                        Toast.makeText(getContext(), "Lỗi API: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        showEmptyState(true);
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu thanh toán (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            }
            
            @Override
            public void onFailure(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }
    
    private void setupFilterChips() {
        if (chipGroupFilter == null) return;
        
        // Hiển thị ChipGroup cho admin
        chipGroupFilter.setVisibility(View.VISIBLE);
        
        // Tạo chips cho các filter
        String[] filterOptions = {"Tất cả", "Chưa thanh toán", "Đã thanh toán", "Thất bại"};
        String[] filterValues = {"all", "pending", "completed", "failed"};
        
        for (int i = 0; i < filterOptions.length; i++) {
            Chip chip = new Chip(getContext());
            chip.setText(filterOptions[i]);
            chip.setCheckable(true);
            chip.setChecked(i == 0); // Mặc định chọn "Tất cả"
            
            final String filterValue = filterValues[i];
            chip.setOnClickListener(v -> {
                // Uncheck all other chips
                for (int j = 0; j < chipGroupFilter.getChildCount(); j++) {
                    Chip otherChip = (Chip) chipGroupFilter.getChildAt(j);
                    otherChip.setChecked(false);
                }
                // Check current chip
                chip.setChecked(true);
                
                // Apply filter
                currentFilter = filterValue;
                applyFilter(filterValue);
            });
            
            chipGroupFilter.addView(chip);
        }
    }
    
    private void applyFilter(String filter) {
        paymentItems.clear();
        
        if (allPaymentItems == null) return;
        
        switch (filter) {
            case "all":
                paymentItems.addAll(allPaymentItems);
                break;
            case "pending":
                for (PaymentItem item : allPaymentItems) {
                    if (item.isBooking() || "pending".equals(item.getStatus())) {
                        paymentItems.add(item);
                    }
                }
                break;
            case "completed":
                for (PaymentItem item : allPaymentItems) {
                    if (item.isPayment() && "completed".equals(item.getStatus())) {
                        paymentItems.add(item);
                    }
                }
                break;
            case "failed":
                for (PaymentItem item : allPaymentItems) {
                    if (item.isPayment() && "failed".equals(item.getStatus())) {
                        paymentItems.add(item);
                    }
                }
                break;
        }
        
        paymentItemAdapter.notifyDataSetChanged();
        
        // Show/hide empty state
        if (paymentItems.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
        }
        
    }
    
    
    private void calculateSummary(List<PaymentItem> paymentItemList) {
        double paidAmount = 0;
        double pendingAmount = 0;
        int totalItems = paymentItemList.size();
        int completedCount = 0;
        int pendingCount = 0;
        
        for (PaymentItem item : paymentItemList) {
            if (item.isBooking()) {
                // Booking chưa thanh toán
                pendingAmount += item.getAmount();
                pendingCount++;
            } else if (item.isPayment()) {
                // Payment đã thanh toán
                if ("completed".equals(item.getStatus())) {
                    paidAmount += item.getAmount();
                    completedCount++;
                } else if ("pending".equals(item.getStatus())) {
                    pendingAmount += item.getAmount();
                    pendingCount++;
                }
            }
        }
        
        // Admin xem tổng quan toàn hệ thống
        tvTotalPaid.setText(String.format("%.0f VNĐ", paidAmount));
        tvPendingAmount.setText(String.format("%.0f VNĐ", pendingAmount));
        
    }
    
    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
    
    private void showEmptyState(boolean show) {
        if (show) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}