//model: class đại diện cho thông tin thanh toán
// Mục đích file: File này dùng để định nghĩa cấu trúc dữ liệu thanh toán
// function: 
// - Payment(): Constructor mặc định
// - getId(): Lấy ID thanh toán
// - setId(): Thiết lập ID thanh toán
// - getBookingId(): Lấy ID đặt phòng
// - setBookingId(): Thiết lập ID đặt phòng
// - getPayerId(): Lấy ID người thanh toán
// - setPayerId(): Thiết lập ID người thanh toán
// - getRecipientId(): Lấy ID người nhận
// - setRecipientId(): Thiết lập ID người nhận
// - getType(): Lấy loại thanh toán
// - setType(): Thiết lập loại thanh toán
// - getAmount(): Lấy số tiền
// - setAmount(): Thiết lập số tiền
// - getStatus(): Lấy trạng thái thanh toán
// - setStatus(): Thiết lập trạng thái thanh toán
// - getPaymentMethod(): Lấy phương thức thanh toán
// - setPaymentMethod(): Thiết lập phương thức thanh toán
// - getTransactionId(): Lấy ID giao dịch
// - setTransactionId(): Thiết lập ID giao dịch
// - getCreatedAt(): Lấy thời gian tạo
// - setCreatedAt(): Thiết lập thời gian tạo
// - getUpdatedAt(): Lấy thời gian cập nhật
// - setUpdatedAt(): Thiết lập thời gian cập nhật
package com.example.appquanlytimtro.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Payment {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("bookingId")
    private String bookingId;
    
    @SerializedName("payerId")
    private String payerId;
    
    @SerializedName("recipientId")
    private String recipientId;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("amount")
    private double amount;
    
    @SerializedName("currency")
    private String currency;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("paymentMethod")
    private String paymentMethod;
    
    @SerializedName("vnpay")
    private VNPayInfo vnpay;
    
    @SerializedName("bankTransfer")
    private BankTransferInfo bankTransfer;
    
    @SerializedName("transactionId")
    private String transactionId;
    
    @SerializedName("externalTransactionId")
    private String externalTransactionId;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("notes")
    private String notes;
    
    @SerializedName("initiatedAt")
    private String initiatedAt;
    
    @SerializedName("processedAt")
    private String processedAt;
    
    @SerializedName("completedAt")
    private String completedAt;
    
    @SerializedName("failedAt")
    private String failedAt;
    
    @SerializedName("failureReason")
    private String failureReason;
    
    @SerializedName("refund")
    private RefundInfo refund;
    
    @SerializedName("fees")
    private Fees fees;
    
    @SerializedName("metadata")
    private Metadata metadata;
    
    @SerializedName("booking")
    private Booking booking;
    
    @SerializedName("payer")
    private User payer;
    
    @SerializedName("recipient")
    private User recipient;

    public Payment() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public VNPayInfo getVnpay() {
        return vnpay;
    }

    public void setVnpay(VNPayInfo vnpay) {
        this.vnpay = vnpay;
    }

    public BankTransferInfo getBankTransfer() {
        return bankTransfer;
    }

    public void setBankTransfer(BankTransferInfo bankTransfer) {
        this.bankTransfer = bankTransfer;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getInitiatedAt() {
        return initiatedAt;
    }

    public void setInitiatedAt(String initiatedAt) {
        this.initiatedAt = initiatedAt;
    }

    public String getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(String processedAt) {
        this.processedAt = processedAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public String getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(String failedAt) {
        this.failedAt = failedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public RefundInfo getRefund() {
        return refund;
    }

    public void setRefund(RefundInfo refund) {
        this.refund = refund;
    }

    public Fees getFees() {
        return fees;
    }

    public void setFees(Fees fees) {
        this.fees = fees;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public User getPayer() {
        return payer;
    }

    public void setPayer(User payer) {
        this.payer = payer;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public static class VNPayInfo {
        @SerializedName("txnRef")
        private String txnRef;
        
        @SerializedName("orderInfo")
        private String orderInfo;
        
        @SerializedName("orderType")
        private String orderType;
        
        @SerializedName("amount")
        private double amount;
        
        @SerializedName("locale")
        private String locale;
        
        @SerializedName("currCode")
        private String currCode;
        
        @SerializedName("returnUrl")
        private String returnUrl;
        
        @SerializedName("ipAddr")
        private String ipAddr;
        
        @SerializedName("createDate")
        private String createDate;
        
        @SerializedName("expireDate")
        private String expireDate;
        
        @SerializedName("vnpTxnRef")
        private String vnpTxnRef;
        
        @SerializedName("vnpAmount")
        private double vnpAmount;
        
        @SerializedName("vnpOrderInfo")
        private String vnpOrderInfo;
        
        @SerializedName("vnpResponseCode")
        private String vnpResponseCode;
        
        @SerializedName("vnpTransactionNo")
        private String vnpTransactionNo;
        
        @SerializedName("vnpTransactionStatus")
        private String vnpTransactionStatus;
        
        @SerializedName("vnpSecureHash")
        private String vnpSecureHash;
        
        @SerializedName("vnpSecureHashType")
        private String vnpSecureHashType;
        
        @SerializedName("vnpBankCode")
        private String vnpBankCode;
        
        @SerializedName("vnpCardType")
        private String vnpCardType;
        
        @SerializedName("vnpPayDate")
        private String vnpPayDate;

        public String getTxnRef() { return txnRef; }
        public void setTxnRef(String txnRef) { this.txnRef = txnRef; }
        
        public String getOrderInfo() { return orderInfo; }
        public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }
        
        public String getOrderType() { return orderType; }
        public void setOrderType(String orderType) { this.orderType = orderType; }
        
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        
        public String getLocale() { return locale; }
        public void setLocale(String locale) { this.locale = locale; }
        
        public String getCurrCode() { return currCode; }
        public void setCurrCode(String currCode) { this.currCode = currCode; }
        
        public String getReturnUrl() { return returnUrl; }
        public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
        
        public String getIpAddr() { return ipAddr; }
        public void setIpAddr(String ipAddr) { this.ipAddr = ipAddr; }
        
        public String getCreateDate() { return createDate; }
        public void setCreateDate(String createDate) { this.createDate = createDate; }
        
        public String getExpireDate() { return expireDate; }
        public void setExpireDate(String expireDate) { this.expireDate = expireDate; }
        
        public String getVnpTxnRef() { return vnpTxnRef; }
        public void setVnpTxnRef(String vnpTxnRef) { this.vnpTxnRef = vnpTxnRef; }
        
        public double getVnpAmount() { return vnpAmount; }
        public void setVnpAmount(double vnpAmount) { this.vnpAmount = vnpAmount; }
        
        public String getVnpOrderInfo() { return vnpOrderInfo; }
        public void setVnpOrderInfo(String vnpOrderInfo) { this.vnpOrderInfo = vnpOrderInfo; }
        
        public String getVnpResponseCode() { return vnpResponseCode; }
        public void setVnpResponseCode(String vnpResponseCode) { this.vnpResponseCode = vnpResponseCode; }
        
        public String getVnpTransactionNo() { return vnpTransactionNo; }
        public void setVnpTransactionNo(String vnpTransactionNo) { this.vnpTransactionNo = vnpTransactionNo; }
        
        public String getVnpTransactionStatus() { return vnpTransactionStatus; }
        public void setVnpTransactionStatus(String vnpTransactionStatus) { this.vnpTransactionStatus = vnpTransactionStatus; }
        
        public String getVnpSecureHash() { return vnpSecureHash; }
        public void setVnpSecureHash(String vnpSecureHash) { this.vnpSecureHash = vnpSecureHash; }
        
        public String getVnpSecureHashType() { return vnpSecureHashType; }
        public void setVnpSecureHashType(String vnpSecureHashType) { this.vnpSecureHashType = vnpSecureHashType; }
        
        public String getVnpBankCode() { return vnpBankCode; }
        public void setVnpBankCode(String vnpBankCode) { this.vnpBankCode = vnpBankCode; }
        
        public String getVnpCardType() { return vnpCardType; }
        public void setVnpCardType(String vnpCardType) { this.vnpCardType = vnpCardType; }
        
        public String getVnpPayDate() { return vnpPayDate; }
        public void setVnpPayDate(String vnpPayDate) { this.vnpPayDate = vnpPayDate; }
    }

    public static class BankTransferInfo {
        @SerializedName("bankName")
        private String bankName;
        
        @SerializedName("accountNumber")
        private String accountNumber;
        
        @SerializedName("accountHolder")
        private String accountHolder;
        
        @SerializedName("transferNote")
        private String transferNote;
        
        @SerializedName("transferDate")
        private String transferDate;
        
        @SerializedName("receiptImage")
        private String receiptImage;

        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        
        public String getAccountHolder() { return accountHolder; }
        public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }
        
        public String getTransferNote() { return transferNote; }
        public void setTransferNote(String transferNote) { this.transferNote = transferNote; }
        
        public String getTransferDate() { return transferDate; }
        public void setTransferDate(String transferDate) { this.transferDate = transferDate; }
        
        public String getReceiptImage() { return receiptImage; }
        public void setReceiptImage(String receiptImage) { this.receiptImage = receiptImage; }
    }

    public static class RefundInfo {
        @SerializedName("refundAmount")
        private double amount;
        
        @SerializedName("reason")
        private String reason;
        
        @SerializedName("processedAt")
        private String processedAt;
        
        @SerializedName("refundTransactionId")
        private String refundTransactionId;
        
        @SerializedName("refundStatus")
        private String status;

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getProcessedAt() { return processedAt; }
        public void setProcessedAt(String processedAt) { this.processedAt = processedAt; }
        
        public String getRefundTransactionId() { return refundTransactionId; }
        public void setRefundTransactionId(String refundTransactionId) { this.refundTransactionId = refundTransactionId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class Fees {
        @SerializedName("platformFee")
        private double platformFee;
        
        @SerializedName("processingFee")
        private double processingFee;
        
        @SerializedName("totalFees")
        private double totalFees;

        public double getPlatformFee() { return platformFee; }
        public void setPlatformFee(double platformFee) { this.platformFee = platformFee; }
        
        public double getProcessingFee() { return processingFee; }
        public void setProcessingFee(double processingFee) { this.processingFee = processingFee; }
        
        public double getTotalFees() { return totalFees; }
        public void setTotalFees(double totalFees) { this.totalFees = totalFees; }
    }

    public static class Metadata {
        @SerializedName("userAgent")
        private String userAgent;
        
        @SerializedName("ipAddress")
        private String ipAddress;
        
        @SerializedName("deviceInfo")
        private String deviceInfo;

        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        
        public String getDeviceInfo() { return deviceInfo; }
        public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    }
}
