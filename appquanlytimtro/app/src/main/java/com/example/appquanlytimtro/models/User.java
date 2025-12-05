//model: class đại diện cho thông tin người dùng
// Mục đích file: File này dùng để định nghĩa cấu trúc dữ liệu người dùng
// function: 
// - User(): Constructor mặc định
// - getId(): Lấy ID người dùng
// - setId(): Thiết lập ID người dùng
// - getFullName(): Lấy họ tên
// - setFullName(): Thiết lập họ tên
// - getEmail(): Lấy email
// - setEmail(): Thiết lập email
// - getPhone(): Lấy số điện thoại
// - setPhone(): Thiết lập số điện thoại
// - getRole(): Lấy vai trò
// - setRole(): Thiết lập vai trò
// - getAddress(): Lấy địa chỉ
// - setAddress(): Thiết lập địa chỉ
// - getProfileImage(): Lấy ảnh đại diện
// - setProfileImage(): Thiết lập ảnh đại diện
// - isVerified(): Kiểm tra đã xác thực chưa
// - setVerified(): Thiết lập trạng thái xác thực
// - getCreatedAt(): Lấy thời gian tạo
// - setCreatedAt(): Thiết lập thời gian tạo
// - getUpdatedAt(): Lấy thời gian cập nhật
// - setUpdatedAt(): Thiết lập thời gian cập nhật
package com.example.appquanlytimtro.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class User implements Serializable {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("role")
    private String role;
    
    @SerializedName("avatar")
    private String avatar;
    
    @SerializedName("address")
    private Address address;
    
    @SerializedName("isActive")
    private boolean isActive;
    
    @SerializedName("isVerified")
    private boolean isVerified;
    
    @SerializedName("landlordInfo")
    private LandlordInfo landlordInfo;
    
    @SerializedName("tenantInfo")
    private TenantInfo tenantInfo;
    
    @SerializedName("lastLogin")
    private String lastLogin;
    
    @SerializedName("loginCount")
    private int loginCount;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("updatedAt")
    private String updatedAt;

    public User() {}

    public User(String fullName, String email, String phone, String role) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public LandlordInfo getLandlordInfo() {
        return landlordInfo;
    }

    public void setLandlordInfo(LandlordInfo landlordInfo) {
        this.landlordInfo = landlordInfo;
    }

    public TenantInfo getTenantInfo() {
        return tenantInfo;
    }

    public void setTenantInfo(TenantInfo tenantInfo) {
        this.tenantInfo = tenantInfo;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
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

    public static class Address implements Serializable {
        @SerializedName("street")
        private String street;
        
        @SerializedName("ward")
        private String ward;
        
        @SerializedName("district")
        private String district;
        
        @SerializedName("city")
        private String city;
        
        @SerializedName("coordinates")
        private Coordinates coordinates;

        public Address() {}

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getWard() {
            return ward;
        }

        public void setWard(String ward) {
            this.ward = ward;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public Coordinates getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(Coordinates coordinates) {
            this.coordinates = coordinates;
        }
    }

    public static class Coordinates implements Serializable {
        @SerializedName("lat")
        private double lat;
        
        @SerializedName("lng")
        private double lng;

        public Coordinates() {}

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }
    }

    public static class LandlordInfo implements Serializable {
        @SerializedName("businessLicense")
        private String businessLicense;
        
        @SerializedName("taxCode")
        private String taxCode;
        
        @SerializedName("bankAccount")
        private BankAccount bankAccount;

        public LandlordInfo() {}

        public String getBusinessLicense() {
            return businessLicense;
        }

        public void setBusinessLicense(String businessLicense) {
            this.businessLicense = businessLicense;
        }

        public String getTaxCode() {
            return taxCode;
        }

        public void setTaxCode(String taxCode) {
            this.taxCode = taxCode;
        }

        public BankAccount getBankAccount() {
            return bankAccount;
        }

        public void setBankAccount(BankAccount bankAccount) {
            this.bankAccount = bankAccount;
        }
    }

    public static class BankAccount implements Serializable {
        @SerializedName("bankName")
        private String bankName;
        
        @SerializedName("accountNumber")
        private String accountNumber;
        
        @SerializedName("accountHolder")
        private String accountHolder;

        public BankAccount() {}

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        public String getAccountHolder() {
            return accountHolder;
        }

        public void setAccountHolder(String accountHolder) {
            this.accountHolder = accountHolder;
        }
    }

    public static class TenantInfo implements Serializable {
        @SerializedName("idCard")
        private String idCard;
        
        @SerializedName("dateOfBirth")
        private String dateOfBirth;
        
        @SerializedName("occupation")
        private String occupation;
        
        @SerializedName("emergencyContact")
        private EmergencyContact emergencyContact;

        public TenantInfo() {}

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getOccupation() {
            return occupation;
        }

        public void setOccupation(String occupation) {
            this.occupation = occupation;
        }

        public EmergencyContact getEmergencyContact() {
            return emergencyContact;
        }

        public void setEmergencyContact(EmergencyContact emergencyContact) {
            this.emergencyContact = emergencyContact;
        }
    }

    public static class EmergencyContact implements Serializable {
        @SerializedName("name")
        private String name;
        
        @SerializedName("phone")
        private String phone;
        
        @SerializedName("relationship")
        private String relationship;

        public EmergencyContact() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getRelationship() {
            return relationship;
        }

        public void setRelationship(String relationship) {
            this.relationship = relationship;
        }
    }
}
