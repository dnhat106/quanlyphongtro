//fragment: màn hình danh sách thanh toán
// Mục đích file: File này dùng để hiển thị danh sách các thanh toán của người dùng
// function: 
// - onCreateView(): Khởi tạo view và setup các component
// - initViews(): Khởi tạo các view components
// - setupRecyclerView(): Thiết lập RecyclerView và adapter
// - loadPayments(): Tải danh sách thanh toán từ API
// - updateEmptyView(): Cập nhật trạng thái empty view
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
// - onPaymentClick(): Xử lý click vào thanh toán
package com.example.appquanlytimtro.payments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FloatingActionButton fabDeposit;
    private PaymentsListAdapter adapter;
    private final List<Map<String, Object>> payments = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_payments, container, false);
        recyclerView = v.findViewById(R.id.recyclerView);
        progressBar = v.findViewById(R.id.progressBar);
        tvEmpty = v.findViewById(R.id.tvEmpty);
        fabDeposit = v.findViewById(R.id.fabDeposit);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PaymentsListAdapter(payments);
        recyclerView.setAdapter(adapter);
        fabDeposit.setOnClickListener(view -> openDeposit());
        loadPayments();
        return v;
    }

    private void loadPayments() {
        showLoading(true);
        RetrofitClient client = RetrofitClient.getInstance(requireContext());
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("limit", "50");
        String userJson = client.getUserData();
        String token = "Bearer " + client.getToken();
        User user = null;
        if (userJson != null) user = new Gson().fromJson(userJson, User.class);
        Call<ApiResponse<Map<String, Object>>> call;
        if (user != null && user.getRole() != null && !user.getRole().equals("admin")) {
            call = client.getApiService().getUserPayments(token, user.getId(), params);
        } else {
            call = client.getApiService().getPayments(token, params);
        }
        call.enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object listObj = response.body().getData().get("payments");
                    if (listObj instanceof List<?>) {
                        payments.clear();
                        for (Object o : (List<?>) listObj) {
                            if (o instanceof Map) {
                                payments.add((Map<String, Object>) o);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        toggleEmpty();
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

    private void toggleEmpty() {
        boolean isEmpty = payments.isEmpty();
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void openDeposit() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new DepositFragment())
                .addToBackStack(null)
                .commit();
    }
}


