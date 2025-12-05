//fragment: màn hình thanh toán tiền cọc
// Mục đích file: File này dùng để xử lý việc thanh toán tiền cọc cho đặt phòng
// function: 
// - onCreateView(): Khởi tạo view và setup các component
// - initViews(): Khởi tạo các view components
// - setupPaymentMethods(): Thiết lập các phương thức thanh toán
// - onPaymentMethodSelected(): Xử lý chọn phương thức thanh toán
// - processPayment(): Xử lý thanh toán
// - processVNPayPayment(): Xử lý thanh toán qua VNPay
// - processCashPayment(): Xử lý thanh toán tiền mặt
// - createPaymentRecord(): Tạo bản ghi thanh toán
// - handlePaymentResult(): Xử lý kết quả thanh toán
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
package com.example.appquanlytimtro.payments;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DepositFragment extends Fragment {

    private TextInputEditText etBookingId, etAmount, etBankName, etAccountNumber, etAccountHolder, etTransferNote;
    private RadioGroup rgMethod;
    private MaterialButton btnPay;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_deposit, container, false);
        bindViews(v);
        setupActions();
        return v;
    }

    private void bindViews(View v) {
        etBookingId = v.findViewById(R.id.etBookingId);
        etAmount = v.findViewById(R.id.etAmount);
        etBankName = v.findViewById(R.id.etBankName);
        etAccountNumber = v.findViewById(R.id.etAccountNumber);
        etAccountHolder = v.findViewById(R.id.etAccountHolder);
        etTransferNote = v.findViewById(R.id.etTransferNote);
        rgMethod = v.findViewById(R.id.rgMethod);
        btnPay = v.findViewById(R.id.btnPay);
        progressBar = v.findViewById(R.id.progressBar);
    }

    private void setupActions() {
        rgMethod.setOnCheckedChangeListener((group, checkedId) -> toggleBankFields());
        btnPay.setOnClickListener(v -> submit());
        toggleBankFields();
    }

    private void toggleBankFields() {
        boolean isBank = rgMethod.getCheckedRadioButtonId() == R.id.rbBankTransfer;
        int vis = isBank ? View.VISIBLE : View.GONE;
        etBankName.setVisibility(vis);
        etAccountNumber.setVisibility(vis);
        etAccountHolder.setVisibility(vis);
        etTransferNote.setVisibility(vis);
    }

    private void submit() {
        String bookingId = text(etBookingId);
        String amountStr = text(etAmount);
        if (TextUtils.isEmpty(bookingId) || TextUtils.isEmpty(amountStr)) {
            Toast.makeText(getContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }
        double amount;
        try { amount = Double.parseDouble(amountStr); } catch (Exception e) { Toast.makeText(getContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show(); return; }

        boolean isVNPay = rgMethod.getCheckedRadioButtonId() == R.id.rbVNPay;
        showLoading(true);
        RetrofitClient client = RetrofitClient.getInstance(requireContext());
        if (isVNPay) {
            Map<String, Object> body = new HashMap<>();
            body.put("bookingId", bookingId);
            body.put("type", "deposit");
            body.put("amount", amount);
            client.getApiService().createVNPayPayment("Bearer " + client.getToken(), body).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Map<String, Object> data = response.body().getData();
                        Object url = data != null ? (data.get("paymentUrl") != null ? data.get("paymentUrl") : data.get("url")) : null;
                        if (url != null) {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(url)));
                            startActivity(i);
                        } else {
                            Toast.makeText(getContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.save_failed, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Map<String, Object> body = new HashMap<>();
            body.put("bookingId", bookingId);
            body.put("type", "deposit");
            body.put("amount", amount);
            body.put("bankName", text(etBankName));
            body.put("accountNumber", text(etAccountNumber));
            body.put("accountHolder", text(etAccountHolder));
            body.put("transferNote", text(etTransferNote));
            client.getApiService().createBankTransferPayment("Bearer " + client.getToken(), body).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(getContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.save_failed, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private String text(TextInputEditText e) { return e.getText() == null ? "" : e.getText().toString().trim(); }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnPay.setEnabled(!show);
    }
}


