//activity: màn hình hiển thị danh sách đặt phòng của người dùng
// Mục đích file: File này dùng để hiển thị danh sách các đặt phòng của người dùng hiện tại
// function: 
// - onCreate(): Khởi tạo activity và setup các component
// - initViews(): Khởi tạo các view components
// - setupToolbar(): Thiết lập toolbar với menu
// - setupRecyclerView(): Thiết lập RecyclerView và adapter
// - setupSwipeRefresh(): Thiết lập chức năng pull-to-refresh
// - loadBookings(): Tải danh sách booking từ API
// - updateEmptyView(): Cập nhật trạng thái empty view
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
// - onBookingClick(): Xử lý click vào booking để xem chi tiết
// - onBookingStatusChange(): Xử lý thay đổi trạng thái booking
// - onPaymentClick(): Xử lý click thanh toán
// - updateBookingStatus(): Cập nhật trạng thái booking
// - onCreateOptionsMenu(): Tạo menu options
// - onOptionsItemSelected(): Xử lý click vào menu item
// - showFilterDialog(): Hiển thị dialog lọc theo trạng thái
// - filterBookingsByStatus(): Lọc booking theo trạng thái
// - convertStatusToValue(): Chuyển đổi text trạng thái thành giá trị
package com.example.appquanlytimtro.bookings;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.BookingAdapter;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Booking;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.payments.PaymentActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingListActivity extends AppCompatActivity implements BookingAdapter.OnBookingClickListener {

    private RetrofitClient retrofitClient;
    private List<Booking> bookings;
    private BookingAdapter bookingAdapter;
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_list);

        retrofitClient = RetrofitClient.getInstance(this);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        
        loadBookings();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewBookings);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Danh sách đặt phòng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        bookings = new ArrayList<>();
        bookingAdapter = new BookingAdapter(bookings, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

    private void loadBookings() {
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        java.util.Map<String, String> params = new java.util.HashMap<>();
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.example.appquanlytimtro.models.User currentUser = gson.fromJson(userJson, com.example.appquanlytimtro.models.User.class);
            if (currentUser != null) {
                params.put("tenantId", currentUser.getId());
            }
        }
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
                            bookings.clear();
                            Gson gson = new Gson();
                            for (Object bookingObj : bookingsData) {
                                if (bookingObj instanceof Map) {
                                    try {
                                        Booking booking = gson.fromJson(gson.toJson(bookingObj), Booking.class);
                                        bookings.add(booking);
                                    } catch (Exception e) {
                                    }
                                }
                            }
                            bookingAdapter.updateBookings(bookings);
                            updateEmptyView();
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
    }

    @Override
    public void onBookingClick(Booking booking) {
    }

    @Override
    public void onBookingStatusChange(Booking booking, String newStatus) {
        updateBookingStatus(booking.getId(), newStatus);
    }

    @Override
    public void onPaymentClick(Booking booking) {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("room_id", booking.getRoom().getId());
        
        if (booking.getLandlord() != null) {
            intent.putExtra("landlord_name", booking.getLandlord().getFullName());
            intent.putExtra("landlord_phone", booking.getLandlord().getPhone());
        }
        if (booking.getRoom() != null && booking.getRoom().getAddress() != null) {
            com.example.appquanlytimtro.models.User.Address addr = booking.getRoom().getAddress();
            String address = (addr.getStreet() != null ? addr.getStreet() + ", " : "") +
                             (addr.getWard() != null ? addr.getWard() + ", " : "") +
                             (addr.getDistrict() != null ? addr.getDistrict() + ", " : "") +
                             (addr.getCity() != null ? addr.getCity() : "");
            if (address.endsWith(", ")) {
                address = address.substring(0, address.length() - 2);
            }
            intent.putExtra("landlord_address", address);
        }
        
        if (booking.getBookingDetails() != null) {
            intent.putExtra("check_in_date", booking.getBookingDetails().getCheckInDate().getTime());
            intent.putExtra("check_out_date", booking.getBookingDetails().getCheckOutDate().getTime());
            intent.putExtra("duration_months", booking.getBookingDetails().getDuration());
        }
        
        if (booking.getPricing() != null) {
            intent.putExtra("monthly_rent", booking.getPricing().getMonthlyRent());
            intent.putExtra("deposit", booking.getPricing().getDeposit());
            intent.putExtra("utilities_amount", booking.getPricing().getUtilities());
            intent.putExtra("amount", booking.getPricing().getDeposit()); 
        }
        
        startActivity(intent);
    }

    private void updateBookingStatus(String bookingId, String newStatus) {
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        retrofitClient.getApiService().updateBooking(token, bookingId, new Booking()).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        loadBookings();
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể cập nhật trạng thái");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.booking_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_filter) {
            showFilterDialog();
            return true;
        } else if (id == R.id.action_refresh) {
            loadBookings();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        String[] statusOptions = {"Tất cả", "Chờ xác nhận", "Đã xác nhận", "Đã thanh toán", "Đang hoạt động", "Đã hoàn thành", "Đã hủy"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Lọc theo trạng thái")
                .setItems(statusOptions, (dialog, which) -> {
                    String selectedStatus = statusOptions[which];
                    filterBookingsByStatus(selectedStatus);
                })
                .show();
    }

    private void filterBookingsByStatus(String status) {
        if ("Tất cả".equals(status)) {
            bookingAdapter.filterByStatus(null);
        } else {
            String statusValue = convertStatusToValue(status);
            bookingAdapter.filterByStatus(statusValue);
        }
    }

    private String convertStatusToValue(String displayStatus) {
        switch (displayStatus) {
            case "Chờ xác nhận": return "pending";
            case "Đã xác nhận": return "confirmed";
            case "Đã thanh toán": return "deposit_paid";
            case "Đang hoạt động": return "active";
            case "Đã hoàn thành": return "completed";
            case "Đã hủy": return "cancelled";
            default: return null;
        }
    }
}