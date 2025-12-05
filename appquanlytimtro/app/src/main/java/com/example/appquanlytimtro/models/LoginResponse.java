//model: class đại diện cho phản hồi đăng nhập
// Mục đích file: File này dùng để định nghĩa cấu trúc dữ liệu phản hồi đăng nhập
// function: 
// - LoginResponse(): Constructor mặc định
// - LoginResponse(User, String): Constructor với user và token
// - getUser(): Lấy thông tin user
// - setUser(): Thiết lập thông tin user
// - getToken(): Lấy token
// - setToken(): Thiết lập token
package com.example.appquanlytimtro.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("user")
    private User user;
    
    @SerializedName("token")
    private String token;

    public LoginResponse() {}

    public LoginResponse(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
