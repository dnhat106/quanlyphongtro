//fragment: màn hình dashboard cho chủ trọ
// Mục đích file: File này dùng để hiển thị tổng quan thống kê cho chủ trọ
// function: 
// - onCreateView(): Khởi tạo view và setup các component
// - initViews(): Khởi tạo các view components
// - setupClickListeners(): Thiết lập các sự kiện click
// - loadUserData(): Tải thông tin user hiện tại
// - loadDashboardData(): Tải dữ liệu thống kê dashboard từ API
// - updateDashboardData(): Cập nhật dữ liệu dashboard lên UI
// - onManageRoomsClick(): Xử lý click quản lý phòng
// - onManageBookingsClick(): Xử lý click quản lý đặt phòng
// - onManagePaymentsClick(): Xử lý click quản lý thanh toán
// - onAddRoomClick(): Xử lý click thêm phòng
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
package com.example.appquanlytimtro.landlord;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;

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

public class LandlordDashboardFragment extends Fragment {

    private TextView tvTotalRooms;
    private TextView tvOccupiedRooms;
    private TextView tvTotalRevenue;
    private TextView tvPendingBookings;
    private MaterialCardView cardLogout;
    private MaterialButton btnAddRoom, btnManageRooms, btnViewBookings;
    
    private RetrofitClient retrofitClient;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_dashboard, container, false);
        
        retrofitClient = RetrofitClient.getInstance(getContext());
        loadUserData();
        
        initViews(view);
        setupClickListeners();
        loadDashboardData();
        
        return view;
    }

    private void initViews(View view) {
        tvTotalRooms = view.findViewById(R.id.tvTotalRooms);
        tvOccupiedRooms = view.findViewById(R.id.tvOccupiedRooms);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvPendingBookings = view.findViewById(R.id.tvPendingBookings);
        cardLogout = view.findViewById(R.id.cardLogout);
        btnAddRoom = view.findViewById(R.id.btnAddRoom);
        btnManageRooms = view.findViewById(R.id.btnManageRooms);
        btnViewBookings = view.findViewById(R.id.btnViewBookings);
    }

    private void setupClickListeners() {
        cardLogout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    if (getActivity() instanceof com.example.appquanlytimtro.MainActivity) {
                        ((com.example.appquanlytimtro.MainActivity) getActivity()).logout();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
        
        if (btnAddRoom != null) {
            btnAddRoom.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddRoomActivity.class);
                startActivity(intent);
            });
        }
        
        if (btnManageRooms != null) {
            btnManageRooms.setOnClickListener(v -> {
                if (getActivity() instanceof com.example.appquanlytimtro.MainActivity) {
                    ((com.example.appquanlytimtro.MainActivity) getActivity()).navigateToFragment(1);
                }
            });
        }
        
        if (btnViewBookings != null) {
            btnViewBookings.setOnClickListener(v -> {
                if (getActivity() instanceof com.example.appquanlytimtro.MainActivity) {
                    ((com.example.appquanlytimtro.MainActivity) getActivity()).navigateToFragment(2);
                }
            });
        }
    }

    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, User.class);
        }
    }

    private void loadDashboardData() {
        if (currentUser == null) {
            loadDefaultData();
            return;
        }
        String token = "Bearer " + retrofitClient.getToken();
        retrofitClient.getApiService().getStatisticsOverview(token).enqueue(new Callback<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Response<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    java.util.Map<String, Object> data = response.body().getData();
                    if (data != null && data.containsKey("stats")) {
                        bindStats((java.util.Map<String, Object>) data.get("stats"));
                    } else {
                        loadDefaultData();
                    }
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

    private void bindStats(java.util.Map<String, Object> stats) {
        if (stats == null) {
            loadDefaultData();
            return;
        }
        java.util.Map<String, Object> rooms = stats.containsKey("rooms") && stats.get("rooms") instanceof java.util.Map ? (java.util.Map<String, Object>) stats.get("rooms") : null;
        java.util.Map<String, Object> bookings = stats.containsKey("bookings") && stats.get("bookings") instanceof java.util.Map ? (java.util.Map<String, Object>) stats.get("bookings") : null;
        java.util.Map<String, Object> payments = stats.containsKey("payments") && stats.get("payments") instanceof java.util.Map ? (java.util.Map<String, Object>) stats.get("payments") : null;

        int totalRooms = rooms != null && rooms.get("totalRooms") instanceof Number ? ((Number) rooms.get("totalRooms")).intValue() : 0;
        int activeRooms = rooms != null && rooms.get("activeRooms") instanceof Number ? ((Number) rooms.get("activeRooms")).intValue() : 0;
        int pendingBookings = bookings != null && bookings.get("pendingBookings") instanceof Number ? ((Number) bookings.get("pendingBookings")).intValue() : 0;
        Number revenueNumber = payments != null && payments.get("totalAmount") instanceof Number ? (Number) payments.get("totalAmount") : null;
        String revenueText = revenueNumber != null ? String.format(java.util.Locale.getDefault(), "%.0f VNĐ", revenueNumber.doubleValue()) : "0 VNĐ";

        tvTotalRooms.setText(String.valueOf(totalRooms));
        tvOccupiedRooms.setText(String.valueOf(activeRooms));
        tvPendingBookings.setText(String.valueOf(pendingBookings));
        tvTotalRevenue.setText(revenueText);
    }
    
    private void loadDefaultData() {
        tvTotalRooms.setText("0");
        tvOccupiedRooms.setText("0");
        tvTotalRevenue.setText("0 VNĐ");
        tvPendingBookings.setText("0");
    }
}
