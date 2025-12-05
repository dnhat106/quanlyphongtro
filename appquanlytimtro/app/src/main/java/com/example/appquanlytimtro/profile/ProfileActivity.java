//activity: màn hình hồ sơ người dùng
// Mục đích file: File này dùng để hiển thị và chỉnh sửa thông tin hồ sơ người dùng
// function: 
// - onCreate(): Khởi tạo activity và setup các component
// - initViews(): Khởi tạo các view components
// - setupToolbar(): Thiết lập toolbar với menu
// - loadUserData(): Tải thông tin user hiện tại
// - displayUserInfo(): Hiển thị thông tin user lên UI
// - setupClickListeners(): Thiết lập các sự kiện click
// - onEditProfileClick(): Xử lý click chỉnh sửa hồ sơ
// - onChangePasswordClick(): Xử lý click đổi mật khẩu
// - onLogoutClick(): Xử lý click đăng xuất
// - updateProfile(): Cập nhật thông tin hồ sơ
// - handleUpdateResponse(): Xử lý phản hồi cập nhật
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
// - onCreateOptionsMenu(): Tạo menu options
// - onOptionsItemSelected(): Xử lý click vào menu item
package com.example.appquanlytimtro.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.appquanlytimtro.MainActivity;
import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.auth.LoginActivity;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    
    private User currentUser;
    private RetrofitClient retrofitClient;
    
    private ImageView ivAvatar;
    private EditText etFullName, etEmail, etPhone;
    private TextView tvRole, tvJoinDate;
    private Button btnSave, btnChangePassword;
    private ProgressBar progressBar;
    
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        initViews();
        setupToolbar();
        
        retrofitClient = RetrofitClient.getInstance(this);
        
        loadUserData();
        setupClickListeners();
    }
    
    private void initViews() {
        ivAvatar = findViewById(R.id.ivAvatar);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        tvRole = findViewById(R.id.tvRole);
        tvJoinDate = findViewById(R.id.tvJoinDate);
        btnSave = findViewById(R.id.btnSave);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thông tin cá nhân");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, User.class);
            
            if (currentUser != null) {
                displayUserData();
            }
        } else {
            // Load fresh data from server
            loadUserFromServer();
        }
    }
    
    private void loadUserFromServer() {
        showLoading(true);
        
        retrofitClient.getApiService().getCurrentUser(retrofitClient.getToken())
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<User> apiResponse = response.body();
                            
                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                currentUser = apiResponse.getData();
                                displayUserData();
                                
                                // Save updated user data
                                Gson gson = new Gson();
                                retrofitClient.saveUserData(gson.toJson(currentUser));
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Không thể tải thông tin người dùng");
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        showLoading(false);
                        showError("Lỗi kết nối. Vui lòng thử lại.");
                    }
                });
    }
    
    private void displayUserData() {
        if (currentUser == null) return;
        
        etFullName.setText(currentUser.getFullName());
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getPhone());
        
        // Set role text
        String roleText = getRoleText(currentUser.getRole());
        tvRole.setText("Vai trò: " + roleText);
        
        // Set join date
        if (currentUser.getCreatedAt() != null) {
            tvJoinDate.setText("Tham gia: " + formatDate(currentUser.getCreatedAt()));
        }
        
        // Set avatar
        if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
            String avatarUrl = currentUser.getAvatar();
            if (!avatarUrl.startsWith("http")) {
                avatarUrl = "http://10.0.2.2:5000" + avatarUrl;
            }
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_room_placeholder)
                    .error(R.drawable.ic_room_placeholder)
                    .into(ivAvatar);
        }
        
        setEditingMode(false);
    }
    
    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> {
            if (isEditing) {
                saveProfile();
            } else {
                setEditingMode(true);
            }
        });
        
        btnChangePassword.setOnClickListener(v -> {
            // Navigate to change password activity
            Toast.makeText(this, "Chức năng đổi mật khẩu sẽ được phát triển", Toast.LENGTH_SHORT).show();
        });
        
        ivAvatar.setOnClickListener(v -> {
            // Change avatar
            Toast.makeText(this, "Chức năng đổi avatar sẽ được phát triển", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void setEditingMode(boolean editing) {
        isEditing = editing;
        etFullName.setEnabled(editing);
        etPhone.setEnabled(editing);
        etEmail.setEnabled(false); // Email cannot be changed
        
        if (editing) {
            btnSave.setText("Lưu");
        } else {
            btnSave.setText("Chỉnh sửa");
        }
    }
    
    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        
        if (validateInput(fullName, phone)) {
            showLoading(true);
            
            // Update user object
            currentUser.setFullName(fullName);
            currentUser.setPhone(phone);
            
            retrofitClient.getApiService().updateUser(retrofitClient.getToken(), currentUser.getId(), currentUser)
                    .enqueue(new Callback<ApiResponse<User>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                            showLoading(false);
                            
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<User> apiResponse = response.body();
                                
                                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                    currentUser = apiResponse.getData();
                                    displayUserData();
                                    
                                    // Save updated user data
                                    Gson gson = new Gson();
                                    retrofitClient.saveUserData(gson.toJson(currentUser));
                                    
                                    Toast.makeText(ProfileActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                                } else {
                                    showError(apiResponse.getMessage());
                                }
                            } else {
                                showError("Không thể cập nhật thông tin");
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                            showLoading(false);
                            showError("Lỗi kết nối. Vui lòng thử lại.");
                        }
                    });
        }
    }
    
    private boolean validateInput(String fullName, String phone) {
        if (fullName.isEmpty()) {
            etFullName.setError("Họ tên không được để trống");
            etFullName.requestFocus();
            return false;
        }
        
        if (phone.isEmpty()) {
            etPhone.setError("Số điện thoại không được để trống");
            etPhone.requestFocus();
            return false;
        }
        
        if (!phone.matches("^[0-9]{10,11}$")) {
            etPhone.setError("Số điện thoại không hợp lệ");
            etPhone.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private String getRoleText(String role) {
        switch (role) {
            case "admin":
                return "Quản trị viên";
            case "landlord":
                return "Chủ trọ";
            case "tenant":
                return "Người thuê trọ";
            default:
                return role;
        }
    }
    
    private String formatDate(String dateString) {
        // Simple date formatting - you might want to use a proper date formatter
        if (dateString != null && dateString.length() > 10) {
            return dateString.substring(0, 10);
        }
        return dateString;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void logout() {
        retrofitClient.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
        btnChangePassword.setEnabled(!show);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
