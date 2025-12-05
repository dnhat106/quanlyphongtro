//fragment: màn hình quản lý đặt phòng cho chủ trọ
// Mục đích file: File này dùng để quản lý các đặt phòng của chủ trọ
// function: 
// - onCreate(): Khởi tạo fragment
// - onCreateView(): Khởi tạo view và setup các component
// - initViews(): Khởi tạo các view components
// - setupRecyclerView(): Thiết lập RecyclerView và adapter
// - setupSwipeRefresh(): Thiết lập chức năng pull-to-refresh
// - setupFilterChips(): Thiết lập các chip lọc theo trạng thái
// - filterBookings(): Lọc booking theo trạng thái
// - loadBookings(): Tải danh sách booking từ API
// - updateEmptyView(): Cập nhật trạng thái empty view
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
// - onConfirmBooking(): Xử lý xác nhận booking
// - onCancelBooking(): Xử lý hủy booking
// - onViewBookingDetails(): Xử lý xem chi tiết booking
// - onAcceptBooking(): Xử lý chấp nhận booking
// - onRejectBooking(): Xử lý từ chối booking
// - checkBookingStatus(): Kiểm tra trạng thái booking trước khi thực hiện hành động
// - updateBookingStatus(): Cập nhật trạng thái booking
package com.example.appquanlytimtro.landlord;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.example.appquanlytimtro.adapters.LandlordBookingAdapter;
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

public class LandlordBookingManagementFragment extends Fragment implements LandlordBookingAdapter.OnBookingActionListener {

    private RetrofitClient retrofitClient;
    private List<Booking> bookings;
    private List<Booking> allBookings; 
    private LandlordBookingAdapter bookingAdapter;
    private String currentFilter = null; 
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private View emptyView;
    private Chip chipAll, chipPending, chipConfirmed, chipPaid, chipCancelled;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_booking_management, container, false);
        
        if (retrofitClient == null) {
            retrofitClient = RetrofitClient.getInstance(requireContext());
        }
        
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
        
        chipAll = view.findViewById(R.id.chipAll);
        chipPending = view.findViewById(R.id.chipPending);
        chipConfirmed = view.findViewById(R.id.chipConfirmed);
        chipPaid = view.findViewById(R.id.chipPaid);
        chipCancelled = view.findViewById(R.id.chipCancelled);
    }

    private void setupRecyclerView() {
        if (bookings == null) {
        bookings = new ArrayList<>();
        }
        if (allBookings == null) {
        allBookings = new ArrayList<>();
        }
        if (recyclerView != null && getContext() != null) {
        bookingAdapter = new LandlordBookingAdapter(bookings, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(bookingAdapter);
        }
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
        swipeRefreshLayout.setOnRefreshListener(this::loadBookings);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        }
    }

    private void setupFilterChips() {
        if (chipAll != null) {
        chipAll.setOnClickListener(v -> filterBookings(null));
        }
        if (chipPending != null) {
        chipPending.setOnClickListener(v -> filterBookings("pending"));
        }
        if (chipConfirmed != null) {
        chipConfirmed.setOnClickListener(v -> filterBookings("confirmed"));
        }
        if (chipPaid != null) {
        chipPaid.setOnClickListener(v -> filterBookings("deposit_paid"));
        }
        if (chipCancelled != null) {
        chipCancelled.setOnClickListener(v -> filterBookings("cancelled"));
        }
    }

    private void filterBookings(String status) {
        currentFilter = status;
        
        if (chipAll != null) {
        chipAll.setChecked(status == null);
        }
        if (chipPending != null) {
        chipPending.setChecked("pending".equals(status));
        }
        if (chipConfirmed != null) {
        chipConfirmed.setChecked("confirmed".equals(status));
        }
        if (chipPaid != null) {
        chipPaid.setChecked("deposit_paid".equals(status));
        }
        if (chipCancelled != null) {
        chipCancelled.setChecked("cancelled".equals(status));
        }
        
        if (bookings == null) {
            bookings = new ArrayList<>();
        }
        bookings.clear();
        
        if (allBookings == null) {
            allBookings = new ArrayList<>();
        }
        
        if (status == null) {
            // Hiển thị tất cả bookings
            bookings.addAll(allBookings);
        } else {
            // Filter theo status
            for (Booking booking : allBookings) {
                if (booking != null && booking.getStatus() != null) {
                    String bookingStatus = booking.getStatus().trim();
                    if (status.equals(bookingStatus)) {
                    bookings.add(booking);
                    }
                }
            }
        }
        
        if (bookingAdapter != null) {
        bookingAdapter.notifyDataSetChanged();
        }
        updateEmptyView();
    }

    private void loadBookings() {
        if (retrofitClient == null || getContext() == null) {
            return;
        }
        
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        java.util.Map<String, String> params = new java.util.HashMap<>();
        // Tăng limit để lấy tất cả bookings
        params.put("limit", "100");
        params.put("page", "1");
        params.put("sortBy", "createdAt");
        params.put("sortOrder", "desc");
        retrofitClient.getApiService().getBookings(token, params).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (getContext() == null || getActivity() == null) {
                    return;
                }
                showLoading(false);
                if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, Object>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Map<String, Object> data = apiResponse.getData();
                        List<?> bookingsData = (List<?>) data.get("bookings");
                        
                        if (bookingsData != null) {
                            if (allBookings == null) {
                                allBookings = new ArrayList<>();
                            }
                            allBookings.clear();
                            Gson gson = new Gson();
                            for (Object bookingObj : bookingsData) {
                                if (bookingObj instanceof Map) {
                                    try {
                                    Booking booking = gson.fromJson(gson.toJson(bookingObj), Booking.class);
                                        // Đảm bảo booking có status hợp lệ
                                        if (booking != null && booking.getStatus() != null) {
                                    allBookings.add(booking);
                                }
                                    } catch (Exception e) {
                                        // Bỏ qua booking không parse được
                                        e.printStackTrace();
                                    }
                                }
                            }
                            
                            // Áp dụng filter hiện tại
                            filterBookings(currentFilter);
                        } else {
                            if (allBookings == null) {
                                allBookings = new ArrayList<>();
                            }
                            allBookings.clear();
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
                if (getContext() == null || getActivity() == null) {
                    return;
                }
                showLoading(false);
                if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
                }
                showError("Lỗi kết nối. Vui lòng thử lại.");
            }
        });
    }

    private void updateEmptyView() {
        if (bookings == null) {
            bookings = new ArrayList<>();
        }
        if (emptyView != null && recyclerView != null) {
        if (bookings.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (show) {
            if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
            }
            if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
            }
        }
    }

    private void showError(String message) {
        Context context = getContext();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConfirmBooking(Booking booking) {
        updateBookingStatus(booking.getId(), "confirmed");
    }

    @Override
    public void onCancelBooking(Booking booking) {
        updateBookingStatus(booking.getId(), "cancelled");
    }

    @Override
    public void onViewBookingDetails(Booking booking) {
    }

    @Override
    public void onAcceptBooking(Booking booking) {
        checkBookingStatus(booking.getId(), "confirmed", "Bạn có chắc chắn muốn chấp nhận đặt phòng này?");
    }

    @Override
    public void onRejectBooking(Booking booking) {
        checkBookingStatus(booking.getId(), "cancelled", "Bạn có chắc chắn muốn từ chối đặt phòng này?");
    }

    @Override
    public void onMarkPaid(Booking booking) {
        checkBookingStatus(booking.getId(), "deposit_paid", "Bạn có chắc chắn đã nhận được thanh toán từ khách thuê?");
    }

    private void checkBookingStatus(String bookingId, String newStatus, String confirmMessage) {
        if (retrofitClient == null || getContext() == null) {
            return;
        }
        
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        retrofitClient.getApiService().getBookingStatus(token, bookingId).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (getContext() == null || getActivity() == null) {
                    return;
                }
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, Object>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Map<String, Object> data = apiResponse.getData();
                        String currentStatus = (String) data.get("currentStatus");
                        Boolean canBeCancelled = (Boolean) data.get("canBeCancelled");
                        Boolean canBeConfirmed = (Boolean) data.get("canBeConfirmed");
                        Boolean canBePaid = (Boolean) data.get("canBePaid");
                        
                        boolean canPerformAction = false;
                        if (newStatus.equals("confirmed")) {
                            canPerformAction = canBeConfirmed;
                        } else if (newStatus.equals("cancelled")) {
                            canPerformAction = canBeCancelled;
                        } else if (newStatus.equals("deposit_paid")) {
                            canPerformAction = canBePaid != null ? canBePaid : "confirmed".equals(currentStatus);
                        }
                        
                        if (canPerformAction) {
                            Context context = getContext();
                            if (context != null) {
                                new androidx.appcompat.app.AlertDialog.Builder(context)
                                    .setTitle("Xác nhận")
                                    .setMessage(confirmMessage)
                                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                                        updateBookingStatus(bookingId, newStatus);
                                    })
                                    .setNegativeButton("Hủy", null)
                                    .show();
                            }
                        } else {
                            String errorMessage = "Không thể thực hiện hành động này. Trạng thái hiện tại: " + currentStatus;
                            if (newStatus.equals("confirmed")) {
                                errorMessage += ". Chỉ có thể xác nhận booking đang ở trạng thái pending.";
                            } else if (newStatus.equals("cancelled")) {
                                errorMessage += ". Chỉ có thể hủy booking ở trạng thái: pending, confirmed, deposit_paid.";
                            } else if (newStatus.equals("deposit_paid")) {
                                errorMessage += ". Chỉ có thể đánh dấu đã thanh toán cho booking đã xác nhận.";
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
                if (getContext() == null || getActivity() == null) {
                    return;
                }
                showLoading(false);
                showError("Lỗi kết nối: " + (t != null ? t.getMessage() : "Unknown error"));
            }
        });
    }

    private void updateBookingStatus(String bookingId, String newStatus) {
        if (retrofitClient == null || getContext() == null) {
            return;
        }
        
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        java.util.Map<String, String> statusUpdate = new java.util.HashMap<>();
        statusUpdate.put("status", newStatus);
        
        retrofitClient.getApiService().updateBookingStatus(token, bookingId, statusUpdate).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                if (getContext() == null || getActivity() == null) {
                    return;
                }
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        String message;
                        switch (newStatus) {
                            case "confirmed":
                                message = "Chấp nhận đặt phòng thành công";
                                break;
                            case "cancelled":
                                message = "Từ chối đặt phòng thành công";
                                break;
                            case "deposit_paid":
                                message = "Đánh dấu đã thanh toán thành công";
                                break;
                            default:
                                message = "Cập nhật trạng thái thành công";
                        }
                        showError(message);
                        loadBookings(); 
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể cập nhật trạng thái đặt phòng");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                if (getContext() == null || getActivity() == null) {
                    return;
                }
                showLoading(false);
                showError("Lỗi kết nối: " + (t != null ? t.getMessage() : "Unknown error"));
            }
        });
    }
}