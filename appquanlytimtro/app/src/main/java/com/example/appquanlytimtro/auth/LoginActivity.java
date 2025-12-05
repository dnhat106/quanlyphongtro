//activity: màn hình đăng nhập
// Mục đích file: File này dùng để xử lý việc đăng nhập của người dùng vào ứng dụng quản lý tìm trọ
// function: 
// - onCreate(): Khởi tạo activity và kiểm tra trạng thái đăng nhập
// - initViews(): Khởi tạo các view components
// - setupClickListeners(): Thiết lập các sự kiện click
// - validateInput(): Kiểm tra tính hợp lệ của dữ liệu nhập
// - login(): Thực hiện đăng nhập
// - handleLoginResponse(): Xử lý phản hồi đăng nhập
// - saveUserData(): Lưu thông tin user và token
// - navigateToMain(): Chuyển đến màn hình chính
// - navigateToRegister(): Chuyển đến màn hình đăng ký
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
package com.example.appquanlytimtro.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appquanlytimtro.MainActivity;
import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.LoginRequest;
import com.example.appquanlytimtro.models.LoginResponse;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    
    private RetrofitClient retrofitClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initViews();
        setupClickListeners();
        
        retrofitClient = RetrofitClient.getInstance(this);
        if (retrofitClient.isLoggedIn()) {
            navigateToMain();
        }
    }
    
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        tvRegister.setOnClickListener(v -> navigateToRegister());
    }
    
    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (validateInput(email, password)) {
            showLoading(true);
            
            LoginRequest loginRequest = new LoginRequest(email, password);
            
            retrofitClient.getApiService().login(loginRequest).enqueue(new Callback<ApiResponse<LoginResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                    showLoading(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<LoginResponse> apiResponse = response.body();
                        
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            LoginResponse loginResponse = apiResponse.getData();
                            
                            // Save token and user data
                            retrofitClient.saveToken(loginResponse.getToken());
                            Gson gson = new Gson();
                            String userJson = gson.toJson(loginResponse.getUser());
                            retrofitClient.saveUserData(userJson);
                            
                            
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            
                            // Small delay before navigation
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                navigateToMain();
                            }, 500);
                        } else {
                            showError(apiResponse.getMessage());
                        }
                    } else {
                        showError("Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.");
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                    showLoading(false);
                    showError("Lỗi kết nối. Vui lòng thử lại.");
                }
            });
        }
    }
    
    private boolean validateInput(String email, String password) {
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
        
        return true;
    }
    
    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
    
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
