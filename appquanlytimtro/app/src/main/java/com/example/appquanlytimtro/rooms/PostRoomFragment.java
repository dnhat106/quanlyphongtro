//fragment: màn hình đăng phòng trọ
// Mục đích file: File này dùng để xử lý việc đăng phòng trọ mới cho chủ trọ
// function: 
// - onCreateView(): Khởi tạo view và setup các component
// - initViews(): Khởi tạo các view components
// - setupImagePicker(): Thiết lập chọn ảnh
// - setupSubmitButton(): Thiết lập nút submit
// - validateInput(): Kiểm tra tính hợp lệ của dữ liệu nhập
// - createRoom(): Tạo phòng trọ mới
// - uploadImages(): Upload ảnh lên server
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
package com.example.appquanlytimtro.rooms;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.Room;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostRoomFragment extends Fragment {

    private TextInputEditText etTitle, etDescription, etCity, etDistrict, etWard, etStreet, etArea, etPrice, etDeposit;
    private MaterialButton btnSubmit, btnPickImages;
    private ProgressBar progressBar;
    private final List<Uri> imageUris = new ArrayList<>();
    private ActivityResultLauncher<Intent> pickImagesLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_room, container, false);
        bindViews(view);
        setupSubmit();
        setupPickImages();
        return view;
    }

    private void bindViews(View v) {
        etTitle = v.findViewById(R.id.etTitle);
        etDescription = v.findViewById(R.id.etDescription);
        etCity = v.findViewById(R.id.etCity);
        etDistrict = v.findViewById(R.id.etDistrict);
        etWard = v.findViewById(R.id.etWard);
        etStreet = v.findViewById(R.id.etStreet);
        etArea = v.findViewById(R.id.etArea);
        etPrice = v.findViewById(R.id.etPrice);
        etDeposit = v.findViewById(R.id.etDeposit);
        btnSubmit = v.findViewById(R.id.btnSubmit);
        btnPickImages = v.findViewById(R.id.btnPickImages);
        progressBar = v.findViewById(R.id.progressBar);
    }

    private void setupSubmit() {
        btnSubmit.setOnClickListener(v -> {
            String title = text(etTitle);
            String desc = text(etDescription);
            String city = text(etCity);
            String district = text(etDistrict);
            String ward = text(etWard);
            String street = text(etStreet);
            String areaStr = text(etArea);
            String priceStr = text(etPrice);
            String depositStr = text(etDeposit);

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(city) || TextUtils.isEmpty(priceStr)) {
                Toast.makeText(getContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double area = TextUtils.isEmpty(areaStr) ? 0 : Double.parseDouble(areaStr);
                double price = Double.parseDouble(priceStr);
                double deposit = TextUtils.isEmpty(depositStr) ? 0 : Double.parseDouble(depositStr);

                Room room = new Room();
                room.setTitle(title);
                room.setDescription(desc);
                User.Address address = new User.Address();
                address.setCity(city);
                address.setDistrict(district);
                address.setWard(ward);
                address.setStreet(street);
                room.setAddress(address);
                room.setArea(area);
                Room.Price rp = new Room.Price();
                rp.setMonthly(price);
                rp.setDeposit(deposit);
                room.setPrice(rp);

                createRoom(room);
            } catch (Exception ex) {
                Toast.makeText(getContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String text(TextInputEditText e) { return e.getText() == null ? "" : e.getText().toString().trim(); }

    private void createRoom(Room room) {
        showLoading(true);
        RetrofitClient client = RetrofitClient.getInstance(requireContext());
        client.getApiService().createRoom("Bearer " + client.getToken(), room).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show();
                    
                    Map<String, Object> responseData = response.body().getData();
                    Room created = null;
                    
                    if (responseData != null && responseData.containsKey("room")) {
                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        String roomJson = gson.toJson(responseData.get("room"));
                        created = gson.fromJson(roomJson, Room.class);
                    }
                    
                    if (created != null && created.getId() != null && !imageUris.isEmpty()) {
                        uploadImages(created.getId());
                    } else {
                        clearForm();
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
    }

    private void setupPickImages() {
        pickImagesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                imageUris.clear();
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        imageUris.add(data.getClipData().getItemAt(i).getUri());
                    }
                } else if (data.getData() != null) {
                    imageUris.add(data.getData());
                }
                Toast.makeText(getContext(), "Đã chọn " + imageUris.size() + " ảnh", Toast.LENGTH_SHORT).show();
            }
        });

        btnPickImages.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickImagesLauncher.launch(Intent.createChooser(intent, "Chọn ảnh phòng"));
        });
    }

    private void uploadImages(String roomId) {
        showLoading(true);
        RetrofitClient client = RetrofitClient.getInstance(requireContext());
        List<MultipartBody.Part> parts = new ArrayList<>();
        for (Uri uri : imageUris) {
            try {
                InputStream is = requireContext().getContentResolver().openInputStream(uri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] bytes = buffer.toByteArray();

                RequestBody req = RequestBody.create(bytes, MediaType.parse("image/*"));
                MultipartBody.Part part = MultipartBody.Part.createFormData("images", "image.jpg", req);
                parts.add(part);

                is.close(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        client.getApiService().uploadRoomImages("Bearer " + client.getToken(), roomId, parts).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show();
                    clearForm();
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

    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        etCity.setText("");
        etDistrict.setText("");
        etWard.setText("");
        etStreet.setText("");
        etArea.setText("");
        etPrice.setText("");
        etDeposit.setText("");
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
    }
}


