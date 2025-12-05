//model: class đại diện cho thông tin đặt phòng
// Mục đích file: File này dùng để định nghĩa cấu trúc dữ liệu đặt phòng
// function: 
// - Booking(): Constructor mặc định
// - getId(): Lấy ID đặt phòng
// - setId(): Thiết lập ID đặt phòng
// - getRoom(): Lấy thông tin phòng
// - setRoom(): Thiết lập thông tin phòng
// - getTenant(): Lấy thông tin người thuê
// - setTenant(): Thiết lập thông tin người thuê
// - getLandlord(): Lấy thông tin chủ trọ
// - setLandlord(): Thiết lập thông tin chủ trọ
// - getBookingDetails(): Lấy chi tiết đặt phòng
// - setBookingDetails(): Thiết lập chi tiết đặt phòng
// - getPricing(): Lấy thông tin giá cả
// - setPricing(): Thiết lập thông tin giá cả
// - getStatus(): Lấy trạng thái đặt phòng
// - setStatus(): Thiết lập trạng thái đặt phòng
// - getNotes(): Lấy ghi chú
// - setNotes(): Thiết lập ghi chú
// - getCreatedAt(): Lấy thời gian tạo
// - setCreatedAt(): Thiết lập thời gian tạo
// - getUpdatedAt(): Lấy thời gian cập nhật
// - setUpdatedAt(): Thiết lập thời gian cập nhật
package com.example.appquanlytimtro.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Booking {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("room")
    private Room room;
    
    @SerializedName("tenant")
    private User tenant;
    
    @SerializedName("landlord")
    private User landlord;
    
    @SerializedName("bookingDetails")
    private BookingDetails bookingDetails;
    
    @SerializedName("pricing")
    private Pricing pricing;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("paymentStatus")
    private PaymentStatus paymentStatus;
    
    @SerializedName("contract")
    private Contract contract;
    
    @SerializedName("documents")
    private List<Document> documents;
    
    @SerializedName("notes")
    private Notes notes;
    
    @SerializedName("cancellation")
    private Cancellation cancellation;
    
    @SerializedName("reviews")
    private Reviews reviews;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("updatedAt")
    private String updatedAt;

    public Booking() {}

    public Booking(String roomId, String tenantId, String checkIn, String checkOut, 
                   int duration, double totalAmount) {
        this.bookingDetails = new BookingDetails();
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            this.bookingDetails.setCheckInDate(sdf.parse(checkIn));
            this.bookingDetails.setCheckOutDate(sdf.parse(checkOut));
        } catch (java.text.ParseException e) {
            this.bookingDetails.setCheckInDate(new java.util.Date());
            this.bookingDetails.setCheckOutDate(new java.util.Date());
        }
        this.bookingDetails.setDuration(duration); 
        this.pricing = new Pricing();
        this.pricing.setTotalAmount(totalAmount);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public User getTenant() {
        return tenant;
    }

    public void setTenant(User tenant) {
        this.tenant = tenant;
    }

    public User getLandlord() {
        return landlord;
    }

    public void setLandlord(User landlord) {
        this.landlord = landlord;
    }

    public BookingDetails getBookingDetails() {
        return bookingDetails;
    }

    public void setBookingDetails(BookingDetails bookingDetails) {
        this.bookingDetails = bookingDetails;
    }

    public Pricing getPricing() {
        return pricing;
    }

    public void setPricing(Pricing pricing) {
        this.pricing = pricing;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public Notes getNotes() {
        return notes;
    }

    public void setNotes(Notes notes) {
        this.notes = notes;
    }

    public Cancellation getCancellation() {
        return cancellation;
    }

    public void setCancellation(Cancellation cancellation) {
        this.cancellation = cancellation;
    }

    public Reviews getReviews() {
        return reviews;
    }

    public void setReviews(Reviews reviews) {
        this.reviews = reviews;
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

    public static class BookingDetails {
        @SerializedName("checkInDate")
        private java.util.Date checkInDate;
        
        @SerializedName("checkOutDate")
        private java.util.Date checkOutDate;
        
        @SerializedName("duration")
        private int duration;
        
        @SerializedName("numberOfOccupants")
        private int numberOfOccupants;

        public java.util.Date getCheckInDate() {
            return checkInDate;
        }

        public void setCheckInDate(java.util.Date checkInDate) {
            this.checkInDate = checkInDate;
        }

        public java.util.Date getCheckOutDate() {
            return checkOutDate;
        }

        public void setCheckOutDate(java.util.Date checkOutDate) {
            this.checkOutDate = checkOutDate;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getNumberOfOccupants() {
            return numberOfOccupants;
        }

        public void setNumberOfOccupants(int numberOfOccupants) {
            this.numberOfOccupants = numberOfOccupants;
        }
    }

    public static class Pricing {
        @SerializedName("monthlyRent")
        private double monthlyRent;
        
        @SerializedName("deposit")
        private double deposit;
        
        @SerializedName("utilities")
        private double utilities;
        
        @SerializedName("totalAmount")
        private double totalAmount;

        public double getMonthlyRent() {
            return monthlyRent;
        }

        public void setMonthlyRent(double monthlyRent) {
            this.monthlyRent = monthlyRent;
        }

        public double getDeposit() {
            return deposit;
        }

        public void setDeposit(double deposit) {
            this.deposit = deposit;
        }

        public double getUtilities() {
            return utilities;
        }

        public void setUtilities(double utilities) {
            this.utilities = utilities;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
        }
    }

    public static class PaymentStatus {
        @SerializedName("deposit")
        private DepositPayment deposit;
        
        @SerializedName("monthly")
        private List<MonthlyPayment> monthly;

        public DepositPayment getDeposit() {
            return deposit;
        }

        public void setDeposit(DepositPayment deposit) {
            this.deposit = deposit;
        }

        public List<MonthlyPayment> getMonthly() {
            return monthly;
        }

        public void setMonthly(List<MonthlyPayment> monthly) {
            this.monthly = monthly;
        }
    }

    public static class DepositPayment {
        @SerializedName("status")
        private String status;
        
        @SerializedName("amount")
        private double amount;
        
        @SerializedName("paidAt")
        private String paidAt;
        
        @SerializedName("paymentMethod")
        private String paymentMethod;
        
        @SerializedName("transactionId")
        private String transactionId;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getPaidAt() {
            return paidAt;
        }

        public void setPaidAt(String paidAt) {
            this.paidAt = paidAt;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }
    }

    public static class MonthlyPayment {
        @SerializedName("month")
        private String month;
        
        @SerializedName("amount")
        private double amount;
        
        @SerializedName("status")
        private String status;
        
        @SerializedName("dueDate")
        private String dueDate;
        
        @SerializedName("paidAt")
        private String paidAt;
        
        @SerializedName("paymentMethod")
        private String paymentMethod;
        
        @SerializedName("transactionId")
        private String transactionId;

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDueDate() {
            return dueDate;
        }

        public void setDueDate(String dueDate) {
            this.dueDate = dueDate;
        }

        public String getPaidAt() {
            return paidAt;
        }

        public void setPaidAt(String paidAt) {
            this.paidAt = paidAt;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }
    }

    public static class Contract {
        @SerializedName("contractNumber")
        private String contractNumber;
        
        @SerializedName("signedAt")
        private String signedAt;
        
        @SerializedName("contractFile")
        private String contractFile;
        
        @SerializedName("terms")
        private List<ContractTerm> terms;

        public String getContractNumber() {
            return contractNumber;
        }

        public void setContractNumber(String contractNumber) {
            this.contractNumber = contractNumber;
        }

        public String getSignedAt() {
            return signedAt;
        }

        public void setSignedAt(String signedAt) {
            this.signedAt = signedAt;
        }

        public String getContractFile() {
            return contractFile;
        }

        public void setContractFile(String contractFile) {
            this.contractFile = contractFile;
        }

        public List<ContractTerm> getTerms() {
            return terms;
        }

        public void setTerms(List<ContractTerm> terms) {
            this.terms = terms;
        }
    }

    public static class ContractTerm {
        @SerializedName("clause")
        private String clause;
        
        @SerializedName("description")
        private String description;

        public String getClause() {
            return clause;
        }

        public void setClause(String clause) {
            this.clause = clause;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class Document {
        @SerializedName("type")
        private String type;
        
        @SerializedName("fileName")
        private String fileName;
        
        @SerializedName("fileUrl")
        private String fileUrl;
        
        @SerializedName("uploadedAt")
        private String uploadedAt;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public String getUploadedAt() {
            return uploadedAt;
        }

        public void setUploadedAt(String uploadedAt) {
            this.uploadedAt = uploadedAt;
        }
    }

    public static class Notes {
        @SerializedName("tenant")
        private String tenant;
        
        @SerializedName("landlord")
        private String landlord;
        
        @SerializedName("admin")
        private String admin;

        public String getTenant() {
            return tenant;
        }

        public void setTenant(String tenant) {
            this.tenant = tenant;
        }

        public String getLandlord() {
            return landlord;
        }

        public void setLandlord(String landlord) {
            this.landlord = landlord;
        }

        public String getAdmin() {
            return admin;
        }

        public void setAdmin(String admin) {
            this.admin = admin;
        }
    }

    public static class Cancellation {
        @SerializedName("cancelledBy")
        private String cancelledBy;
        
        @SerializedName("cancelledAt")
        private String cancelledAt;
        
        @SerializedName("reason")
        private String reason;
        
        @SerializedName("refundAmount")
        private double refundAmount;
        
        @SerializedName("refundStatus")
        private String refundStatus;

        public String getCancelledBy() {
            return cancelledBy;
        }

        public void setCancelledBy(String cancelledBy) {
            this.cancelledBy = cancelledBy;
        }

        public String getCancelledAt() {
            return cancelledAt;
        }

        public void setCancelledAt(String cancelledAt) {
            this.cancelledAt = cancelledAt;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public double getRefundAmount() {
            return refundAmount;
        }

        public void setRefundAmount(double refundAmount) {
            this.refundAmount = refundAmount;
        }

        public String getRefundStatus() {
            return refundStatus;
        }

        public void setRefundStatus(String refundStatus) {
            this.refundStatus = refundStatus;
        }
    }

    public static class Reviews {
        @SerializedName("tenantReview")
        private Review tenantReview;
        
        @SerializedName("landlordReview")
        private Review landlordReview;

        public Review getTenantReview() {
            return tenantReview;
        }

        public void setTenantReview(Review tenantReview) {
            this.tenantReview = tenantReview;
        }

        public Review getLandlordReview() {
            return landlordReview;
        }

        public void setLandlordReview(Review landlordReview) {
            this.landlordReview = landlordReview;
        }
    }

    public static class Review {
        @SerializedName("rating")
        private double rating;
        
        @SerializedName("comment")
        private String comment;
        
        @SerializedName("createdAt")
        private String createdAt;

        public double getRating() {
            return rating;
        }

        public void setRating(double rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}
