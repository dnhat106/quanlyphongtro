//model: class đại diện cho phản hồi đăng ký
// Mục đích file: File này dùng để định nghĩa cấu trúc dữ liệu phản hồi đăng ký
// function: 
// - RegisterResponse(): Constructor mặc định
// - RegisterResponse(User, String): Constructor với user và token
// - getUser(): Lấy thông tin user
// - setUser(): Thiết lập thông tin user
// - getToken(): Lấy token
// - setToken(): Thiết lập token
package com.example.appquanlytimtro.models;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {
    @SerializedName("user")
    private User user;
    
    @SerializedName("token")
    private String token;

    public RegisterResponse() {}

    public RegisterResponse(User user, String token) {
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
