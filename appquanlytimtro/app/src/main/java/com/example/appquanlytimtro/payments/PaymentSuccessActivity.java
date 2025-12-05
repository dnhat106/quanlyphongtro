//activity: màn hình hiển thị thông tin chủ trọ sau khi thanh toán thành công
// Mục đích file: File này dùng để hiển thị thông tin chủ trọ (số điện thoại, địa chỉ) sau khi khách hàng thanh toán thành công qua VNPay
package com.example.appquanlytimtro.payments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.bookings.BookingListActivity;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Map;

public class PaymentSuccessActivity extends AppCompatActivity {

    private TextView tvSuccessMessage, tvLandlordName, tvLandlordPhone, tvLandlordAddress;
    private MaterialButton btnCall, btnViewBooking, btnFinish;
    private ProgressBar progressBar;
    private MaterialCardView cardLandlordInfo;
    
    private RetrofitClient retrofitClient;
    private String txnRef;
    private String landlordPhone;
    private String landlordAddress;
    private String landlordName;
    private String paymentId;
    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        initViews();
        setupToolbar();
        loadDataFromIntent();
        loadLandlordInfo();
    }

    private void initViews() {
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);
        tvLandlordName = findViewById(R.id.tvLandlordName);
        tvLandlordPhone = findViewById(R.id.tvLandlordPhone);
        tvLandlordAddress = findViewById(R.id.tvLandlordAddress);
        btnCall = findViewById(R.id.btnCall);
        btnViewBooking = findViewById(R.id.btnViewBooking);
        btnFinish = findViewById(R.id.btnFinish);
        progressBar = findViewById(R.id.progressBar);
        cardLandlordInfo = findViewById(R.id.cardLandlordInfo);
        
        retrofitClient = RetrofitClient.getInstance(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thanh toán thành công");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            // Lấy từ URL redirect (nếu có)
            Uri data = intent.getData();
            if (data != null) {
                txnRef = data.getQueryParameter("txnRef");
                landlordPhone = data.getQueryParameter("landlordPhone");
                landlordAddress = data.getQueryParameter("landlordAddress");
                paymentId = data.getQueryParameter("paymentId");
                bookingId = data.getQueryParameter("bookingId");
            } else {
                // Lấy từ intent extras
                txnRef = intent.getStringExtra("txnRef");
                landlordPhone = intent.getStringExtra("landlord_phone");
                landlordAddress = intent.getStringExtra("landlord_address");
                landlordName = intent.getStringExtra("landlord_name");
                paymentId = intent.getStringExtra("paymentId");
                bookingId = intent.getStringExtra("bookingId");
            }
        }
    }
    
    private void loadLandlordInfoFromBooking() {
        if (bookingId == null || bookingId.isEmpty()) {
            displayLandlordInfo();
            return;
        }
        
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        retrofitClient.getApiService().getBooking(token, bookingId)
            .enqueue(new retrofit2.Callback<com.example.appquanlytimtro.models.ApiResponse<com.example.appquanlytimtro.models.Booking>>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.appquanlytimtro.models.ApiResponse<com.example.appquanlytimtro.models.Booking>> call,
                                       retrofit2.Response<com.example.appquanlytimtro.models.ApiResponse<com.example.appquanlytimtro.models.Booking>> response) {
                    showLoading(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        com.example.appquanlytimtro.models.ApiResponse<com.example.appquanlytimtro.models.Booking> apiResponse = response.body();
                        
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            com.example.appquanlytimtro.models.Booking booking = apiResponse.getData();
                            
                            if (booking.getLandlord() != null) {
                                landlordName = booking.getLandlord().getFullName();
                                landlordPhone = booking.getLandlord().getPhone();
                                
                                // Lấy địa chỉ từ landlord hoặc room
                                if (booking.getLandlord().getAddress() != null) {
                                    com.example.appquanlytimtro.models.User.Address addr = booking.getLandlord().getAddress();
                                    landlordAddress = (addr.getStreet() != null ? addr.getStreet() + ", " : "") +
                                                     (addr.getWard() != null ? addr.getWard() + ", " : "") +
                                                     (addr.getDistrict() != null ? addr.getDistrict() + ", " : "") +
                                                     (addr.getCity() != null ? addr.getCity() : "");
                                    if (landlordAddress.endsWith(", ")) {
                                        landlordAddress = landlordAddress.substring(0, landlordAddress.length() - 2);
                                    }
                                } else if (booking.getRoom() != null && booking.getRoom().getAddress() != null) {
                                    com.example.appquanlytimtro.models.User.Address addr = booking.getRoom().getAddress();
                                    landlordAddress = (addr.getStreet() != null ? addr.getStreet() + ", " : "") +
                                                     (addr.getWard() != null ? addr.getWard() + ", " : "") +
                                                     (addr.getDistrict() != null ? addr.getDistrict() + ", " : "") +
                                                     (addr.getCity() != null ? addr.getCity() : "");
                                    if (landlordAddress.endsWith(", ")) {
                                        landlordAddress = landlordAddress.substring(0, landlordAddress.length() - 2);
                                    }
                                }
                                
                                displayLandlordInfo();
                                return;
                            }
                        }
                    }
                    
                    // Fallback
                    displayLandlordInfo();
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.appquanlytimtro.models.ApiResponse<com.example.appquanlytimtro.models.Booking>> call, Throwable t) {
                    showLoading(false);
                    // Fallback
                    displayLandlordInfo();
                }
            });
    }

    private void loadLandlordInfo() {
        // Ưu tiên lấy từ bookingId nếu có (cho trường hợp lỗi "Website chưa được phê duyệt")
        if (bookingId != null && !bookingId.isEmpty()) {
            loadLandlordInfoFromBooking();
            return;
        }
        
        if (txnRef == null || txnRef.isEmpty()) {
            // Nếu không có txnRef, hiển thị thông tin từ intent
            displayLandlordInfo();
            return;
        }

        showLoading(true);

        retrofitClient.getApiService().getLandlordInfoAfterPayment(txnRef)
            .enqueue(new retrofit2.Callback<ApiResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(retrofit2.Call<ApiResponse<Map<String, Object>>> call,
                                     retrofit2.Response<ApiResponse<Map<String, Object>>> response) {
                    showLoading(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Map<String, Object>> apiResponse = response.body();
                        
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            Map<String, Object> data = apiResponse.getData();
                            
                            if (data.containsKey("landlord")) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> landlord = (Map<String, Object>) data.get("landlord");
                                
                                if (landlord != null) {
                                    landlordName = (String) landlord.get("fullName");
                                    landlordPhone = (String) landlord.get("phone");
                                    landlordAddress = (String) landlord.get("address");
                                    
                                    if (data.containsKey("paymentId")) {
                                        paymentId = String.valueOf(data.get("paymentId"));
                                    }
                                    if (data.containsKey("bookingId")) {
                                        bookingId = String.valueOf(data.get("bookingId"));
                                    }
                                    
                                    displayLandlordInfo();
                                }
                            }
                        } else {
                            // Fallback: hiển thị thông tin từ intent
                            displayLandlordInfo();
                        }
                    } else {
                        // Fallback: hiển thị thông tin từ intent
                        displayLandlordInfo();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                    showLoading(false);
                    // Fallback: hiển thị thông tin từ intent
                    displayLandlordInfo();
                }
            });
    }

    private void displayLandlordInfo() {
        tvSuccessMessage.setText("Thanh toán đặt cọc thành công!\nVui lòng liên hệ chủ trọ để nhận phòng:");
        
        if (landlordName != null && !landlordName.isEmpty()) {
            tvLandlordName.setText(landlordName);
        } else {
            tvLandlordName.setText("Chủ trọ");
        }
        
        if (landlordPhone != null && !landlordPhone.isEmpty()) {
            tvLandlordPhone.setText(landlordPhone);
            btnCall.setVisibility(View.VISIBLE);
            btnCall.setOnClickListener(v -> {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + landlordPhone));
                startActivity(callIntent);
            });
        } else {
            tvLandlordPhone.setText("Chưa có thông tin");
            btnCall.setVisibility(View.GONE);
        }
        
        if (landlordAddress != null && !landlordAddress.isEmpty()) {
            tvLandlordAddress.setText(landlordAddress);
        } else {
            tvLandlordAddress.setText("Chưa có thông tin");
        }
        
        cardLandlordInfo.setVisibility(View.VISIBLE);
        
        btnViewBooking.setOnClickListener(v -> {
            if (bookingId != null && !bookingId.isEmpty()) {
                // TODO: Mở màn hình chi tiết booking
                Toast.makeText(this, "Xem chi tiết đặt phòng", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không có thông tin đặt phòng", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnFinish.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentSuccessActivity.this, BookingListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        cardLandlordInfo.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

