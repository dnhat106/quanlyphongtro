//fragment: màn hình chính cho người thuê trọ
// Mục đích file: File này dùng để hiển thị màn hình chính cho người thuê trọ
// function: 
// - onCreateView(): Khởi tạo view và setup các component
// - initViews(): Khởi tạo các view components
// - setupRecyclerView(): Thiết lập RecyclerView và adapter
// - setupClickListeners(): Thiết lập các sự kiện click
// - loadUserData(): Tải thông tin user hiện tại
// - loadRecentRooms(): Tải danh sách phòng gần đây
// - loadUserBookings(): Tải danh sách đặt phòng của user
// - onSearchRoomsClick(): Xử lý click tìm kiếm phòng
// - onViewAllRoomsClick(): Xử lý click xem tất cả phòng
// - onViewBookingsClick(): Xử lý click xem đặt phòng
// - onRoomClick(): Xử lý click vào phòng
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
package com.example.appquanlytimtro.tenant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.MainActivity;
import com.example.appquanlytimtro.rooms.RoomListActivity;
import com.example.appquanlytimtro.bookings.BookingListActivity;
import com.example.appquanlytimtro.payments.PaymentListActivity;
import com.example.appquanlytimtro.profile.ProfileActivity;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.models.ApiResponse;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TenantHomeFragment extends Fragment {

    private TextView tvWelcome;
    private TextView tvTotalBookings;
    private TextView tvPendingPayments;
    private TextView tvTotalDeposit;
    private TextView tvTotalPaid;
    private MaterialCardView cardSearchRooms;
    private MaterialCardView cardMyBookings;
    private MaterialCardView cardMyPayments;
    private MaterialCardView cardMyProfile;
    private MaterialCardView cardLogout;
    private RecyclerView recyclerViewRecentRooms;
    
    private RetrofitClient retrofitClient;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenant_home, container, false);
        
        retrofitClient = RetrofitClient.getInstance(getContext());
        loadUserData();
        
        initViews(view);
        setupClickListeners();
        loadDashboardStats();
        
        return view;
    }

    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvTotalBookings = view.findViewById(R.id.tvTotalBookings);
        tvPendingPayments = view.findViewById(R.id.tvPendingPayments);
        tvTotalDeposit = view.findViewById(R.id.tvTotalDeposit);
        tvTotalPaid = view.findViewById(R.id.tvTotalPaid);
        cardSearchRooms = view.findViewById(R.id.cardSearchRooms);
        cardMyBookings = view.findViewById(R.id.cardMyBookings);
        cardMyPayments = view.findViewById(R.id.cardMyPayments);
        cardMyProfile = view.findViewById(R.id.cardMyProfile);
        cardLogout = view.findViewById(R.id.cardLogout);
        recyclerViewRecentRooms = view.findViewById(R.id.recyclerViewRecentRooms);
        
        recyclerViewRecentRooms.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, User.class);
            if (currentUser != null) {
                updateWelcomeMessage();
            }
        }
    }
    
    private void updateWelcomeMessage() {
        if (tvWelcome != null && currentUser != null) {
            String welcomeText = "Chào mừng " + currentUser.getFullName() + " đến với Quản Lý Tìm Trọ!";
            tvWelcome.setText(welcomeText);
        }
    }

    private void setupClickListeners() {
        cardSearchRooms.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RoomListActivity.class);
            intent.putExtra("show_available_only", true); // Chỉ hiển thị phòng có sẵn
            startActivity(intent);
        });

        cardMyBookings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BookingListActivity.class);
            startActivity(intent);
        });

        cardMyPayments.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PaymentListActivity.class);
            startActivity(intent);
        });

        cardMyProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        cardLogout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).logout();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }

    private void loadDashboardStats() {
        if (currentUser == null) {
            setDefaultStats();
            return;
        }
        String token = "Bearer " + retrofitClient.getToken();
        retrofitClient.getApiService().getStatisticsOverview(token).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    Map<String, Object> stats = data != null && data.containsKey("stats") ? (Map<String, Object>) data.get("stats") : null;
                    bindStats(stats);
                } else {
                    setDefaultStats();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                setDefaultStats();
            }
        });
    }

    private void bindStats(Map<String, Object> stats) {
        if (stats == null) {
            setDefaultStats();
            return;
        }
        Map<String, Object> bookings = stats.get("bookings") instanceof Map ? (Map<String, Object>) stats.get("bookings") : null;
        Map<String, Object> payments = stats.get("payments") instanceof Map ? (Map<String, Object>) stats.get("payments") : null;

        int totalBookings = bookings != null && bookings.get("totalBookings") instanceof Number ? ((Number) bookings.get("totalBookings")).intValue() : 0;
        int pending = bookings != null && bookings.get("pendingBookings") instanceof Number ? ((Number) bookings.get("pendingBookings")).intValue() : 0;
        double totalDeposit = bookings != null && bookings.get("totalDeposit") instanceof Number ? ((Number) bookings.get("totalDeposit")).doubleValue() : 0;
        double totalPaid = payments != null && payments.get("totalAmount") instanceof Number ? ((Number) payments.get("totalAmount")).doubleValue() : 0;

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());

        tvTotalBookings.setText(String.valueOf(totalBookings));
        tvPendingPayments.setText(String.valueOf(pending));
        tvTotalDeposit.setText(formatter.format(totalDeposit) + " VNĐ");
        tvTotalPaid.setText(formatter.format(totalPaid) + " VNĐ");
    }

    private void setDefaultStats() {
        tvTotalBookings.setText("0");
        tvPendingPayments.setText("0");
        tvTotalDeposit.setText("0 VNĐ");
        tvTotalPaid.setText("0 VNĐ");
    }
}
