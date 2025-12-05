//fragment: màn hình quản lý người dùng cho admin
// Mục đích file: File này dùng để quản lý tất cả người dùng trong hệ thống cho admin
// function: 
// - onCreateView(): Khởi tạo view và setup các component
// - loadUsers(): Tải danh sách người dùng từ API
// - showLoading(): Hiển thị/ẩn loading indicator
// - onUserClick(): Xử lý click vào người dùng
package com.example.appquanlytimtro.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsersFragment extends Fragment implements UsersAdapter.OnUserClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvTotalUsers, tvTotalLandlords, tvTotalTenants;
    private MaterialButton btnAdd;
    private UsersAdapter adapter;
    private final List<User> users = new ArrayList<>();
    private ActivityResultLauncher<Intent> addEditUserLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_users, container, false);
        
        recyclerView = v.findViewById(R.id.recyclerView);
        progressBar = v.findViewById(R.id.progressBar);
        tvTotalUsers = v.findViewById(R.id.tvTotalUsers);
        tvTotalLandlords = v.findViewById(R.id.tvTotalLandlords);
        tvTotalTenants = v.findViewById(R.id.tvTotalTenants);
        btnAdd = v.findViewById(R.id.btnAdd);
        
        setupActivityLauncher();
        setupAddButton();
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UsersAdapter(users);
        adapter.setOnUserClickListener(this);
        recyclerView.setAdapter(adapter);
        loadUsers();
        return v;
    }

    private void setupActivityLauncher() {
        addEditUserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    loadUsers();
                }
            }
        );
    }

    private void setupAddButton() {
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddEditUserActivity.class);
            addEditUserLauncher.launch(intent);
        });
    }

    private void loadUsers() {
        showLoading(true);
        RetrofitClient client = RetrofitClient.getInstance(requireContext());
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("limit", "50");
        client.getApiService().getUsers("Bearer " + client.getToken(), params).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    Object listObj = response.body().getData().get("users");
                    if (listObj instanceof List<?>) {
                        users.clear();
                        int totalUsers = 0;
                        int totalLandlords = 0;
                        int totalTenants = 0;
                        
                        for (Object o : (List<?>) listObj) {
                            String json = gson.toJson(o);
                            User u = gson.fromJson(json, User.class);
                            users.add(u);
                            
                            totalUsers++;
                            if ("landlord".equals(u.getRole())) {
                                totalLandlords++;
                            } else if ("tenant".equals(u.getRole())) {
                                totalTenants++;
                            }
                        }
                        
                        tvTotalUsers.setText(String.valueOf(totalUsers));
                        tvTotalLandlords.setText(String.valueOf(totalLandlords));
                        tvTotalTenants.setText(String.valueOf(totalTenants));
                        
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), R.string.load_failed, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onUserClick(User user) {
    }

    @Override
    public void onUserEdit(User user) {
        Intent intent = new Intent(getContext(), AddEditUserActivity.class);
        intent.putExtra("user", user);
        addEditUserLauncher.launch(intent);
    }

    @Override
    public void onUserDelete(User user) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Xóa người dùng")
            .setMessage("Bạn có chắc chắn muốn xóa người dùng " + user.getFullName() + "?")
            .setPositiveButton("Xóa", (dialog, which) -> deleteUser(user))
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteUser(User user) {
        showLoading(true);
        RetrofitClient client = RetrofitClient.getInstance(requireContext());
        client.getApiService().deleteUser("Bearer " + client.getToken(), user.getId())
            .enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(getContext(), "Xóa người dùng thành công", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else {
                        String errorMsg = "Lỗi xóa người dùng";
                        if (response.body() != null && response.body().getMessage() != null) {
                            errorMsg = response.body().getMessage();
                        }
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(getContext(), "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                }
            });
    }
}


