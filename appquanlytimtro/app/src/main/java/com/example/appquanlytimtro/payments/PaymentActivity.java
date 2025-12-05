//activity: màn hình thanh toán
// Mục đích file: File này dùng để xử lý việc thanh toán cho đặt phòng
// function: 
// - onCreate(): Khởi tạo activity và lấy thông tin từ intent
// - initViews(): Khởi tạo các view components
// - setupToolbar(): Thiết lập toolbar với nút back
// - loadUserData(): Tải thông tin user hiện tại
// - loadRoomDetails(): Tải thông tin chi tiết phòng
// - displayPaymentInfo(): Hiển thị thông tin thanh toán
// - calculateTotalAmount(): Tính tổng số tiền cần thanh toán
// - setupPaymentMethods(): Thiết lập các phương thức thanh toán
// - onPaymentMethodSelected(): Xử lý chọn phương thức thanh toán
// - processPayment(): Xử lý thanh toán
// - processVNPayPayment(): Xử lý thanh toán qua VNPay
// - processCashPayment(): Xử lý thanh toán tiền mặt
// - createPaymentRecord(): Tạo bản ghi thanh toán
// - handlePaymentResult(): Xử lý kết quả thanh toán
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
// - onOptionsItemSelected(): Xử lý click vào menu item
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
import com.example.appquanlytimtro.config.VNPayConfig;
import com.example.appquanlytimtro.models.Booking;
import com.example.appquanlytimtro.models.Room;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvRoomTitle, tvRoomAddress, tvCheckIn, tvCheckOut, tvTotalAmount, tvDepositAmount;
    private MaterialButton btnPayNow, btnPayLater;
    private ProgressBar progressBar;
    private MaterialCardView cardPaymentInfo;
    
    private Room room;
    private Booking booking;
    private User currentUser;
    private RetrofitClient retrofitClient;
    
    private String landlordNameExtra;
    private String landlordPhoneExtra;
    private String landlordAddressExtra;
    
    private long checkInDate = 0;
    private long checkOutDate = 0;
    private int durationMonths = 0;
    private double monthlyRent = 0;
    private double deposit = 0;
    private double utilitiesAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();
        setupToolbar();
        loadData();
        setupPaymentButtons();
    }

    private void initViews() {
        tvRoomTitle = findViewById(R.id.tvRoomTitle);
        tvRoomAddress = findViewById(R.id.tvRoomAddress);
        tvCheckIn = findViewById(R.id.tvCheckIn);
        tvCheckOut = findViewById(R.id.tvCheckOut);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvDepositAmount = findViewById(R.id.tvDepositAmount);
        btnPayNow = findViewById(R.id.btnPayNow);
        btnPayLater = findViewById(R.id.btnPayLater);
        progressBar = findViewById(R.id.progressBar);
        cardPaymentInfo = findViewById(R.id.cardPaymentInfo);
        
        retrofitClient = RetrofitClient.getInstance(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thanh toán");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadData() {
        Intent intent = getIntent();
        if (intent != null) {
            String roomId = intent.getStringExtra("room_id");
            String bookingId = intent.getStringExtra("booking_id");
            
            landlordNameExtra = intent.getStringExtra("landlord_name");
            landlordPhoneExtra = intent.getStringExtra("landlord_phone");
            landlordAddressExtra = intent.getStringExtra("landlord_address");
            
            long checkInDate = intent.getLongExtra("check_in_date", 0);
            long checkOutDate = intent.getLongExtra("check_out_date", 0);
            int durationMonths = intent.getIntExtra("duration_months", 0);
            double monthlyRent = intent.getDoubleExtra("monthly_rent", 0);
            double deposit = intent.getDoubleExtra("deposit", 0);
            double utilitiesAmount = intent.getDoubleExtra("utilities_amount", 0);
            
            if (roomId != null) {
                loadRoomDetails(roomId);
            }
            
//            if (bookingId != null) {
//                loadBookingDetails(bookingId);
//            }
            
            if (checkInDate > 0 && checkOutDate > 0) {
                storeBookingDetails(checkInDate, checkOutDate, durationMonths, monthlyRent, deposit, utilitiesAmount);
            }
        }
    }
    
    private void storeBookingDetails(long checkInDate, long checkOutDate, int durationMonths, 
                                   double monthlyRent, double deposit, double utilitiesAmount) {
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.durationMonths = durationMonths;
        this.monthlyRent = monthlyRent;
        this.deposit = deposit;
        this.utilitiesAmount = utilitiesAmount;
        
        updateBookingDetailsUI();
    }
    
    private void updateBookingDetailsUI() {
        if (checkInDate > 0 && checkOutDate > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            tvCheckIn.setText(sdf.format(new java.util.Date(checkInDate)));
            tvCheckOut.setText(sdf.format(new java.util.Date(checkOutDate)));
            
            double totalAmount = (monthlyRent * durationMonths) + deposit + utilitiesAmount;
            java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.getDefault());
            tvTotalAmount.setText("Tổng cộng: " + formatter.format(totalAmount) + " VNĐ");
            tvDepositAmount.setText("Thanh toán tiền cọc: " + formatter.format(deposit) + " VNĐ");
        }
    }
    
    private void loadRoomDetails(String roomId) {
        showLoading(true);

        retrofitClient.getApiService().getRoom(roomId).enqueue(new retrofit2.Callback<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>>() {
                    @Override
            public void onResponse(retrofit2.Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, retrofit2.Response<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                    com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        java.util.Map<String, Object> data = apiResponse.getData();
                        if (data.containsKey("room")) {
                            try {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                String roomJson = gson.toJson(data.get("room"));
                                room = gson.fromJson(roomJson, Room.class);
                                
                                displayRoomInfo();
                                
                            } catch (Exception e) {
                                Toast.makeText(PaymentActivity.this, "Lỗi xử lý dữ liệu phòng", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                showLoading(false);
                    }

                    @Override
            public void onFailure(retrofit2.Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                        showLoading(false);
                Toast.makeText(PaymentActivity.this, "Lỗi tải thông tin phòng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    
    private void displayRoomInfo() {
        if (room != null) {
            tvRoomTitle.setText(room.getTitle());
            
            if (room.getAddress() != null) {
                String address = "";
                if (room.getAddress().getStreet() != null && !room.getAddress().getStreet().isEmpty()) {
                    address += room.getAddress().getStreet() + ", ";
                }
                if (room.getAddress().getWard() != null && !room.getAddress().getWard().isEmpty()) {
                    address += room.getAddress().getWard() + ", ";
                }
                if (room.getAddress().getDistrict() != null && !room.getAddress().getDistrict().isEmpty()) {
                    address += room.getAddress().getDistrict() + ", ";
                }
                if (room.getAddress().getCity() != null && !room.getAddress().getCity().isEmpty()) {
                    address += room.getAddress().getCity();
                }
                if (address.endsWith(", ")) {
                    address = address.substring(0, address.length() - 2);
                }
                tvRoomAddress.setText(address);
            }
            
            if (room.getPrice() != null) {
                if (monthlyRent <= 0) {
                    monthlyRent = room.getPrice().getMonthly();
                }
                if (deposit <= 0) {
                    deposit = room.getPrice().getDeposit();
                }
                if (durationMonths <= 0) {
                    durationMonths = 1;
                }
                updateBookingDetailsUI();
            }
        }
    }
    
    private void setupPaymentButtons() {
        btnPayNow.setOnClickListener(v -> processPayment());
        btnPayLater.setOnClickListener(v -> {
            Toast.makeText(this, "Đã lưu thông tin đặt phòng. Vui lòng thanh toán sau.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
    
    private void loadCurrentUser() {
        String token = "Bearer " + retrofitClient.getToken();
        retrofitClient.getApiService().getCurrentUser(token).enqueue(new retrofit2.Callback<com.example.appquanlytimtro.models.ApiResponse<User>>() {
                    @Override
            public void onResponse(retrofit2.Call<com.example.appquanlytimtro.models.ApiResponse<User>> call, retrofit2.Response<com.example.appquanlytimtro.models.ApiResponse<User>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                    com.example.appquanlytimtro.models.ApiResponse<User> apiResponse = response.body();
                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        currentUser = apiResponse.getData();
                        proceedWithPayment();
                            } else {
                        Toast.makeText(PaymentActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                    Toast.makeText(PaymentActivity.this, "Lỗi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
            public void onFailure(retrofit2.Call<com.example.appquanlytimtro.models.ApiResponse<User>> call, Throwable t) {
                Toast.makeText(PaymentActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void proceedWithPayment() {
        String orderId = "ORDER_" + System.currentTimeMillis();
        String orderInfo = "Thanh toan phong tro: " + (room != null ? room.getTitle() : "Phòng trọ");
        
        double amountToPay = 0;
        if (checkInDate > 0 && checkOutDate > 0) {
            amountToPay = deposit;
        } else if (room != null) {
            amountToPay = room.getPrice().getDeposit();
        }
        
        long amount = (long) amountToPay;
        
        VNPayService.PaymentRequest paymentRequest = new VNPayService.PaymentRequest(
            orderId,
            orderInfo,
            amount,
            "127.0.0.1" 
        );
        
        // Tạo intent với thông tin booking và order
        Intent intent = new Intent(this, VNPayWebViewActivity.class);
        String paymentUrl = VNPayService.createPaymentUrl(paymentRequest);
        if (paymentUrl != null) {
            intent.putExtra("payment_url", paymentUrl);
            intent.putExtra("order_id", orderId);
            
            // Truyền bookingId nếu có
            Intent currentIntent = getIntent();
            if (currentIntent != null) {
                String bookingId = currentIntent.getStringExtra("booking_id");
                if (bookingId != null) {
                    intent.putExtra("booking_id", bookingId);
                }
                
                // Truyền roomId nếu có
                String roomId = currentIntent.getStringExtra("room_id");
                if (roomId != null) {
                    intent.putExtra("room_id", roomId);
                } else if (room != null && room.getId() != null) {
                    intent.putExtra("room_id", room.getId());
                }
                
                // Truyền thông tin chủ trọ nếu có
                String landlordNameExtra = currentIntent.getStringExtra("landlord_name");
                String landlordPhoneExtra = currentIntent.getStringExtra("landlord_phone");
                String landlordAddressExtra = currentIntent.getStringExtra("landlord_address");
                if (landlordNameExtra != null) {
                    intent.putExtra("landlord_name", landlordNameExtra);
                }
                if (landlordPhoneExtra != null) {
                    intent.putExtra("landlord_phone", landlordPhoneExtra);
                }
                if (landlordAddressExtra != null) {
                    intent.putExtra("landlord_address", landlordAddressExtra);
                }
            }
            
            startActivityForResult(intent, 1001);
        } else {
            Toast.makeText(this, "Lỗi tạo URL thanh toán", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void processPayment() {
        if (room == null) {
            Toast.makeText(this, "Không có thông tin phòng để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!VNPayConfig.isConfigured()) {
            Toast.makeText(this, "VNPay chưa được cấu hình. Vui lòng liên hệ admin.", Toast.LENGTH_LONG).show();
            return;
        }
        
        double amountToPay = 0;
        if (checkInDate > 0 && checkOutDate > 0) {
            amountToPay = deposit;
        } else if (room != null && room.getPrice() != null) {
            amountToPay = room.getPrice().getDeposit();
        }
        
        if (amountToPay <= 0) {
            Toast.makeText(this, "Số tiền thanh toán không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        loadCurrentUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            boolean paymentSuccess = data.getBooleanExtra("payment_success", false);
            String paymentMessage = data.getStringExtra("payment_message");
            String transactionId = data.getStringExtra("transaction_id");
            String orderId = data.getStringExtra("order_id");
            long amount = data.getLongExtra("amount", 0);
            
            if (paymentSuccess) {
                // Mở màn hình hiển thị thông tin chủ trọ
            Intent successIntent = new Intent(this, PaymentSuccessActivity.class);
            successIntent.putExtra("txnRef", orderId);
            successIntent.putExtra("transaction_id", transactionId);
            successIntent.putExtra("amount", amount);
            successIntent.putExtra("bookingId", getIntent().getStringExtra("booking_id"));
            successIntent.putExtra("landlord_name", landlordNameExtra);
            successIntent.putExtra("landlord_phone", landlordPhoneExtra);
            successIntent.putExtra("landlord_address", landlordAddressExtra);
                startActivity(successIntent);
                finish();
            } else {
                Toast.makeText(this, "Thanh toán thất bại: " + paymentMessage, Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        
        Uri uri = intent.getData();
        if (uri != null && uri.getScheme().equals("com.example.appquanlytimtro") && uri.getHost().equals("vnpay")) {
            handlePaymentResult(uri);
        }
    }
    
    private void handlePaymentResult(Uri uri) {
        VNPayService.PaymentResponse response = VNPayService.handlePaymentResult(uri);
        
        if (response.isSuccess()) {
            // Mở màn hình hiển thị thông tin chủ trọ
            Intent successIntent = new Intent(this, PaymentSuccessActivity.class);
            successIntent.putExtra("txnRef", response.getOrderId());
            successIntent.putExtra("transaction_id", response.getTransactionId());
            successIntent.putExtra("amount", response.getAmount());
            successIntent.putExtra("bookingId", getIntent().getStringExtra("booking_id"));
            successIntent.putExtra("landlord_name", landlordNameExtra);
            successIntent.putExtra("landlord_phone", landlordPhoneExtra);
            successIntent.putExtra("landlord_address", landlordAddressExtra);
            startActivity(successIntent);
            finish();
        } else {
            Toast.makeText(this, "Thanh toán thất bại: " + response.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void createBookingRecord() {
        showLoading(true);
        
        Booking booking = new Booking();
        
        booking.setRoom(room);
        
        booking.setTenant(currentUser);
        
        booking.setLandlord(room.getLandlord());
        
        Booking.BookingDetails bookingDetails = new Booking.BookingDetails();
        if (checkInDate > 0 && checkOutDate > 0) {
            bookingDetails.setCheckInDate(new java.util.Date(checkInDate));
            bookingDetails.setCheckOutDate(new java.util.Date(checkOutDate));
            bookingDetails.setDuration(durationMonths);
        } else {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                bookingDetails.setCheckInDate(sdf.parse("2025-01-01"));
                bookingDetails.setCheckOutDate(sdf.parse("2025-02-01"));
                bookingDetails.setDuration(1);
            } catch (java.text.ParseException e) {
                bookingDetails.setCheckInDate(new java.util.Date());
                bookingDetails.setCheckOutDate(new java.util.Date());
                bookingDetails.setDuration(1);
            }
        }
        booking.setBookingDetails(bookingDetails);
        
        Booking.Pricing pricing = new Booking.Pricing();
        if (checkInDate > 0 && checkOutDate > 0) {
            double totalAmount = (monthlyRent * durationMonths) + deposit + utilitiesAmount;
            pricing.setTotalAmount(totalAmount);
            pricing.setMonthlyRent(monthlyRent);
            pricing.setDeposit(deposit);
            pricing.setUtilities(utilitiesAmount);
        } else {
            double roomDeposit = room.getPrice().getDeposit();
            double roomMonthly = room.getPrice().getMonthly();
            double roomUtilities = 0;
            Object utilitiesObj = room.getPrice().getUtilities();
            if (utilitiesObj instanceof Number) {
                roomUtilities = ((Number) utilitiesObj).doubleValue();
            }
            double roomTotal = roomMonthly + roomDeposit + roomUtilities;
            
            pricing.setTotalAmount(roomTotal);
            pricing.setMonthlyRent(roomMonthly);
            pricing.setDeposit(roomDeposit);
            pricing.setUtilities(roomUtilities);
        }
        booking.setPricing(pricing);
        
        booking.setStatus("confirmed");
        
        String token = "Bearer " + retrofitClient.getToken();
        
        java.util.Map<String, Object> bookingRequest = new java.util.HashMap<>();
        bookingRequest.put("roomId", room.getId());
        
        java.util.Map<String, Object> bookingDetailsMap = new java.util.HashMap<>();
        
        java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
        isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        
        java.util.Calendar localCheckIn = java.util.Calendar.getInstance();
        localCheckIn.setTime(bookingDetails.getCheckInDate());
        localCheckIn.set(java.util.Calendar.HOUR_OF_DAY, 0);
        localCheckIn.set(java.util.Calendar.MINUTE, 0);
        localCheckIn.set(java.util.Calendar.SECOND, 0);
        localCheckIn.set(java.util.Calendar.MILLISECOND, 0);
        
        java.util.Calendar localCheckOut = java.util.Calendar.getInstance();
        localCheckOut.setTime(bookingDetails.getCheckOutDate());
        localCheckOut.set(java.util.Calendar.HOUR_OF_DAY, 0);
        localCheckOut.set(java.util.Calendar.MINUTE, 0);
        localCheckOut.set(java.util.Calendar.SECOND, 0);
        localCheckOut.set(java.util.Calendar.MILLISECOND, 0);
        
        String checkInDateStr = isoFormat.format(localCheckIn.getTime());
        String checkOutDateStr = isoFormat.format(localCheckOut.getTime());
        
        bookingDetailsMap.put("checkInDate", checkInDateStr);
        bookingDetailsMap.put("checkOutDate", checkOutDateStr);
        bookingDetailsMap.put("duration", bookingDetails.getDuration());
        bookingDetailsMap.put("numberOfOccupants", 1);
        bookingRequest.put("bookingDetails", bookingDetailsMap);
        
        java.util.Map<String, Object> pricingMap = new java.util.HashMap<>();
        if (checkInDate > 0 && checkOutDate > 0) {
            double totalAmount = (monthlyRent * durationMonths) + deposit + utilitiesAmount;
            pricingMap.put("deposit", deposit);
            pricingMap.put("monthlyRent", monthlyRent);
            pricingMap.put("totalAmount", totalAmount);
            pricingMap.put("utilities", utilitiesAmount);
        } else {
            double roomDeposit = room.getPrice().getDeposit();
            double roomMonthly = room.getPrice().getMonthly();
            double roomUtilities = 0;
            Object utilitiesObj = room.getPrice().getUtilities();
            if (utilitiesObj instanceof Number) {
                roomUtilities = ((Number) utilitiesObj).doubleValue();
            }
            double roomTotal = roomMonthly + roomDeposit + roomUtilities;
            
            pricingMap.put("deposit", roomDeposit);
            pricingMap.put("monthlyRent", roomMonthly);
            pricingMap.put("totalAmount", roomTotal);
            pricingMap.put("utilities", roomUtilities);
        }
        bookingRequest.put("pricing", pricingMap);
        
        java.util.Map<String, Object> notesMap = new java.util.HashMap<>();
        notesMap.put("tenant", "Thanh toán qua VNPay");
        bookingRequest.put("notes", notesMap);
        
        retrofitClient.getApiService().createBooking(token, bookingRequest).enqueue(new retrofit2.Callback<com.example.appquanlytimtro.models.ApiResponse<Booking>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.appquanlytimtro.models.ApiResponse<Booking>> call, retrofit2.Response<com.example.appquanlytimtro.models.ApiResponse<Booking>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    com.example.appquanlytimtro.models.ApiResponse<Booking> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(PaymentActivity.this, "Đặt phòng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(PaymentActivity.this, "Lỗi tạo đặt phòng: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PaymentActivity.this, "Lỗi tạo đặt phòng", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.appquanlytimtro.models.ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(PaymentActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        cardPaymentInfo.setVisibility(show ? View.GONE : View.VISIBLE);
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