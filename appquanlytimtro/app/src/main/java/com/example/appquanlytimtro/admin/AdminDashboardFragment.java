//fragment: màn hình dashboard cho admin
// Mục đích file: File này dùng để hiển thị tổng quan thống kê hệ thống cho admin
// function: 
// - onCreateView(): Khởi tạo view và setup các component
// - initViews(): Khởi tạo các view components
// - setupClickListeners(): Thiết lập các sự kiện click
// - loadUserData(): Tải thông tin user hiện tại
// - loadDashboardData(): Tải dữ liệu thống kê dashboard từ API
// - updateDashboardData(): Cập nhật dữ liệu dashboard lên UI
// - loadDefaultData(): Tải dữ liệu mặc định khi không có dữ liệu từ API
package com.example.appquanlytimtro.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.card.MaterialCardView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.models.User;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardFragment extends Fragment {

    private TextView tvTotalUsers;
    private TextView tvTotalLandlords;
    private TextView tvTotalRooms;
    private TextView tvTotalRevenue;
    private MaterialCardView cardLogout;
    
    private RetrofitClient retrofitClient;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        
        retrofitClient = RetrofitClient.getInstance(getContext());
        loadUserData();
        
        initViews(view);
        setupClickListeners();
        loadDashboardData();
        
        return view;
    }

    private void initViews(View view) {
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvTotalLandlords = view.findViewById(R.id.tvTotalLandlords);
        tvTotalRooms = view.findViewById(R.id.tvTotalRooms);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        cardLogout = view.findViewById(R.id.cardLogout);
    }

    private void setupClickListeners() {
        cardLogout.setOnClickListener(v -> {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Call logout method from MainActivity
                    if (getActivity() instanceof com.example.appquanlytimtro.MainActivity) {
                        ((com.example.appquanlytimtro.MainActivity) getActivity()).logout();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }

    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, User.class);
        }
    }

    private void loadDashboardData() {
        if (currentUser == null) return;
        
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().getStatisticsOverview(token).enqueue(new Callback<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Response<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    java.util.Map<String, Object> data = response.body().getData();
                    updateDashboardData(data);
                } else {
                    loadDefaultData();
                }
            }

            @Override
            public void onFailure(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                loadDefaultData();
            }
        });
    }
    
    private void updateDashboardData(java.util.Map<String, Object> data) {
        Object statsObj = data.get("stats");
        if (statsObj instanceof java.util.Map) {
            java.util.Map<String, Object> stats = (java.util.Map<String, Object>) statsObj;
            
            Object usersObj = stats.get("users");
            int totalUsers = 0;
            int totalLandlords = 0;
            if (usersObj instanceof java.util.List) {
                java.util.List<?> usersList = (java.util.List<?>) usersObj;
                for (Object userObj : usersList) {
                    if (userObj instanceof java.util.Map) {
                        java.util.Map<String, Object> user = (java.util.Map<String, Object>) userObj;
                        Object role = user.get("_id");
                        Object count = user.get("count");
                        if (count instanceof Number) {
                            totalUsers += ((Number) count).intValue();
                            if ("landlord".equals(role)) {
                                totalLandlords = ((Number) count).intValue();
                            }
                        }
                    }
                }
            }
            
            if (tvTotalUsers != null) {
                tvTotalUsers.setText(String.valueOf(totalUsers));
            }
            
            if (tvTotalLandlords != null) {
                tvTotalLandlords.setText(String.valueOf(totalLandlords));
            }
            
            Object roomsObj = stats.get("rooms");
            if (roomsObj instanceof java.util.Map) {
                java.util.Map<String, Object> rooms = (java.util.Map<String, Object>) roomsObj;
                Object totalRooms = rooms.get("totalRooms");
                if (tvTotalRooms != null) {
                    if (totalRooms != null) {
                        int roomCount = (int) Math.round(((Number) totalRooms).doubleValue());
                        tvTotalRooms.setText(String.valueOf(roomCount));
                    } else {
                        tvTotalRooms.setText("0");
                    }
                }
            }
            
            Object paymentsObj = stats.get("payments");
            if (paymentsObj instanceof java.util.Map) {
                java.util.Map<String, Object> payments = (java.util.Map<String, Object>) paymentsObj;
                Object totalAmount = payments.get("totalAmount");
                if (tvTotalRevenue != null) {
                    if (totalAmount != null) {
                        tvTotalRevenue.setText(totalAmount.toString() + " VNĐ");
                    } else {
                        tvTotalRevenue.setText("0 VNĐ");
                    }
                }
            }
        }
    }
    
    private void loadDefaultData() {
        tvTotalUsers.setText("0");
        tvTotalLandlords.setText("0");
        tvTotalRooms.setText("0");
        tvTotalRevenue.setText("0 VNĐ");
    }
}
