//model: class đại diện cho yêu cầu đăng ký
// Mục đích file: File này dùng để định nghĩa cấu trúc dữ liệu yêu cầu đăng ký
// function: 
// - RegisterRequest(): Constructor mặc định
// - RegisterRequest(String, String, String, String, String): Constructor với đầy đủ thông tin
// - getFullName(): Lấy họ tên
// - setFullName(): Thiết lập họ tên
// - getEmail(): Lấy email
// - setEmail(): Thiết lập email
// - getPassword(): Lấy mật khẩu
// - setPassword(): Thiết lập mật khẩu
// - getPhone(): Lấy số điện thoại
// - setPhone(): Thiết lập số điện thoại
// - getRole(): Lấy vai trò
// - setRole(): Thiết lập vai trò
package com.example.appquanlytimtro.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("role")
    private String role;

    public RegisterRequest() {}

    public RegisterRequest(String fullName, String email, String password, String phone, String role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
