package com.example.appquanlytimtro.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;

public class AddEditUserActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPassword, etPhone, etStreet;
    private AutoCompleteTextView spinnerRole, etCity, etDistrict, etWard;
    private MaterialButton btnSubmit;
    private ProgressBar progressBar;
    private RetrofitClient retrofitClient;
    private User existingUser;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_user);

        retrofitClient = RetrofitClient.getInstance(this);
        
        existingUser = (User) getIntent().getSerializableExtra("user");
        isEditMode = existingUser != null;

        initViews();
        setupToolbar();
        setupRoleSpinner();
        setupAddressDropdowns();
        setupSubmitButton();

        if (isEditMode) {
            loadUserData();
        }
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etStreet = findViewById(R.id.etStreet);
        etCity = findViewById(R.id.etCity);
        etDistrict = findViewById(R.id.etDistrict);
        etWard = findViewById(R.id.etWard);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        if (isEditMode) {
            findViewById(R.id.layoutPassword).setVisibility(View.GONE);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "Sửa người dùng" : "Thêm người dùng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRoleSpinner() {
        String[] roles = {"tenant", "landlord", "admin"};
        String[] roleLabels = {"Người thuê", "Chủ trọ", "Quản trị viên"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roleLabels);
        spinnerRole.setAdapter(adapter);
    }

    private void setupAddressDropdowns() {
        String[] cities = {"Đà Nẵng", "TP. Hồ Chí Minh", "Hà Nội"};
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cities);
        etCity.setAdapter(cityAdapter);

        etCity.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = cities[position];
            setupDistrictDropdown(selectedCity);
            etDistrict.setText("");
            etWard.setText("");
        });

        etDistrict.setOnItemClickListener((parent, view, position, id) -> {
            String city = etCity.getText() != null ? etCity.getText().toString() : "";
            String district = etDistrict.getText() != null ? etDistrict.getText().toString() : "";
            setupWardDropdown(city, district);
            etWard.setText("");
        });
    }

    private void setupDistrictDropdown(String city) {
        String[] districts;
        if ("Đà Nẵng".equals(city)) {
            districts = new String[]{"Hải Châu", "Thanh Khê", "Sơn Trà"};
        } else if ("TP. Hồ Chí Minh".equals(city)) {
            districts = new String[]{"Quận 1", "Gò Vấp", "Thủ Đức"};
        } else if ("Hà Nội".equals(city)) {
            districts = new String[]{"Hoàn Kiếm", "Cầu Giấy", "Đống Đa"};
        } else {
            districts = new String[]{};
        }
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, districts);
        etDistrict.setAdapter(districtAdapter);
    }

    private void setupWardDropdown(String city, String district) {
        String[] wards;
        if ("Đà Nẵng".equals(city)) {
            if ("Hải Châu".equals(district)) {
                wards = new String[]{"Nam Dương", "Phước Ninh"};
            } else if ("Thanh Khê".equals(district)) {
                wards = new String[]{"Xuân Hà", "Tân Chính"};
            } else if ("Sơn Trà".equals(district)) {
                wards = new String[]{"Phước Mỹ", "An Hải Tây"};
            } else {
                wards = new String[]{};
            }
        } else if ("TP. Hồ Chí Minh".equals(city)) {
            if ("Quận 1".equals(district)) {
                wards = new String[]{"Bến Nghé", "Bến Thành"};
            } else if ("Gò Vấp".equals(district)) {
                wards = new String[]{"Phường 5", "Phường 10"};
            } else if ("Thủ Đức".equals(district)) {
                wards = new String[]{"Linh Tây", "Hiệp Phú"};
            } else {
                wards = new String[]{};
            }
        } else if ("Hà Nội".equals(city)) {
            if ("Hoàn Kiếm".equals(district)) {
                wards = new String[]{"Hàng Trống", "Tràng Tiền"};
            } else if ("Cầu Giấy".equals(district)) {
                wards = new String[]{"Dịch Vọng", "Yên Hòa"};
            } else if ("Đống Đa".equals(district)) {
                wards = new String[]{"Nam Đồng", "Phương Mai"};
            } else {
                wards = new String[]{};
            }
        } else {
            wards = new String[]{};
        }
        ArrayAdapter<String> wardAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, wards);
        etWard.setAdapter(wardAdapter);
    }

    private void loadUserData() {
        if (existingUser == null) return;

        etFullName.setText(existingUser.getFullName());
        etEmail.setText(existingUser.getEmail());
        etPhone.setText(existingUser.getPhone());

        String role = existingUser.getRole();
        String[] roleLabels = {"Người thuê", "Chủ trọ", "Quản trị viên"};
        String[] roles = {"tenant", "landlord", "admin"};
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(role)) {
                spinnerRole.setText(roleLabels[i], false);
                break;
            }
        }

        if (existingUser.getAddress() != null) {
            User.Address address = existingUser.getAddress();
            if (address.getStreet() != null) etStreet.setText(address.getStreet());
            if (address.getCity() != null) {
                etCity.setText(address.getCity(), false);
                setupDistrictDropdown(address.getCity());
            }
            if (address.getDistrict() != null) {
                etDistrict.setText(address.getDistrict(), false);
                setupWardDropdown(address.getCity(), address.getDistrict());
            }
            if (address.getWard() != null) {
                etWard.setText(address.getWard(), false);
            }
        }

        etEmail.setEnabled(false);
    }

    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> {
            if (validateInput()) {
                if (isEditMode) {
                    updateUser();
                } else {
                    createUser();
                }
            }
        });
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etFullName.getText())) {
            etFullName.setError("Vui lòng nhập họ tên");
            return false;
        }

        if (TextUtils.isEmpty(etEmail.getText())) {
            etEmail.setError("Vui lòng nhập email");
            return false;
        }

        if (!isEditMode) {
            if (etPassword == null || TextUtils.isEmpty(etPassword.getText())) {
                if (etPassword != null) {
                    etPassword.setError("Vui lòng nhập mật khẩu");
                }
                return false;
            }
        }

        if (TextUtils.isEmpty(etPhone.getText())) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            return false;
        }

        if (TextUtils.isEmpty(spinnerRole.getText())) {
            Toast.makeText(this, "Vui lòng chọn vai trò", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String getRoleFromLabel(String label) {
        switch (label) {
            case "Người thuê":
                return "tenant";
            case "Chủ trọ":
                return "landlord";
            case "Quản trị viên":
                return "admin";
            default:
                return "tenant";
        }
    }

    private void createUser() {
        showLoading(true);

        User user = new User();
        user.setFullName(etFullName.getText().toString().trim());
        user.setEmail(etEmail.getText().toString().trim().toLowerCase());
        user.setPhone(etPhone.getText().toString().trim());
        user.setRole(getRoleFromLabel(spinnerRole.getText().toString()));

        User.Address address = new User.Address();
        if (!TextUtils.isEmpty(etStreet.getText())) address.setStreet(etStreet.getText().toString().trim());
        if (etCity.getText() != null && !TextUtils.isEmpty(etCity.getText())) address.setCity(etCity.getText().toString().trim());
        if (etDistrict.getText() != null && !TextUtils.isEmpty(etDistrict.getText())) address.setDistrict(etDistrict.getText().toString().trim());
        if (etWard.getText() != null && !TextUtils.isEmpty(etWard.getText())) address.setWard(etWard.getText().toString().trim());
        user.setAddress(address);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("fullName", user.getFullName());
        requestBody.put("email", user.getEmail());
        if (etPassword != null && etPassword.getText() != null) {
            requestBody.put("password", etPassword.getText().toString());
        }
        requestBody.put("phone", user.getPhone());
        requestBody.put("role", user.getRole());
        if (user.getAddress() != null) {
            Map<String, String> addressMap = new HashMap<>();
            if (address.getStreet() != null) addressMap.put("street", address.getStreet());
            if (address.getCity() != null) addressMap.put("city", address.getCity());
            if (address.getDistrict() != null) addressMap.put("district", address.getDistrict());
            if (address.getWard() != null) addressMap.put("ward", address.getWard());
            if (!addressMap.isEmpty()) {
                requestBody.put("address", addressMap);
            }
        }

        String token = "Bearer " + retrofitClient.getToken();
        retrofitClient.getApiService().createUser(token, requestBody).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AddEditUserActivity.this, "Tạo người dùng thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    handleValidationErrors(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AddEditUserActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUser() {
        showLoading(true);

        User user = new User();
        user.setFullName(etFullName.getText().toString().trim());
        user.setPhone(etPhone.getText().toString().trim());
        user.setRole(getRoleFromLabel(spinnerRole.getText().toString()));

        User.Address address = new User.Address();
        if (!TextUtils.isEmpty(etStreet.getText())) address.setStreet(etStreet.getText().toString().trim());
        if (etCity.getText() != null && !TextUtils.isEmpty(etCity.getText())) address.setCity(etCity.getText().toString().trim());
        if (etDistrict.getText() != null && !TextUtils.isEmpty(etDistrict.getText())) address.setDistrict(etDistrict.getText().toString().trim());
        if (etWard.getText() != null && !TextUtils.isEmpty(etWard.getText())) address.setWard(etWard.getText().toString().trim());
        user.setAddress(address);

        String token = "Bearer " + retrofitClient.getToken();
        retrofitClient.getApiService().updateUser(token, existingUser.getId(), user).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AddEditUserActivity.this, "Cập nhật người dùng thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    handleValidationErrors(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AddEditUserActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
    }

    private void handleValidationErrors(Response<ApiResponse<User>> response) {
        clearAllErrors();
        
        String errorMsg = isEditMode ? "Lỗi cập nhật người dùng" : "Lỗi tạo người dùng";
        boolean hasFieldErrors = false;
        
        try {
            String errorBodyString = null;
            if (response.errorBody() != null) {
                errorBodyString = response.errorBody().string();
            } else if (response.body() != null) {
                Gson gson = new Gson();
                errorBodyString = gson.toJson(response.body());
            }
            
            if (errorBodyString != null) {
                Gson gson = new Gson();
                JsonObject errorJson = gson.fromJson(errorBodyString, JsonObject.class);
                
                if (errorJson.has("errors") && errorJson.get("errors").isJsonArray()) {
                    JsonArray errorsArray = errorJson.getAsJsonArray("errors");
                    
                    for (JsonElement element : errorsArray) {
                        JsonObject errorObj = element.getAsJsonObject();
                        String field = errorObj.has("field") ? errorObj.get("field").getAsString() : null;
                        String message = errorObj.has("message") ? errorObj.get("message").getAsString() : null;
                        
                        if (field != null && message != null) {
                            hasFieldErrors = true;
                            setFieldError(field, message);
                        }
                    }
                }
                
                if (errorJson.has("message")) {
                    errorMsg = errorJson.get("message").getAsString();
                }
            } else if (response.body() != null && response.body().getMessage() != null) {
                errorMsg = response.body().getMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (response.body() != null && response.body().getMessage() != null) {
                errorMsg = response.body().getMessage();
            }
        }
        
        if (!hasFieldErrors) {
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void clearAllErrors() {
        etFullName.setError(null);
        etEmail.setError(null);
        if (etPassword != null) etPassword.setError(null);
        etPhone.setError(null);
    }

    private void setFieldError(String field, String message) {
        switch (field) {
            case "fullName":
                etFullName.setError(message);
                etFullName.requestFocus();
                break;
            case "email":
                etEmail.setError(message);
                etEmail.requestFocus();
                break;
            case "password":
                if (etPassword != null) {
                    etPassword.setError(message);
                    etPassword.requestFocus();
                }
                break;
            case "phone":
                etPhone.setError(message);
                etPhone.requestFocus();
                break;
        }
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

