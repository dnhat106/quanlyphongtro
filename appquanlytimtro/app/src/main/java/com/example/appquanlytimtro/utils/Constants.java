//class: chứa các hằng số của ứng dụng
// Mục đích file: File này dùng để định nghĩa các hằng số được sử dụng trong toàn bộ ứng dụng
// function: 
// - Constants(): Constructor mặc định (không sử dụng)
// - BASE_URL: URL cơ sở của API
// - ROLE_ADMIN: Vai trò admin
// - ROLE_LANDLORD: Vai trò chủ trọ
// - ROLE_TENANT: Vai trò người thuê trọ
// - ROOM_TYPE_STUDIO: Loại phòng studio
// - ROOM_TYPE_1_BEDROOM: Loại phòng 1 phòng ngủ
// - ROOM_TYPE_2_BEDROOM: Loại phòng 2 phòng ngủ
// - ROOM_TYPE_3_BEDROOM: Loại phòng 3 phòng ngủ
// - ROOM_TYPE_SHARED: Loại phòng chung
// - BOOKING_STATUS_PENDING: Trạng thái đặt phòng chờ xác nhận
// - BOOKING_STATUS_CONFIRMED: Trạng thái đặt phòng đã xác nhận
// - BOOKING_STATUS_DEPOSIT_PAID: Trạng thái đặt phòng đã thanh toán cọc
// - BOOKING_STATUS_ACTIVE: Trạng thái đặt phòng đang hoạt động
// - BOOKING_STATUS_COMPLETED: Trạng thái đặt phòng đã hoàn thành
// - BOOKING_STATUS_CANCELLED: Trạng thái đặt phòng đã hủy
// - PAYMENT_STATUS_PENDING: Trạng thái thanh toán chờ xử lý
// - PAYMENT_STATUS_COMPLETED: Trạng thái thanh toán đã hoàn thành
// - PAYMENT_STATUS_FAILED: Trạng thái thanh toán thất bại
// - PAYMENT_STATUS_REFUNDED: Trạng thái thanh toán đã hoàn tiền
package com.example.appquanlytimtro.utils;

public class Constants {
    public static final String BASE_URL = "http://10.0.2.2:5000/api/";
    
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_LANDLORD = "landlord";
    public static final String ROLE_TENANT = "tenant";
    
    public static final String ROOM_TYPE_STUDIO = "studio";
    public static final String ROOM_TYPE_1_BEDROOM = "1_bedroom";
    public static final String ROOM_TYPE_2_BEDROOM = "2_bedroom";
    public static final String ROOM_TYPE_3_BEDROOM = "3_bedroom";
    public static final String ROOM_TYPE_SHARED = "shared";
    
    public static final String BOOKING_STATUS_PENDING = "pending";
    public static final String BOOKING_STATUS_CONFIRMED = "confirmed";
    public static final String BOOKING_STATUS_DEPOSIT_PAID = "deposit_paid";
    public static final String BOOKING_STATUS_ACTIVE = "active";
    public static final String BOOKING_STATUS_COMPLETED = "completed";
    public static final String BOOKING_STATUS_CANCELLED = "cancelled";
    
    public static final String PREFS_NAME = "app_prefs";
    public static final String TOKEN_KEY = "auth_token";
    public static final String USER_DATA_KEY = "user_data";
}