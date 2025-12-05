//fragment: màn hình hồ sơ người dùng
// Mục đích file: File này dùng để hiển thị thông tin hồ sơ người dùng
// function: 
// - onCreateView(): Khởi tạo view và setup các component
// - initViews(): Khởi tạo các view components
// - setupClickListeners(): Thiết lập các sự kiện click
// - loadUserData(): Tải thông tin user hiện tại
// - displayUserInfo(): Hiển thị thông tin user lên UI
// - onEditProfileClick(): Xử lý click chỉnh sửa hồ sơ
// - onChangePasswordClick(): Xử lý click đổi mật khẩu
// - onLogoutClick(): Xử lý click đăng xuất
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
package com.example.appquanlytimtro.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ImageView ivAvatar;
    private EditText etFullName, etEmail, etPhone;
    private TextView tvRole, tvJoinDate;
    private MaterialButton btnSave, btnChangePassword, btnLogout;
    private ProgressBar progressBar;

    private RetrofitClient retrofitClient;
    private User currentUser;
    private boolean isEditing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        retrofitClient = RetrofitClient.getInstance(requireContext());
        bindViews(view);
        loadUserData();
        setupClickListeners();
        return view;
    }

    private void bindViews(View view) {
        ivAvatar = view.findViewById(R.id.ivAvatar);
        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        tvRole = view.findViewById(R.id.tvRole);
        tvJoinDate = view.findViewById(R.id.tvJoinDate);
        btnSave = view.findViewById(R.id.btnSave);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnLogout = view.findViewById(R.id.btnLogout);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        if (!TextUtils.isEmpty(userJson)) {
            currentUser = new Gson().fromJson(userJson, User.class);
            if (currentUser != null) {
                etFullName.setText(currentUser.getFullName());
                etEmail.setText(currentUser.getEmail());
                etPhone.setText(currentUser.getPhone());
                tvRole.setText(currentUser.getRole());
                if (getContext() != null && currentUser.getAvatar() != null) {
                    Glide.with(getContext()).load(currentUser.getAvatar()).into(ivAvatar);
                }
            }
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

        btnChangePassword.setOnClickListener(v -> showChangePassword());
        
        btnLogout.setOnClickListener(v -> {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Call logout method from MainActivity
                    if (getActivity() instanceof com.example.appquanlytimtro.MainActivity) {
                        ((com.example.appquanlytimtro.MainActivity) getActivity()).logout();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }

    private void setEditingMode(boolean editing) {
        isEditing = editing;
        etFullName.setEnabled(editing);
        etPhone.setEnabled(editing);
        etEmail.setEnabled(false);
        btnSave.setText(editing ? R.string.save : R.string.edit);
    }

    private void saveProfile() {
        if (currentUser == null) return;
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phone)) {
            Toast.makeText(getContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading(true);
        currentUser.setFullName(fullName);
        currentUser.setPhone(phone);
        retrofitClient.getApiService().updateUser("Bearer " + retrofitClient.getToken(), currentUser.getId(), currentUser)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            User updated = response.body().getData();
                            currentUser = updated != null ? updated : currentUser;
                            retrofitClient.saveUserData(new Gson().toJson(currentUser));
                            setEditingMode(false);
                            Toast.makeText(getContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), R.string.save_failed, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showChangePassword() {
        ChangePasswordFragment dialog = new ChangePasswordFragment();
        dialog.show(getParentFragmentManager(), "ChangePassword");
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
        btnChangePassword.setEnabled(!show);
        btnLogout.setEnabled(!show);
    }
}


