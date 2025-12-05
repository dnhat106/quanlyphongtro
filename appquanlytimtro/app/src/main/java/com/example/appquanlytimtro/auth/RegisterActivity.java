//activity: màn hình đăng ký
// Mục đích file: File này dùng để xử lý việc đăng ký tài khoản mới cho người dùng trong ứng dụng quản lý tìm trọ
// function: 
// - onCreate(): Khởi tạo activity và setup các component
// - initViews(): Khởi tạo các view components
// - setupSpinners(): Thiết lập spinner chọn vai trò
// - setupClickListeners(): Thiết lập các sự kiện click
// - validateInput(): Kiểm tra tính hợp lệ của dữ liệu nhập
// - validatePassword(): Kiểm tra độ mạnh của mật khẩu
// - register(): Thực hiện đăng ký
// - handleRegisterResponse(): Xử lý phản hồi đăng ký
// - saveUserData(): Lưu thông tin user và token
// - navigateToMain(): Chuyển đến màn hình chính
// - navigateToLogin(): Chuyển đến màn hình đăng nhập
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
package com.example.appquanlytimtro.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appquanlytimtro.MainActivity;
import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.RegisterRequest;
import com.example.appquanlytimtro.models.RegisterResponse;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    
    private EditText etFullName, etEmail, etPassword, etConfirmPassword, etPhone;
    private Spinner spinnerRole;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    
    private RetrofitClient retrofitClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        initViews();
        setupSpinner();
        setupClickListeners();
        
        retrofitClient = RetrofitClient.getInstance(this);
    }
    
    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhone = findViewById(R.id.etPhone);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupSpinner() {
        String[] roles = {"Người thuê trọ", "Chủ trọ"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> performRegister());
        tvLogin.setOnClickListener(v -> navigateToLogin());
    }
    
    private void performRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String role = spinnerRole.getSelectedItemPosition() == 0 ? "tenant" : "landlord";
        
        if (validateInput(fullName, email, password, confirmPassword, phone)) {
            showLoading(true);
            
            RegisterRequest registerRequest = new RegisterRequest(fullName, email, password, phone, role);
            
            retrofitClient.getApiService().register(registerRequest).enqueue(new Callback<ApiResponse<RegisterResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<RegisterResponse>> call, Response<ApiResponse<RegisterResponse>> response) {
                    showLoading(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<RegisterResponse> apiResponse = response.body();
                        
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            RegisterResponse registerResponse = apiResponse.getData();
                            
                            retrofitClient.saveToken(registerResponse.getToken());
                            Gson gson = new Gson();
                            retrofitClient.saveUserData(gson.toJson(registerResponse.getUser()));
                            
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        } else {
                            String errorMessage = apiResponse.getMessage();
                            if (errorMessage == null || errorMessage.isEmpty()) {
                                errorMessage = "Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.";
                            }
                            showError(errorMessage);
                        }
                    } else {
                        String errorMessage = "Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.";
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                Gson gson = new Gson();
                                ApiResponse<?> errorResponse = gson.fromJson(errorBody, ApiResponse.class);
                                if (errorResponse != null && errorResponse.getMessage() != null) {
                                    errorMessage = errorResponse.getMessage();
                                }
                            } catch (Exception e) {
                            }
                        }
                        showError(errorMessage);
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<RegisterResponse>> call, Throwable t) {
                    showLoading(false);
                    showError("Lỗi kết nối. Vui lòng thử lại.");
                }
            });
        }
    }
    
    private boolean validateInput(String fullName, String email, String password, String confirmPassword, String phone) {
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Họ tên không được để trống");
            etFullName.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email không được để trống");
            etEmail.requestFocus();
            return false;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mật khẩu không được để trống");
            etPassword.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return false;
        }
        
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
            etPassword.setError("Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 số");
            etPassword.requestFocus();
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Số điện thoại không được để trống");
            etPhone.requestFocus();
            return false;
        }
        
        if (!phone.matches("^[0-9]{10,11}$")) {
            etPhone.setError("Số điện thoại phải có 10-11 chữ số");
            etPhone.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
        etFullName.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
        etConfirmPassword.setEnabled(!show);
        etPhone.setEnabled(!show);
        spinnerRole.setEnabled(!show);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
