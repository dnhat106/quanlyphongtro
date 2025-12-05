//activity: màn hình danh sách thanh toán
// Mục đích file: File này dùng để hiển thị danh sách các thanh toán của người dùng
// function: 
// - onCreate(): Khởi tạo activity và setup các component
// - initViews(): Khởi tạo các view components
// - setupToolbar(): Thiết lập toolbar với menu
// - setupRecyclerView(): Thiết lập RecyclerView và adapter
// - setupSwipeRefresh(): Thiết lập chức năng pull-to-refresh
// - loadPayments(): Tải danh sách thanh toán từ API
// - updateEmptyView(): Cập nhật trạng thái empty view
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
// - onPaymentClick(): Xử lý click vào thanh toán
// - onCreateOptionsMenu(): Tạo menu options
// - onOptionsItemSelected(): Xử lý click vào menu item
package com.example.appquanlytimtro.payments;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.PaymentAdapter;
import com.example.appquanlytimtro.models.Payment;
import com.example.appquanlytimtro.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvTotalPaid, tvPendingAmount;
    private LinearLayout emptyState;
    
    private PaymentAdapter paymentAdapter;
    private List<Payment> payments = new ArrayList<>();
    private RetrofitClient retrofitClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_list);
        
        retrofitClient = RetrofitClient.getInstance(this);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        loadPayments();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        tvTotalPaid = findViewById(R.id.tvTotalPaid);
        tvPendingAmount = findViewById(R.id.tvPendingAmount);
        emptyState = findViewById(R.id.emptyState);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lịch sử thanh toán");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupRecyclerView() {
        // Get current user role
        String userJson = retrofitClient.getUserData();
        String userRole = "tenant"; // default
        if (userJson != null) {
            try {
                com.example.appquanlytimtro.models.User currentUser = new com.google.gson.Gson().fromJson(userJson, com.example.appquanlytimtro.models.User.class);
                if (currentUser != null && currentUser.getRole() != null) {
                    userRole = currentUser.getRole();
                }
            } catch (Exception e) {
            }
        }
        
        paymentAdapter = new PaymentAdapter(payments, new PaymentAdapter.OnPaymentClickListener() {
            @Override
            public void onPaymentClick(Payment payment) {
                PaymentListActivity.this.onPaymentClick(payment);
            }
            
            @Override
            public void onPaymentAction(Payment payment, String action) {
                // Handle payment actions if needed
                Toast.makeText(PaymentListActivity.this, "Action: " + action + " for payment: " + payment.getId(), Toast.LENGTH_SHORT).show();
            }
        }, userRole);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(paymentAdapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadPayments);
    }
    
    private void loadPayments() {
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        
        // Get current user ID
        String userJson = retrofitClient.getUserData();
        if (userJson == null) {
            showLoading(false);
            Toast.makeText(this, "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        com.example.appquanlytimtro.models.User currentUser = new com.google.gson.Gson().fromJson(userJson, com.example.appquanlytimtro.models.User.class);
        if (currentUser == null || currentUser.getId() == null) {
            showLoading(false);
            Toast.makeText(this, "Thông tin người dùng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Use the general payments endpoint instead of user-specific endpoint
        retrofitClient.getApiService().getPayments(token, new java.util.HashMap<>()).enqueue(new Callback<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Response<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    java.util.Map<String, Object> data = response.body().getData();
                    
                    if (data != null && data.containsKey("payments")) {
                        try {
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            String paymentsJson = gson.toJson(data.get("payments"));
                            List<Payment> paymentList = gson.fromJson(paymentsJson, new com.google.gson.reflect.TypeToken<List<Payment>>(){}.getType());
                            
                            
                            if (paymentList != null) {
                                // Filter payments - chỉ hiển thị payments từ booking đã confirmed
                                List<Payment> filteredPayments = filterValidPayments(paymentList);
                                
                                payments.clear();
                                payments.addAll(filteredPayments);
                                paymentAdapter.notifyDataSetChanged();
                                
                                // Calculate summary
                                calculateSummary(filteredPayments);
                                
                                // Show/hide empty state
                                if (payments.isEmpty()) {
                                    showEmptyState(true);
                                } else {
                                    showEmptyState(false);
                                }
                            } else {
                                showEmptyState(true);
                            }
                        } catch (Exception e) {
                            Toast.makeText(PaymentListActivity.this, "Lỗi xử lý dữ liệu thanh toán", Toast.LENGTH_SHORT).show();
                            showEmptyState(true);
                        }
                    } else {
                        showEmptyState(true);
                    }
                } else {
                    Toast.makeText(PaymentListActivity.this, "Lỗi tải dữ liệu thanh toán", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            }
            
            @Override
            public void onFailure(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(PaymentListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }
    
    private List<Payment> filterValidPayments(List<Payment> paymentList) {
        List<Payment> validPayments = new ArrayList<>();
        
        for (Payment payment : paymentList) {
            if (payment == null) continue;
            
            if (payment.getBooking() == null) {
                validPayments.add(payment);
                continue;
            }

            String bookingStatus = payment.getBooking().getStatus();
            if ("confirmed".equals(bookingStatus)
                    || "deposit_paid".equals(bookingStatus)
                    || "active".equals(bookingStatus)
                    || "completed".equals(bookingStatus)) {
                validPayments.add(payment);
            }
        }
        
        return validPayments;
    }
    
    private void calculateSummary(List<Payment> paymentList) {
        double totalPaid = 0;
        double pendingAmount = 0;
        
        for (Payment payment : paymentList) {
            if ("completed".equals(payment.getStatus())) {
                totalPaid += payment.getAmount();
            } else if ("pending".equals(payment.getStatus())) {
                pendingAmount += payment.getAmount();
            }
        }
        
        tvTotalPaid.setText(String.format("%.0f VNĐ", totalPaid));
        tvPendingAmount.setText(String.format("%.0f VNĐ", pendingAmount));
    }
    
    private void onPaymentClick(Payment payment) {
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}