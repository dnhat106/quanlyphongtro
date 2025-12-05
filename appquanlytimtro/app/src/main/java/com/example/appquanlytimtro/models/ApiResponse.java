//model: class đại diện cho phản hồi từ API
// Mục đích file: File này dùng để định nghĩa cấu trúc phản hồi chung từ API
// function: 
// - ApiResponse(): Constructor mặc định
// - ApiResponse(String, String, T): Constructor với tham số
// - getStatus(): Lấy trạng thái phản hồi
// - setStatus(): Thiết lập trạng thái phản hồi
// - getMessage(): Lấy thông báo phản hồi
// - setMessage(): Thiết lập thông báo phản hồi
// - getData(): Lấy dữ liệu phản hồi
// - setData(): Thiết lập dữ liệu phản hồi
// - isSuccess(): Kiểm tra phản hồi có thành công không
package com.example.appquanlytimtro.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("status")
    private String status;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private T data;

    public ApiResponse() {}

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return "success".equals(status);
    }
}
