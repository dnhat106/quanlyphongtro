//config: file cấu hình các thông số cho VNPay
// Mục đích file: File này chứa các cấu hình cần thiết để tích hợp với VNPay
// function: 
// - isConfigured(): Kiểm tra xem VNPay đã được cấu hình chưa
// - getConfigurationMessage(): Lấy thông báo trạng thái cấu hình
package com.example.appquanlytimtro.config;

/**
 * Cấu hình VNPay
 * 
 * HƯỚNG DẪN CẤU HÌNH:
 * 1. Đăng ký tài khoản tại: https://sandbox.vnpayment.vn/
 * 2. Lấy TMN_CODE và HASH_SECRET từ dashboard
 * 3. Thay thế các giá trị bên dưới
 * 4. Đảm bảo bảo mật thông tin credentials
 */
public class VNPayConfig {
    
    // VNPay Sandbox URL
    public static final String VNPAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    
    // Return URL cho app (đã cấu hình trong AndroidManifest.xml)
    public static final String VNPAY_RETURN_URL = "com.example.appquanlytimtro://vnpay";
    
    // VNPay credentials - đã cấu hình
    public static final String VNPAY_TMN_CODE = "L6S8YHK4";
    public static final String VNPAY_HASH_SECRET = "JFP6X1C4JK3GV8TFVKAQDWF2F11P13BB";
    
    // VNPay API parameters
    public static final String VERSION = "2.1.0";
    public static final String CURRENCY_CODE = "VND";
    public static final String LOCALE = "vn";
    public static final String COMMAND = "pay";
    
    // Timeout cho giao dịch (phút)
    public static final int TRANSACTION_TIMEOUT = 15;
    
    /**
     * Kiểm tra xem đã cấu hình credentials chưa
     */
    public static boolean isConfigured() {
        return !VNPAY_TMN_CODE.equals("YOUR_TMN_CODE") && 
               !VNPAY_HASH_SECRET.equals("YOUR_HASH_SECRET");
    }
    
    /**
     * Lấy thông báo cấu hình
     */
    public static String getConfigurationMessage() {
        if (isConfigured()) {
            return "VNPay đã được cấu hình";
        } else {
            return "Cần cấu hình VNPay credentials trong VNPayConfig.java";
        }
    }
}
