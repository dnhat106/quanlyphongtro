//model: class đại diện cho thông tin thông báo
// Mục đích file: File này dùng để định nghĩa cấu trúc dữ liệu thông báo
// function: 
// - Notification(): Constructor mặc định
// - getId(): Lấy ID thông báo
// - setId(): Thiết lập ID thông báo
// - getRecipientId(): Lấy ID người nhận
// - setRecipientId(): Thiết lập ID người nhận
// - getType(): Lấy loại thông báo
// - setType(): Thiết lập loại thông báo
// - getTitle(): Lấy tiêu đề thông báo
// - setTitle(): Thiết lập tiêu đề thông báo
// - getMessage(): Lấy nội dung thông báo
// - setMessage(): Thiết lập nội dung thông báo
// - isRead(): Kiểm tra đã đọc chưa
// - setRead(): Thiết lập trạng thái đã đọc
// - getCreatedAt(): Lấy thời gian tạo
// - setCreatedAt(): Thiết lập thời gian tạo
package com.example.appquanlytimtro.models;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("recipient")
    private String recipientId;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private NotificationData data;
    
    @SerializedName("isRead")
    private boolean isRead;
    
    @SerializedName("readAt")
    private String readAt;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("updatedAt")
    private String updatedAt;
    
    @SerializedName("recipient")
    private User recipient;

    public Notification() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationData getData() {
        return data;
    }

    public void setData(NotificationData data) {
        this.data = data;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getReadAt() {
        return readAt;
    }

    public void setReadAt(String readAt) {
        this.readAt = readAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public static class NotificationData {
        @SerializedName("bookingId")
        private String bookingId;
        
        @SerializedName("roomId")
        private String roomId;
        
        @SerializedName("paymentId")
        private String paymentId;
        
        @SerializedName("amount")
        private double amount;
        
        @SerializedName("tenantId")
        private String tenantId;
        
        @SerializedName("landlordId")
        private String landlordId;

        public String getBookingId() {
            return bookingId;
        }

        public void setBookingId(String bookingId) {
            this.bookingId = bookingId;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getPaymentId() {
            return paymentId;
        }

        public void setPaymentId(String paymentId) {
            this.paymentId = paymentId;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getLandlordId() {
            return landlordId;
        }

        public void setLandlordId(String landlordId) {
            this.landlordId = landlordId;
        }
    }
}
