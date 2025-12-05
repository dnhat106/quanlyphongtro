//fragment: màn hình đổi mật khẩu
// Mục đích file: File này dùng để xử lý việc đổi mật khẩu của người dùng
// function: 
// - onCreateView(): Khởi tạo view và setup các component
// - initViews(): Khởi tạo các view components
// - setupClickListeners(): Thiết lập các sự kiện click
// - validateInput(): Kiểm tra tính hợp lệ của dữ liệu nhập
// - changePassword(): Thực hiện đổi mật khẩu
// - handleChangePasswordResponse(): Xử lý phản hồi đổi mật khẩu
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
// - dismiss(): Đóng dialog
package com.example.appquanlytimtro.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends DialogFragment {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnSubmit, btnCancel;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);
        bindViews(view);
        setupClickListeners();
        return view;
    }

    private void bindViews(View view) {
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnCancel = view.findViewById(R.id.btnCancel);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnSubmit.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String current = etCurrentPassword.getText().toString();
        String next = etNewPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(current) || TextUtils.isEmpty(next) || next.length() < 6 || !next.equals(confirm)) {
            Toast.makeText(getContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        RetrofitClient client = RetrofitClient.getInstance(requireContext());
        Map<String, String> body = new HashMap<>();
        body.put("currentPassword", current);
        body.put("newPassword", next);

        client.getApiService().changePassword("Bearer " + client.getToken(), body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), R.string.save_failed, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
        btnCancel.setEnabled(!show);
    }
}


