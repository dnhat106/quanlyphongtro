//model: class đại diện cho item thanh toán
// Mục đích file: File này dùng để định nghĩa cấu trúc dữ liệu item thanh toán
// function: 
// - PaymentItem(): Constructor mặc định
// - getId(): Lấy ID item thanh toán
// - setId(): Thiết lập ID item thanh toán
// - getType(): Lấy loại item thanh toán
// - setType(): Thiết lập loại item thanh toán
// - getStatus(): Lấy trạng thái item thanh toán
// - setStatus(): Thiết lập trạng thái item thanh toán
// - getAmount(): Lấy số tiền
// - setAmount(): Thiết lập số tiền
// - getDescription(): Lấy mô tả
// - setDescription(): Thiết lập mô tả
// - getDate(): Lấy ngày tháng
// - setDate(): Thiết lập ngày tháng
package com.example.appquanlytimtro.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaymentItem {
    @SerializedName("id")
    private String id;
    
    @SerializedName("type")
    private String type;
    
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("amount")
    private double amount;
    
    @SerializedName("paymentMethod")
    private String paymentMethod;
    
    @SerializedName("initiatedAt")
    private String initiatedAt;
    
    @SerializedName("completedAt")
    private String completedAt;
    
    @SerializedName("payer")
    private User payer;
    
    @SerializedName("recipient")
    private User recipient;
    
    @SerializedName("booking")
    private Booking booking;
    
    public PaymentItem(Payment payment) {
        this.id = payment.getId();
        this.type = "payment";
        this.status = payment.getStatus();
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.initiatedAt = payment.getInitiatedAt();
        this.completedAt = payment.getCompletedAt();
        this.payer = payment.getPayer();
        this.recipient = payment.getRecipient();
        this.booking = payment.getBooking();
    }
    
    public PaymentItem(Booking booking) {
        this.id = booking.getId();
        this.type = "booking";
        this.status = "pending";
        this.amount = booking.getPricing() != null ? booking.getPricing().getDeposit() : 0;
        this.paymentMethod = "pending";
        this.initiatedAt = booking.getCreatedAt();
        this.completedAt = null;
        this.payer = booking.getTenant();
        this.recipient = booking.getLandlord();
        this.booking = booking;
    }
    
    public String getId() { return id; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getInitiatedAt() { return initiatedAt; }
    public String getCompletedAt() { return completedAt; }
    public User getPayer() { return payer; }
    public User getRecipient() { return recipient; }
    public Booking getBooking() { return booking; }
    
    public boolean isPayment() {
        return "payment".equals(type);
    }
    
    public boolean isBooking() {
        return "booking".equals(type);
    }
    
    public String getStatusText() {
        if (isBooking()) {
            return "Chưa thanh toán";
        } else {
            switch (status) {
                case "completed":
                    return "Đã thanh toán";
                case "pending":
                    return "Đang xử lý";
                case "failed":
                    return "Thất bại";
                default:
                    return status;
            }
        }
    }
    
    public String getPaymentMethodText() {
        if (isBooking()) {
            return "Chưa thanh toán";
        } else {
            switch (paymentMethod) {
                case "vnpay":
                    return "VNPay";
                case "bank_transfer":
                    return "Chuyển khoản";
                case "cash":
                    return "Tiền mặt";
                default:
                    return paymentMethod;
            }
        }
    }
}
