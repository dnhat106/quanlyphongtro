//activity: màn hình hiển thị chi tiết thông tin đặt phòng
// Mục đích file: File này dùng để hiển thị thông tin chi tiết của một đặt phòng cụ thể
// function: 
// - onCreate(): Khởi tạo activity và lấy booking_id từ intent
// - initViews(): Khởi tạo các view components
// - setupToolbar(): Thiết lập toolbar với nút back
// - loadBookingDetails(): Tải thông tin chi tiết booking từ API
// - populateBookingDetails(): Hiển thị thông tin booking lên UI
// - getStatusText(): Chuyển đổi mã trạng thái thành text hiển thị
// - getStatusColor(): Lấy màu sắc tương ứng với trạng thái
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
// - onCreateOptionsMenu(): Tạo menu options
// - onOptionsItemSelected(): Xử lý click vào menu item
package com.example.appquanlytimtro.bookings;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Booking;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingDetailActivity extends AppCompatActivity {

    private RetrofitClient retrofitClient;
    private String bookingId;
    private Booking booking;
    
    private ProgressBar progressBar;
    private View contentView;
    private TextView tvRoomTitle;
    private TextView tvRoomAddress;
    private TextView tvCheckInDate;
    private TextView tvCheckOutDate;
    private TextView tvDuration;
    private TextView tvNumberOfOccupants;
    private TextView tvTotalAmount;
    private TextView tvDepositAmount;
    private TextView tvMonthlyRent;
    private TextView tvUtilities;
    private TextView tvLandlordName;
    private TextView tvLandlordPhone;
    private TextView tvLandlordEmail;
    private TextView tvNotes;
    private Chip chipStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        retrofitClient = RetrofitClient.getInstance(this);
        bookingId = getIntent().getStringExtra("booking_id");
        
        initViews();
        setupToolbar();
        
        if (bookingId != null) {
            loadBookingDetails();
        } else {
            showError("Không tìm thấy thông tin đặt phòng");
        }
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        contentView = findViewById(R.id.contentView);
        tvRoomTitle = findViewById(R.id.tvRoomTitle);
        tvRoomAddress = findViewById(R.id.tvRoomAddress);
        tvCheckInDate = findViewById(R.id.tvCheckInDate);
        tvCheckOutDate = findViewById(R.id.tvCheckOutDate);
        tvDuration = findViewById(R.id.tvDuration);
        tvNumberOfOccupants = findViewById(R.id.tvNumberOfOccupants);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvDepositAmount = findViewById(R.id.tvDepositAmount);
        tvMonthlyRent = findViewById(R.id.tvMonthlyRent);
        tvUtilities = findViewById(R.id.tvUtilities);
        tvLandlordName = findViewById(R.id.tvLandlordName);
        tvLandlordPhone = findViewById(R.id.tvLandlordPhone);
        tvLandlordEmail = findViewById(R.id.tvLandlordEmail);
        tvNotes = findViewById(R.id.tvNotes);
        chipStatus = findViewById(R.id.chipStatus);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết đặt phòng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadBookingDetails() {
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        retrofitClient.getApiService().getBooking(token, bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        booking = apiResponse.getData();
                        populateBookingDetails();
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể tải thông tin đặt phòng");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void populateBookingDetails() {
        if (booking == null) return;

        if (booking.getRoom() != null) {
            tvRoomTitle.setText(booking.getRoom().getTitle());
            if (booking.getRoom().getAddress() != null) {
                String address = booking.getRoom().getAddress().getStreet() + ", " +
                               booking.getRoom().getAddress().getWard() + ", " +
                               booking.getRoom().getAddress().getDistrict() + ", " +
                               booking.getRoom().getAddress().getCity();
                tvRoomAddress.setText(address);
            }
        }

        if (booking.getBookingDetails() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvCheckInDate.setText(sdf.format(booking.getBookingDetails().getCheckInDate()));
            tvCheckOutDate.setText(sdf.format(booking.getBookingDetails().getCheckOutDate()));
            tvDuration.setText(booking.getBookingDetails().getDuration() + " tháng");
            tvNumberOfOccupants.setText(String.valueOf(booking.getBookingDetails().getNumberOfOccupants()));
        }

        if (booking.getPricing() != null) {
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            tvTotalAmount.setText(formatter.format(booking.getPricing().getTotalAmount()) + " VNĐ");
            tvDepositAmount.setText(formatter.format(booking.getPricing().getDeposit()) + " VNĐ");
            tvMonthlyRent.setText(formatter.format(booking.getPricing().getMonthlyRent()) + " VNĐ");
            tvUtilities.setText(formatter.format(booking.getPricing().getUtilities()) + " VNĐ");
        }

        if (booking.getLandlord() != null) {
            tvLandlordName.setText(booking.getLandlord().getFullName());
            tvLandlordPhone.setText(booking.getLandlord().getPhone());
            tvLandlordEmail.setText(booking.getLandlord().getEmail());
        }

        if (booking.getNotes() != null && booking.getNotes().getTenant() != null) {
            tvNotes.setText(booking.getNotes().getTenant());
        }

        chipStatus.setText(getStatusText(booking.getStatus()));
        chipStatus.setChipBackgroundColorResource(getStatusColor(booking.getStatus()));
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "Chờ xác nhận";
            case "confirmed": return "Đã xác nhận";
            case "deposit_paid": return "Đã thanh toán";
            case "active": return "Đang hoạt động";
            case "completed": return "Đã hoàn thành";
            case "cancelled": return "Đã hủy";
            default: return status;
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "pending": return R.color.warning;
            case "confirmed": return R.color.info;
            case "deposit_paid": return R.color.success;
            case "active": return R.color.primary;
            case "completed": return R.color.success;
            case "cancelled": return R.color.error;
            default: return R.color.on_surface_variant;
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        contentView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.booking_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
