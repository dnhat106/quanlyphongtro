//fragment: màn hình quản lý phòng cho chủ trọ
// Mục đích file: File này dùng để quản lý các phòng trọ của chủ trọ
// function: 
// - onCreateView(): Khởi tạo view và setup các component
// - initViews(): Khởi tạo các view components
// - setupRecyclerView(): Thiết lập RecyclerView và adapter
// - setupSwipeRefresh(): Thiết lập chức năng pull-to-refresh
// - setupClickListeners(): Thiết lập các sự kiện click
// - loadRooms(): Tải danh sách phòng từ API
// - updateEmptyView(): Cập nhật trạng thái empty view
// - onAddRoomClick(): Xử lý click thêm phòng
// - onRoomClick(): Xử lý click vào phòng
// - onEditRoomClick(): Xử lý click chỉnh sửa phòng
// - onDeleteRoomClick(): Xử lý click xóa phòng
// - onToggleStatusClick(): Xử lý click thay đổi trạng thái phòng
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
package com.example.appquanlytimtro.landlord;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.rooms.RoomListActivity;
import com.example.appquanlytimtro.landlord.AddRoomActivity;
import com.example.appquanlytimtro.landlord.EditRoomActivity;
import com.example.appquanlytimtro.rooms.PostRoomFragment;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.models.Room;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.adapters.LandlordRoomAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandlordRoomManagementFragment extends Fragment implements LandlordRoomAdapter.OnRoomActionListener {

    private RecyclerView recyclerViewRooms;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialButton btnAddRoom;
    
    private RetrofitClient retrofitClient;
    private User currentUser;
    private LandlordRoomAdapter roomAdapter;
    private java.util.List<Room> roomList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_room_management, container, false);
        
        retrofitClient = RetrofitClient.getInstance(getContext());
        loadUserData();
        
        initViews(view);
        setupClickListeners();
        loadRooms();
        
        return view;
    }
    
    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, User.class);
        }
    }
    
    private void initViews(View view) {
        recyclerViewRooms = view.findViewById(R.id.recyclerViewRooms);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        btnAddRoom = view.findViewById(R.id.btnAddRoom);
        
        roomList = new java.util.ArrayList<>();
        roomAdapter = new LandlordRoomAdapter(roomList, this);
        
        if (recyclerViewRooms != null) {
            recyclerViewRooms.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerViewRooms.setAdapter(roomAdapter);
        }
        
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadRooms);
        }
    }
    
    private void setupClickListeners() {
        if (btnAddRoom != null) {
            btnAddRoom.setOnClickListener(v -> openAddRoom());
        }
    }
    
    private void openAddRoom() {
        Intent intent = new Intent(getActivity(), AddRoomActivity.class);
        startActivity(intent);
    }
    
    private void loadRooms() {
        if (currentUser == null) {
            return;
        }
        
        
        String token = "Bearer " + retrofitClient.getToken();
        
        java.util.Map<String, String> queryParams = new java.util.HashMap<>();
        
        retrofitClient.getApiService().getUserRooms(token, currentUser.getId(), queryParams).enqueue(new Callback<ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<java.util.Map<String, Object>>> call, Response<ApiResponse<java.util.Map<String, Object>>> response) {
                
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    java.util.Map<String, Object> data = response.body().getData();
                    
                    if (data != null && data.containsKey("rooms")) {
                        try {
                            Gson gson = new Gson();
                            java.util.List<?> roomsData = (java.util.List<?>) data.get("rooms");
                            
                            roomList.clear();
                            
                            int successCount = 0;
                            int errorCount = 0;
                            
                            for (int i = 0; i < roomsData.size(); i++) {
                                Object roomObj = roomsData.get(i);
                                try {
                                    String roomJson = gson.toJson(roomObj);
                                    
                                    Room room = gson.fromJson(roomJson, Room.class);
                                    if (room != null) {
                                        roomList.add(room);
                                        successCount++;
                                    } else {
                                        errorCount++;
                                    }
                                } catch (Exception e) {
                                    errorCount++;
                                }
                            }               
                            roomAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                        }
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Không có phòng nào", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không thể tải danh sách phòng", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    
    @Override
    public void onEditRoom(Room room) {
        Intent intent = new Intent(getActivity(), EditRoomActivity.class);
        intent.putExtra("room_id", room.getId());
        startActivity(intent);
    }
    
    @Override
    public void onDeleteRoom(Room room) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Xóa phòng")
                .setMessage("Bạn có chắc chắn muốn xóa phòng \"" + room.getTitle() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteRoom(room))
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void deleteRoom(Room room) {
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().deleteRoom(token, room.getId()).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Đã xóa phòng", Toast.LENGTH_SHORT).show();
                    }
                    loadRooms(); // Reload the list
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không thể xóa phòng", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
}
