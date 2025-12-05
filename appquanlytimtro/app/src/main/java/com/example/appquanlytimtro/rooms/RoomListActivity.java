//activity: màn hình danh sách phòng trọ
// Mục đích file: File này dùng để hiển thị danh sách các phòng trọ
// function: 
// - onCreate(): Khởi tạo activity và setup các component
// - initViews(): Khởi tạo các view components
// - setupToolbar(): Thiết lập toolbar với menu
// - setupRecyclerView(): Thiết lập RecyclerView và adapter
// - setupSwipeRefresh(): Thiết lập chức năng pull-to-refresh
// - setupSearch(): Thiết lập chức năng tìm kiếm
// - loadRooms(): Tải danh sách phòng từ API
// - searchRooms(): Tìm kiếm phòng
// - filterRooms(): Lọc phòng theo tiêu chí
// - updateEmptyView(): Cập nhật trạng thái empty view
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
// - onRoomClick(): Xử lý click vào phòng
// - onCreateOptionsMenu(): Tạo menu options
// - onOptionsItemSelected(): Xử lý click vào menu item
package com.example.appquanlytimtro.rooms;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.RoomAdapter;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Room;
import com.google.gson.Gson;
import com.example.appquanlytimtro.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomListActivity extends AppCompatActivity implements RoomAdapter.OnRoomClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private RoomAdapter roomAdapter;
    private List<Room> rooms;
    private RetrofitClient retrofitClient;
    private boolean showMyRooms = false;
    private boolean showAvailableOnly = false;

    private TextInputEditText etSearch;
    private MaterialButton btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        initViews();
        setupToolbar();
        setupSearchListeners();

        retrofitClient = RetrofitClient.getInstance(this);

        Intent intent = getIntent();
        if (intent != null) {
            showMyRooms = intent.getBooleanExtra("show_my_rooms", false);
            showAvailableOnly = intent.getBooleanExtra("show_available_only", false);

            if (showMyRooms) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Phòng trọ của tôi");
                }
            } else if (showAvailableOnly) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Phòng trọ có sẵn");
                }
            }
        }

        setupRecyclerView();
        setupSwipeRefresh();
        loadRooms("");
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);

        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Danh sách phòng trọ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        rooms = new ArrayList<>();
        roomAdapter = new RoomAdapter(rooms, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(roomAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            String searchQuery = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
            loadRooms(searchQuery);
        });
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    private void setupSearchListeners() {
        btnSearch.setOnClickListener(v -> performSearch());

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });
    }

    private void performSearch() {
        String searchQuery = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        loadRooms(searchQuery);
    }


    private void loadRooms(String searchQuery) {
        showLoading(true);

        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("limit", "20");

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String query = searchQuery.trim();
            Log.d("RoomListActivity", "Search query: " + query);

            if (query.contains("-")) {
                String[] parts = query.split("-", 2);
                if (parts.length == 2) {
                    String city = parts[0].trim();
                    String district = parts[1].trim();
                    Log.d("RoomListActivity", "Split - city: " + city + ", district: " + district);
                    if (!city.isEmpty()) {
                        params.put("city", city);
                    }
                    if (!district.isEmpty()) {
                        params.put("district", district);
                    }
                }
            } else {
                try {
                    double price = Double.parseDouble(query);
                    double minPrice = Math.max(0, price - 200000);
                    double maxPrice = price + 200000;
                    params.put("minPrice", String.valueOf((int)minPrice));
                    params.put("maxPrice", String.valueOf((int)maxPrice));
                    Log.d("RoomListActivity", "Price search - min: " + minPrice + ", max: " + maxPrice);
                } catch (NumberFormatException e) {
                    params.put("city", query);
                    Log.d("RoomListActivity", "City search: " + query);
                }
            }
        }

        Log.d("RoomListActivity", "Request params: " + params.toString());

        if (showAvailableOnly) {
            params.put("status", "active");
            params.put("available", "true");
            params.put("excludeBooked", "true");
        }

        Call<ApiResponse<Map<String, Object>>> call = null;

        if (showMyRooms) {
            String userId = getCurrentUserId();
            if (userId != null) {
                call = retrofitClient.getApiService().getUserRooms(
                        retrofitClient.getToken(),
                        userId,
                        params
                );
            } else {
                showError("Không thể lấy thông tin người dùng");
                showLoading(false);
                return;
            }
        } else {
            call = retrofitClient.getApiService().getRooms(params);
        }

        if (call != null) {
            call.enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);

                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Map<String, Object>> apiResponse = response.body();

                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            Map<String, Object> data = apiResponse.getData();
                            List<?> roomsData = (List<?>) data.get("rooms");

                            if (roomsData != null) {
                                Log.d("RoomListActivity", "API returned " + roomsData.size() + " rooms");
                                rooms.clear();
                                Gson gson = new Gson();
                                for (Object roomObj : roomsData) {
                                    if (roomObj instanceof Map) {
                                        Room room = gson.fromJson(gson.toJson(roomObj), Room.class);
                                        rooms.add(room);
                                        if (room.getAddress() != null) {
                                            Log.d("RoomListActivity", "Room: " + room.getTitle() +
                                                    ", City: " + room.getAddress().getCity() +
                                                    ", District: " + room.getAddress().getDistrict());
                                        }
                                    }
                                }
                                Log.d("RoomListActivity", "Total rooms after processing: " + rooms.size());
                                roomAdapter.notifyDataSetChanged();
                            } else {
                                Log.d("RoomListActivity", "roomsData is null");
                            }
                        } else {
                            showError(apiResponse.getMessage());
                        }
                    } else {
                        showError("Không thể tải danh sách phòng trọ");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);
                    showError("Lỗi kết nối. Vui lòng thử lại.");
                }
            });
        }
    }

    private String getCurrentUserId() {
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.example.appquanlytimtro.models.User user = gson.fromJson(userJson, com.example.appquanlytimtro.models.User.class);
                return user != null ? user.getId() : null;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void onRoomClick(Room room) {
        Intent intent = new Intent(this, RoomDetailActivity.class);
        intent.putExtra("room_id", room.getId());
        intent.putExtra("room_object", room);
        startActivity(intent);
    }

    @Override
    public void onRoomDelete(Room room) {
    }

    public void onRoomLike(Room room) {
        retrofitClient.getApiService().toggleRoomLike(retrofitClient.getToken(), room.getId())
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                int position = rooms.indexOf(room);
                                if (position != -1) {
                                    roomAdapter.notifyItemChanged(position);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (showMyRooms) {
            getMenuInflater().inflate(R.menu.room_list_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_add_room) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
