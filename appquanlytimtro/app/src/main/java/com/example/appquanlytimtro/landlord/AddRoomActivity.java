//activity: màn hình thêm phòng trọ mới
// Mục đích file: File này dùng để tạo phòng trọ mới cho chủ trọ
// function: 
// - onCreate(): Khởi tạo activity và setup các component
// - initViews(): Khởi tạo các view components
// - setupToolbar(): Thiết lập toolbar với nút back
// - setupSpinners(): Thiết lập spinner chọn loại phòng
// - mapRoomTypeToBackend(): Chuyển đổi loại phòng hiển thị thành mã backend
// - mapAmenityToBackend(): Chuyển đổi tiện ích hiển thị thành mã backend
// - setupAmenities(): Thiết lập danh sách tiện ích
// - setupRules(): Thiết lập danh sách quy định
// - setupImagePicker(): Thiết lập chọn ảnh
// - removeImage(): Xóa ảnh khỏi danh sách
// - setupSubmitButton(): Thiết lập nút submit
// - validateInput(): Kiểm tra tính hợp lệ của dữ liệu nhập
// - createRoom(): Tạo phòng trọ mới
// - uploadImages(): Upload ảnh lên server
// - getText(): Lấy text từ EditText
// - showLoading(): Hiển thị/ẩn loading indicator
// - onOptionsItemSelected(): Xử lý click vào menu item
package com.example.appquanlytimtro.landlord;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.ImagePreviewAdapter;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Room;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddRoomActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etStreet;
    private AutoCompleteTextView etCity, etDistrict, etWard;
    private TextInputEditText etArea, etPrice, etDeposit, etElectricPrice, etWaterPrice, etInternetPrice, etOtherPrice;
    private AutoCompleteTextView spinnerRoomType;
    private LinearLayout layoutAmenities, layoutRules;
    private MaterialButton btnSubmit, btnPickImages;
    private RecyclerView recyclerViewImages;
    private ProgressBar progressBar;
    
    private final List<Uri> imageUris = new ArrayList<>();
    private ImagePreviewAdapter imageAdapter;
    private ActivityResultLauncher<Intent> pickImagesLauncher;
    private RetrofitClient retrofitClient;

    private final String[] amenitiesList = {
        "WiFi", "Điều hòa", "Tủ lạnh", "Máy giặt", "Bàn làm việc", 
        "Tủ quần áo", "Giường", "Bếp", "Nóng lạnh", "Ban công",
        "Thang máy", "Bảo vệ 24/7", "Chỗ để xe", "Camera an ninh"
    };
    
    private final String[] rulesList = {
        "Không hút thuốc", "Không nuôi thú cưng", "Không ồn ào sau 22h",
        "Không tổ chức tiệc tùng", "Giữ gìn vệ sinh chung", "Trả phòng đúng hạn",
        "Không sử dụng thiết bị công suất lớn", "Khách không được ở qua đêm"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);
        
        retrofitClient = RetrofitClient.getInstance(this);
        
        initViews();
        setupToolbar();
        setupSpinners();
        setupAmenities();
        setupRules();
        setupImagePicker();
        setupSubmitButton();
    }
    
    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etCity = findViewById(R.id.etCity);
        etDistrict = findViewById(R.id.etDistrict);
        etWard = findViewById(R.id.etWard);
        etStreet = findViewById(R.id.etStreet);
        etArea = findViewById(R.id.etArea);
        etPrice = findViewById(R.id.etPrice);
        etDeposit = findViewById(R.id.etDeposit);
        etElectricPrice = findViewById(R.id.etElectricPrice);
        etWaterPrice = findViewById(R.id.etWaterPrice);
        etInternetPrice = findViewById(R.id.etInternetPrice);
        etOtherPrice = findViewById(R.id.etOtherPrice);
        spinnerRoomType = findViewById(R.id.spinnerRoomType);
        layoutAmenities = findViewById(R.id.layoutAmenities);
        layoutRules = findViewById(R.id.layoutRules);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnPickImages = findViewById(R.id.btnPickImages);
        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        progressBar = findViewById(R.id.progressBar);
        
        imageAdapter = new ImagePreviewAdapter(imageUris, this::removeImage);
        recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewImages.setAdapter(imageAdapter);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thêm phòng trọ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupSpinners() {
        String[] roomTypes = {"Phòng trọ", "Chung cư mini", "Nhà nguyên căn", "Homestay", "Ký túc xá"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roomTypes);
        spinnerRoomType.setAdapter(adapter);

        String[] cities = {"Hà Nội", "TP.HCM", "Đà Nẵng"};
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cities);
        etCity.setAdapter(cityAdapter);

        etCity.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = cities[position];
            String[] districts;
            if ("Đà Nẵng".equals(selectedCity)) {
                districts = new String[]{"Hải Châu", "Thanh Khê", "Hoà Khánh", "Sơn Trà"};
            } else if ("Hà Nội".equals(selectedCity)) {
                districts = new String[]{"Ba Đình", "Hoàn Kiếm", "Đống Đa", "Cầu Giấy"};
            } else {
                districts = new String[]{"Quận 1", "Quận 3", "Bình Thạnh", "Phú Nhuận"};
            }
            ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, districts);
            etDistrict.setAdapter(districtAdapter);
            etDistrict.setText("");
            etWard.setText("");
        });

        etDistrict.setOnItemClickListener((parent, view, position, id) -> {
            String city = etCity.getText() != null ? etCity.getText().toString() : "";
            String district = etDistrict.getText() != null ? etDistrict.getText().toString() : "";
            String[] wards;
            if ("Đà Nẵng".equals(city) && "Hải Châu".equals(district)) {
                wards = new String[]{"Hải Châu 1", "Hải Châu 2"};
            } else if ("Đà Nẵng".equals(city) && "Thanh Khê".equals(district)) {
                wards = new String[]{"Thanh Khê Đông", "Thanh Khê Tây"};
            } else {
                wards = new String[]{"Phường 1", "Phường 2", "Phường 3"};
            }
            ArrayAdapter<String> wardAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, wards);
            etWard.setAdapter(wardAdapter);
            etWard.setText("");
        });
    }
    
    private String mapRoomTypeToBackend(String displayType) {
        switch (displayType) {
            case "Phòng trọ":
                return "studio";
            case "Chung cư mini":
                return "1bedroom";
            case "Nhà nguyên căn":
                return "2bedroom";
            case "Homestay":
                return "3bedroom";
            case "Ký túc xá":
                return "shared";
            default:
                return "studio";
        }
    }
    
    private String mapAmenityToBackend(String displayAmenity) {
        switch (displayAmenity) {
            case "WiFi":
                return "wifi";
            case "Điều hòa":
                return "air_conditioner";
            case "Tủ lạnh":
                return "refrigerator";
            case "Máy giặt":
                return "washing_machine";
            case "Bàn làm việc":
                return "desk";
            case "Tủ quần áo":
                return "wardrobe";
            case "Giường":
                return "bed";
            case "Bếp":
                return "kitchen";
            case "Nóng lạnh":
                return "hot_water";
            case "Ban công":
                return "balcony";
            case "Thang máy":
                return "elevator";
            case "Bảo vệ 24/7":
                return "security";
            case "Chỗ để xe":
                return "parking";
            case "Camera an ninh":
                return "security";
            default:
                return displayAmenity.toLowerCase().replace(" ", "_");
        }
    }
    
    private void setupAmenities() {
        for (String amenity : amenitiesList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(amenity);
            checkBox.setTag(amenity);
            layoutAmenities.addView(checkBox);
        }
    }
    
    private void setupRules() {
        for (String rule : rulesList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(rule);
            checkBox.setTag(rule);
            layoutRules.addView(checkBox);
        }
    }
    
    private void setupImagePicker() {
        pickImagesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            imageUris.add(imageUri);
                        }
                    } else if (data.getData() != null) {
                        imageUris.add(data.getData());
                    }
                    imageAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Đã chọn " + imageUris.size() + " ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        btnPickImages.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickImagesLauncher.launch(Intent.createChooser(intent, "Chọn ảnh phòng"));
        });
    }
    
    private void removeImage(int position) {
        if (position >= 0 && position < imageUris.size()) {
            imageUris.remove(position);
            imageAdapter.notifyItemRemoved(position);
        }
    }
    
    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> {
            if (validateInput()) {
                createRoom();
            }
        });
    }
    
    private boolean validateInput() {
        String title = getText(etTitle);
        String city = getAutoText(etCity);
        String priceStr = getText(etPrice);
        
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Vui lòng nhập tiêu đề");
            etTitle.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(city)) {
            etCity.setError("Vui lòng chọn thành phố");
            etCity.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Vui lòng nhập giá thuê");
            etPrice.requestFocus();
            return false;
        }
        
        try {
            Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError("Giá thuê không hợp lệ");
            etPrice.requestFocus();
            return false;
        }
                
        return true;
    }
    
    private void createRoom() {
        showLoading(true);
                
        Room room = new Room();
        room.setTitle(getText(etTitle));
        room.setDescription(getText(etDescription));
        room.setRoomType(mapRoomTypeToBackend(spinnerRoomType.getText().toString()));
        
        User.Address address = new User.Address();
        address.setCity(getAutoText(etCity));
        address.setDistrict(getAutoText(etDistrict));
        address.setWard(getAutoText(etWard));
        address.setStreet(getText(etStreet));
        room.setAddress(address);
        
        String areaStr = getText(etArea);
        if (!TextUtils.isEmpty(areaStr)) {
            try {
                room.setArea(Double.parseDouble(areaStr));
            } catch (NumberFormatException e) {
                room.setArea(0);
            }
        }
        
        Room.Price price = new Room.Price();
        try {
            price.setMonthly(Double.parseDouble(getText(etPrice)));
        } catch (NumberFormatException e) {
            price.setMonthly(0);
        }
        
        String depositStr = getText(etDeposit);
        if (!TextUtils.isEmpty(depositStr)) {
            try {
                price.setDeposit(Double.parseDouble(depositStr));
            } catch (NumberFormatException e) {
                price.setDeposit(0);
            }
        }
        
        String electricStr = getText(etElectricPrice);
        String waterStr = getText(etWaterPrice);
        String internetStr = getText(etInternetPrice);
        String otherStr = getText(etOtherPrice);
        
        double electricity = 0, water = 0, internet = 0, other = 0;
        
        if (!TextUtils.isEmpty(electricStr)) {
            try {
                electricity = Math.max(0, Double.parseDouble(electricStr));
            } catch (NumberFormatException e) {
                electricity = 0;
            }
        }
        
        if (!TextUtils.isEmpty(waterStr)) {
            try {
                water = Math.max(0, Double.parseDouble(waterStr));
            } catch (NumberFormatException e) {
                water = 0;
            }
        }
        
        if (!TextUtils.isEmpty(internetStr)) {
            try {
                internet = Math.max(0, Double.parseDouble(internetStr));
            } catch (NumberFormatException e) {
                internet = 0;
            }
        }
        
        if (!TextUtils.isEmpty(otherStr)) {
            try {
                other = Math.max(0, Double.parseDouble(otherStr));
            } catch (NumberFormatException e) {
                other = 0;
            }
        }
        
        double totalUtilities = electricity + water + internet + other;
        price.setUtilities(totalUtilities);
        
        room.setPrice(price);
        
        List<String> selectedAmenities = new ArrayList<>();
        for (int i = 0; i < layoutAmenities.getChildCount(); i++) {
            View child = layoutAmenities.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                if (checkBox.isChecked()) {
                    String displayAmenity = checkBox.getTag().toString();
                    String backendAmenity = mapAmenityToBackend(displayAmenity);
                    selectedAmenities.add(backendAmenity);
                }
            }
        }
        room.setAmenities(selectedAmenities);
        
        List<String> selectedRules = new ArrayList<>();
        for (int i = 0; i < layoutRules.getChildCount(); i++) {
            View child = layoutRules.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                if (checkBox.isChecked()) {
                    selectedRules.add(checkBox.getTag().toString());
                }
            }
        }
        room.setRules(selectedRules);
        
        Room.Availability availability = new Room.Availability();
        availability.setAvailable(true);
        room.setAvailability(availability);
        
        room.setStatus("active");
        
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                User currentUser = gson.fromJson(userJson, User.class);
                room.setLandlord(currentUser);
            } catch (Exception e) {
            }
        }
        
        Room.ContactInfo contactInfo = new Room.ContactInfo();
        if (room.getLandlord() != null) {
            contactInfo.setPhone(room.getLandlord().getPhone());
            contactInfo.setEmail(room.getLandlord().getEmail());
        }
        room.setContactInfo(contactInfo);
        
        
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().createRoom(token, room).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        
                        if (response.body().isSuccess()) {
                            Map<String, Object> responseData = response.body().getData();
                            
                            Room createdRoom = null;
                            
                            if (responseData != null && responseData.containsKey("room")) {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                Object roomData = responseData.get("room");
                                
                                String roomJson = gson.toJson(roomData);
                                
                                createdRoom = gson.fromJson(roomJson, Room.class);
                                
                                if (createdRoom.getId() == null && roomData instanceof Map) {
                                    Map<String, Object> roomMap = (Map<String, Object>) roomData;
                                    if (roomMap.containsKey("_id")) {
                                        String roomId = (String) roomMap.get("_id");
                                        createdRoom.setId(roomId);
                                    }
                                }
                                
                                if (roomJson.contains("\"_id\"")) {
                                } else {
                                }
                            } else {
                            }
                            
                            
                            if (createdRoom != null && createdRoom.getId() != null) {
                                if (!imageUris.isEmpty()) {
                                    uploadImages(createdRoom.getId());
                                } else {
                                    showLoading(false);
                                    Toast.makeText(AddRoomActivity.this, "Tạo phòng thành công!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } else {
                                showLoading(false);
                                if (createdRoom == null) {
                                } else if (createdRoom.getId() == null) {
                                }
                                Toast.makeText(AddRoomActivity.this, "Tạo phòng thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            showLoading(false);
                            String errorMsg = response.body().getMessage() != null ? response.body().getMessage() : "Tạo phòng thất bại";
                            Toast.makeText(AddRoomActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(AddRoomActivity.this, "Tạo phòng thất bại - Response body null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showLoading(false);
                    String errorBody = "";
                    try {
                        errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(AddRoomActivity.this, "Lỗi HTTP " + response.code() + ": " + errorBody, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AddRoomActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void uploadImages(String roomId) {
        
        if (roomId == null || roomId.trim().isEmpty()) {
            showLoading(false);
            Toast.makeText(this, "Lỗi: ID phòng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (imageUris.isEmpty()) {
            showLoading(false);
            Toast.makeText(this, "Không có ảnh để upload", Toast.LENGTH_SHORT).show();
            return;
        }
        
        List<MultipartBody.Part> parts = new ArrayList<>();
        
        for (int i = 0; i < imageUris.size(); i++) {
            Uri uri = imageUris.get(i);
            try {
                
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    continue;
                }
                
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] bytes = buffer.toByteArray();
                
                
                if (bytes.length == 0) {
                    continue;
                }
                
                RequestBody requestBody = RequestBody.create(bytes, MediaType.parse("image/*"));
                MultipartBody.Part part = MultipartBody.Part.createFormData("images", "image" + (i + 1) + ".jpg", requestBody);
                parts.add(part);
                
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().uploadRoomImages(token, roomId, parts).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AddRoomActivity.this, "Tạo phòng và upload ảnh thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Upload ảnh thất bại";
                    Toast.makeText(AddRoomActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AddRoomActivity.this, "Lỗi upload ảnh: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private String getAutoText(AutoCompleteTextView view) {
        return view.getText() != null ? view.getText().toString().trim() : "";
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
        btnPickImages.setEnabled(!show);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
