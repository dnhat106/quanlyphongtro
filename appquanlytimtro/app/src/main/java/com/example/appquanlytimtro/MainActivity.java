//activity: màn hình chính của ứng dụng
// Mục đích file: File này dùng để quản lý màn hình chính và điều hướng giữa các fragment
// function: 
// - onCreate(): Khởi tạo activity và setup các component
// - initViews(): Khởi tạo các view components
// - setupToolbar(): Thiết lập toolbar với menu
// - setupBottomNavigation(): Thiết lập bottom navigation
// - loadUserData(): Tải thông tin user hiện tại
// - setupFragments(): Thiết lập các fragment
// - showFragment(): Hiển thị fragment được chọn
// - onNavigationItemSelected(): Xử lý chọn item trong bottom navigation
// - onCreateOptionsMenu(): Tạo menu options
// - onOptionsItemSelected(): Xử lý click vào menu item
// - onProfileClick(): Xử lý click vào profile
// - onLogoutClick(): Xử lý click đăng xuất
// - onBookingsClick(): Xử lý click vào đặt phòng
// - onPaymentsClick(): Xử lý click vào thanh toán
// - onRoomsClick(): Xử lý click vào phòng trọ
// - logout(): Thực hiện đăng xuất
// - navigateToLogin(): Chuyển đến màn hình đăng nhập
package com.example.appquanlytimtro;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.appquanlytimtro.auth.LoginActivity;
import com.example.appquanlytimtro.bookings.BookingListActivity;
import com.example.appquanlytimtro.payments.PaymentListActivity;
import com.example.appquanlytimtro.rooms.RoomListActivity;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.example.appquanlytimtro.profile.ProfileFragment;
import com.example.appquanlytimtro.admin.AdminUsersFragment;
import com.example.appquanlytimtro.admin.AdminRoomsFragment;
import com.example.appquanlytimtro.tenant.TenantHomeFragment;
import com.example.appquanlytimtro.landlord.LandlordDashboardFragment;
import com.example.appquanlytimtro.landlord.LandlordRoomManagementFragment;
import com.example.appquanlytimtro.landlord.LandlordBookingManagementFragment;
import com.example.appquanlytimtro.landlord.LandlordPaymentManagementFragment;
import com.example.appquanlytimtro.admin.AdminDashboardFragment;
import com.example.appquanlytimtro.admin.AdminBookingManagementFragment;
import com.example.appquanlytimtro.admin.AdminPaymentManagementFragment;

public class MainActivity extends AppCompatActivity {

    private RetrofitClient retrofitClient;
    private User currentUser;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupToolbar();
        
        retrofitClient = RetrofitClient.getInstance(this);

        if (!retrofitClient.isLoggedIn()) {
            navigateToLogin();
            return;
        }
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            loadUserData();
            setupBottomNavigation();
        }, 100);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if (!retrofitClient.isLoggedIn()) {
            navigateToLogin();
            return;
        }
        
        if (currentUser == null) {
            loadUserData();
        }
    }
    
    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (bottomNavigationView == null) {
            navigateToLogin();
            return;
        }
        
    }
    
    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản Lý Tìm Trọ");
        }
        
    }
    
    
    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        
        if (userJson != null && !userJson.isEmpty()) {
            try {
                Gson gson = new Gson();
                currentUser = gson.fromJson(userJson, User.class);
                
                
                if (currentUser != null && currentUser.getRole() != null) {
                    initBottomMenuByRole(currentUser.getRole());
                    
                    loadDefaultFragment();
                    
                    if (Constants.ROLE_TENANT.equals(currentUser.getRole())) {
                        bottomNavigationView.setSelectedItemId(R.id.nav_home);
                    } else {
                        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
                    }
                } else {
                    navigateToLogin();
                }
            } catch (Exception e) {
                navigateToLogin();
            }
        } else {
            navigateToLogin();
        }
    }
    
    private void loadDefaultFragment() {
        if (currentUser == null) return;
        
        String role = currentUser.getRole();
        Fragment defaultFragment = null;
        
        if (Constants.ROLE_TENANT.equals(role)) {
            defaultFragment = new TenantHomeFragment();
        } else if (Constants.ROLE_LANDLORD.equals(role)) {
            defaultFragment = new LandlordDashboardFragment();
        } else if (Constants.ROLE_ADMIN.equals(role)) {
            defaultFragment = new AdminDashboardFragment();
        }
        
        if (defaultFragment != null) {
            switchFragment(defaultFragment);
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) {
            return;
        }
        
        
        if (currentUser != null) {
            initBottomMenuByRole(currentUser.getRole());
        }
        
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            String role = currentUser != null ? currentUser.getRole() : "";
            
            if (id == R.id.nav_home || id == R.id.nav_dashboard) {
                if (Constants.ROLE_TENANT.equals(role)) {
                    switchFragment(new TenantHomeFragment());
                } else if (Constants.ROLE_LANDLORD.equals(role)) {
                    switchFragment(new LandlordDashboardFragment());
                } else if (Constants.ROLE_ADMIN.equals(role)) {
                    switchFragment(new AdminDashboardFragment());
                }
                return true;
            } else if (id == R.id.nav_search) {
                if (Constants.ROLE_TENANT.equals(role)) {
                    Intent intent = new Intent(this, RoomListActivity.class);
                    intent.putExtra("show_available_only", true);
                    startActivity(intent);
                }
                return true;
            } else if (id == R.id.nav_manage_rooms || id == R.id.nav_rooms) {
                if (Constants.ROLE_LANDLORD.equals(role)) {
                    switchFragment(new LandlordRoomManagementFragment());
                } else if (Constants.ROLE_ADMIN.equals(role)) {
                    switchFragment(new AdminRoomsFragment());
                }
                return true;
            } else if (id == R.id.nav_bookings) {
                if (Constants.ROLE_TENANT.equals(role)) {
                    Intent intent = new Intent(this, BookingListActivity.class);
                    startActivity(intent);
                } else if (Constants.ROLE_LANDLORD.equals(role)) {
                    switchFragment(new LandlordBookingManagementFragment());
                }
                return true;
            } else if (id == R.id.nav_payments) {
                if (Constants.ROLE_TENANT.equals(role)) {
                    Intent intent = new Intent(this, PaymentListActivity.class);
                    startActivity(intent);
                } else if (Constants.ROLE_LANDLORD.equals(role)) {
                    switchFragment(new LandlordPaymentManagementFragment());
                } else if (Constants.ROLE_ADMIN.equals(role)) {
                    switchFragment(new AdminPaymentManagementFragment());
                }
                return true;
            } else if (id == R.id.nav_users) {
                if (Constants.ROLE_ADMIN.equals(role)) {
                    switchFragment(new AdminUsersFragment());
                }
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void initBottomMenuByRole(String role) {
        if (bottomNavigationView == null) return;
        if (Constants.ROLE_TENANT.equals(role)) {
            bottomNavigationView.getMenu().clear();
            getMenuInflater().inflate(R.menu.menu_tenant, bottomNavigationView.getMenu());
        } else if (Constants.ROLE_LANDLORD.equals(role)) {
            bottomNavigationView.getMenu().clear();
            getMenuInflater().inflate(R.menu.menu_landlord, bottomNavigationView.getMenu());
        } else if (Constants.ROLE_ADMIN.equals(role)) {
            bottomNavigationView.getMenu().clear();
            getMenuInflater().inflate(R.menu.menu_admin, bottomNavigationView.getMenu());
        }
    }

    private void switchFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }
    
    
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    public void logout() {
        retrofitClient.logout();
        navigateToLogin();
    }
    
    public void navigateToFragment(int position) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(bottomNavigationView.getMenu().getItem(position).getItemId());
        }
    }
}