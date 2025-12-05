//fragment: màn hình quản lý đặt phòng cho admin
// Mục đích file: File này dùng để quản lý tất cả các đặt phòng trong hệ thống cho admin
// function: 
// - onCreateView(): Khởi tạo view và setup các component
// - initViews(): Khởi tạo các view components
// - setupRecyclerView(): Thiết lập RecyclerView và adapter
// - setupSwipeRefresh(): Thiết lập chức năng pull-to-refresh
// - setupFilterChips(): Thiết lập các chip lọc theo trạng thái
// - loadBookings(): Tải danh sách booking từ API
// - filterBookings(): Lọc booking theo trạng thái
// - updateEmptyView(): Cập nhật trạng thái empty view
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
// - onViewBookingDetails(): Xử lý xem chi tiết booking
// - onDeleteBooking(): Xử lý xóa booking
// - onAcceptBooking(): Xử lý chấp nhận booking
// - onRejectBooking(): Xử lý từ chối booking
// - checkBookingStatus(): Kiểm tra trạng thái booking trước khi thực hiện hành động
// - updateBookingStatus(): Cập nhật trạng thái booking
// - deleteBooking(): Xóa booking
package com.example.appquanlytimtro.admin;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.chip.Chip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.AdminBookingAdapter;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Booking;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminBookingManagementFragment extends Fragment implements AdminBookingAdapter.OnBookingActionListener {

    private RetrofitClient retrofitClient;
    private List<Booking> bookings;
    private List<Booking> allBookings; // Store all bookings for filtering
    private AdminBookingAdapter bookingAdapter;
    private String currentFilter = null; // Current filter status
    
    // Views
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private View emptyView;
    private Chip chipAll, chipPending, chipConfirmed, chipPaid, chipActive, chipCompleted, chipCancelled;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrofitClient = RetrofitClient.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_booking_management, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupFilterChips();
        
        loadBookings();
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewBookings);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        
        // Filter chips
        chipAll = view.findViewById(R.id.chipAll);
        chipPending = view.findViewById(R.id.chipPending);
        chipConfirmed = view.findViewById(R.id.chipConfirmed);
        chipPaid = view.findViewById(R.id.chipPaid);
        chipActive = view.findViewById(R.id.chipActive);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        chipCancelled = view.findViewById(R.id.chipCancelled);
    }

    private void setupRecyclerView() {
        bookings = new ArrayList<>();
        allBookings = new ArrayList<>();
        bookingAdapter = new AdminBookingAdapter(bookings, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(bookingAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadBookings);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> filterBookings(null));
        chipPending.setOnClickListener(v -> filterBookings("pending"));
        chipConfirmed.setOnClickListener(v -> filterBookings("confirmed"));
        chipPaid.setOnClickListener(v -> filterBookings("deposit_paid"));
        chipActive.setOnClickListener(v -> filterBookings("active"));
        chipCompleted.setOnClickListener(v -> filterBookings("completed"));
        chipCancelled.setOnClickListener(v -> filterBookings("cancelled"));
    }

    private void filterBookings(String status) {
        currentFilter = status;
        
        // Update chip states
        chipAll.setChecked(status == null);
        chipPending.setChecked("pending".equals(status));
        chipConfirmed.setChecked("confirmed".equals(status));
        chipPaid.setChecked("deposit_paid".equals(status));
        chipActive.setChecked("active".equals(status));
        chipCompleted.setChecked("completed".equals(status));
        chipCancelled.setChecked("cancelled".equals(status));
        
        // Filter bookings
        bookings.clear();
        if (status == null) {
            bookings.addAll(allBookings);
        } else {
            for (Booking booking : allBookings) {
                if (status.equals(booking.getStatus())) {
                    bookings.add(booking);
                }
            }
        }
        
        bookingAdapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void loadBookings() {
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        java.util.Map<String, String> params = new java.util.HashMap<>();
        retrofitClient.getApiService().getBookings(token, params).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, Object>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Map<String, Object> data = apiResponse.getData();
                        List<?> bookingsData = (List<?>) data.get("bookings");
                        
                        if (bookingsData != null) {
                            allBookings.clear();
                            Gson gson = new Gson();
                            for (Object bookingObj : bookingsData) {
                                if (bookingObj instanceof Map) {
                                    Booking booking = gson.fromJson(gson.toJson(bookingObj), Booking.class);
                                    allBookings.add(booking);
                                }
                            }
                            
                            // Apply current filter
                            filterBookings(currentFilter);
                        }
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể tải danh sách đặt phòng");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                showError("Lỗi kết nối. Vui lòng thử lại.");
            }
        });
    }

    private void updateEmptyView() {
        if (bookings.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Context context = getContext();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewBookingDetails(Booking booking) {
        // Navigate to booking detail
        // Intent intent = new Intent(getContext(), BookingDetailActivity.class);
        // intent.putExtra("booking_id", booking.getId());
        // startActivity(intent);
    }

    @Override
    public void onDeleteBooking(Booking booking) {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa đặt phòng này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteBooking(booking.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onAcceptBooking(Booking booking) {
        // Check current status first
        checkBookingStatus(booking.getId(), "confirmed", "Bạn có chắc chắn muốn chấp nhận đặt phòng này?");
    }

    @Override
    public void onRejectBooking(Booking booking) {
        // Check current status first
        checkBookingStatus(booking.getId(), "cancelled", "Bạn có chắc chắn muốn từ chối đặt phòng này?");
    }

    private void checkBookingStatus(String bookingId, String newStatus, String confirmMessage) {
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        retrofitClient.getApiService().getBookingStatus(token, bookingId).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, Object>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Map<String, Object> data = apiResponse.getData();
                        String currentStatus = (String) data.get("currentStatus");
                        Boolean canBeCancelled = (Boolean) data.get("canBeCancelled");
                        Boolean canBeConfirmed = (Boolean) data.get("canBeConfirmed");
                        
                        // Check if action is allowed
                        boolean canPerformAction = false;
                        if (newStatus.equals("confirmed")) {
                            canPerformAction = canBeConfirmed;
                        } else if (newStatus.equals("cancelled")) {
                            canPerformAction = canBeCancelled;
                        }
                        
                        if (canPerformAction) {
                            // Show confirmation dialog
                            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                    .setTitle("Xác nhận")
                                    .setMessage(confirmMessage)
                                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                                        updateBookingStatus(bookingId, newStatus);
                                    })
                                    .setNegativeButton("Hủy", null)
                                    .show();
                        } else {
                            String errorMessage = "Không thể thực hiện hành động này. Trạng thái hiện tại: " + currentStatus;
                            if (newStatus.equals("confirmed")) {
                                errorMessage += ". Chỉ có thể xác nhận booking đang ở trạng thái pending.";
                            } else if (newStatus.equals("cancelled")) {
                                errorMessage += ". Chỉ có thể hủy booking ở trạng thái: pending, confirmed, deposit_paid.";
                            }
                            showError(errorMessage);
                        }
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể kiểm tra trạng thái booking");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void updateBookingStatus(String bookingId, String newStatus) {
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        java.util.Map<String, String> statusUpdate = new java.util.HashMap<>();
        statusUpdate.put("status", newStatus);
        
        retrofitClient.getApiService().updateBookingStatus(token, bookingId, statusUpdate).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        String message = newStatus.equals("confirmed") ? "Chấp nhận đặt phòng thành công" : "Từ chối đặt phòng thành công";
                        showError(message);
                        loadBookings(); // Reload list
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể cập nhật trạng thái đặt phòng");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void deleteBooking(String bookingId) {
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        retrofitClient.getApiService().deleteBooking(token, bookingId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        showError("Xóa đặt phòng thành công");
                        loadBookings(); // Reload list
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể xóa đặt phòng");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}