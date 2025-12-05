//model: class đại diện cho yêu cầu đăng nhập
// Mục đích file: File này dùng để định nghĩa cấu trúc dữ liệu yêu cầu đăng nhập
// function: 
// - LoginRequest(): Constructor mặc định
// - LoginRequest(String, String): Constructor với email và password
// - getEmail(): Lấy email
// - setEmail(): Thiết lập email
// - getPassword(): Lấy mật khẩu
// - setPassword(): Thiết lập mật khẩu
package com.example.appquanlytimtro.models;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("email")
    private String email;
    
    @SerializedName("password")
    private String password;

    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
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
}
