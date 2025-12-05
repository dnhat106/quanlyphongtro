//model: class đại diện cho thông tin phòng trọ
// Mục đích file: File này dùng để định nghĩa cấu trúc dữ liệu phòng trọ
// function: 
// - Room(): Constructor mặc định
// - getId(): Lấy ID phòng
// - setId(): Thiết lập ID phòng
// - getTitle(): Lấy tiêu đề phòng
// - setTitle(): Thiết lập tiêu đề phòng
// - getDescription(): Lấy mô tả phòng
// - setDescription(): Thiết lập mô tả phòng
// - getAddress(): Lấy địa chỉ phòng
// - setAddress(): Thiết lập địa chỉ phòng
// - getRoomType(): Lấy loại phòng
// - setRoomType(): Thiết lập loại phòng
// - getArea(): Lấy diện tích phòng
// - setArea(): Thiết lập diện tích phòng
// - getPrice(): Lấy giá phòng
// - setPrice(): Thiết lập giá phòng
// - getAmenities(): Lấy danh sách tiện ích
// - setAmenities(): Thiết lập danh sách tiện ích
// - getRules(): Lấy danh sách quy định
// - setRules(): Thiết lập danh sách quy định
// - getImages(): Lấy danh sách hình ảnh
// - setImages(): Thiết lập danh sách hình ảnh
// - getLandlord(): Lấy thông tin chủ trọ
// - setLandlord(): Thiết lập thông tin chủ trọ
// - getStatus(): Lấy trạng thái phòng
// - setStatus(): Thiết lập trạng thái phòng
// - getAvailability(): Lấy thông tin khả dụng
// - setAvailability(): Thiết lập thông tin khả dụng
// - getContactInfo(): Lấy thông tin liên hệ
// - setContactInfo(): Thiết lập thông tin liên hệ
// - getCreatedAt(): Lấy thời gian tạo
// - setCreatedAt(): Thiết lập thời gian tạo
// - getUpdatedAt(): Lấy thời gian cập nhật
// - setUpdatedAt(): Thiết lập thời gian cập nhật
package com.example.appquanlytimtro.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Room implements Serializable {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("address")
    private User.Address address;
    
    @SerializedName("roomType")
    private String roomType;
    
    @SerializedName("area")
    private double area;
    
    @SerializedName("price")
    private Price price;
    
    @SerializedName("amenities")
    private List<String> amenities;
    
    @SerializedName("rules")
    private List<String> rules;
    
    @SerializedName("images")
    private List<RoomImage> images;
    
    @SerializedName("landlord")
    private User landlord;
    
    @SerializedName("availability")
    private Availability availability;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("contactInfo")
    private ContactInfo contactInfo;
    
    @SerializedName("nearbyPlaces")
    private List<NearbyPlace> nearbyPlaces;
    
    @SerializedName("rating")
    private Rating rating;
    
    @SerializedName("views")
    private int views;
    
    @SerializedName("likes")
    private List<String> likes;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("updatedAt")
    private String updatedAt;

    public Room() {}

    public Room(String title, String description, User.Address address, String roomType, 
                double area, Price price) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.roomType = roomType;
        this.area = area;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User.Address getAddress() {
        return address;
    }

    public void setAddress(User.Address address) {
        this.address = address;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public List<String> getRules() {
        return rules;
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }

    public List<RoomImage> getImages() {
        return images;
    }

    public void setImages(List<RoomImage> images) {
        this.images = images;
    }

    public User getLandlord() {
        return landlord;
    }

    public void setLandlord(User landlord) {
        this.landlord = landlord;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public List<NearbyPlace> getNearbyPlaces() {
        return nearbyPlaces;
    }

    public void setNearbyPlaces(List<NearbyPlace> nearbyPlaces) {
        this.nearbyPlaces = nearbyPlaces;
    }

    public Rating getRating() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
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

    public static class Price implements Serializable {
        @SerializedName("monthly")
        private double monthly;
        
        @SerializedName("deposit")
        private double deposit;
        
        @SerializedName("utilities")
        private Object utilities;

        public Price() {}

        public double getMonthly() {
            return monthly;
        }

        public void setMonthly(double monthly) {
            this.monthly = monthly;
        }

        public double getDeposit() {
            return deposit;
        }

        public void setDeposit(double deposit) {
            this.deposit = deposit;
        }

        public Utilities getUtilities() {
            if (utilities instanceof Utilities) {
                return (Utilities) utilities;
            } else if (utilities instanceof Number) {
                Utilities utils = new Utilities();
                double total = ((Number) utilities).doubleValue();
                utils.setElectricity(total * 0.4);
                utils.setWater(total * 0.3);
                utils.setInternet(total * 0.2);
                utils.setOther(total * 0.1);
                return utils;
            }
            return null;
        }

        public void setUtilities(Utilities utilities) {
            this.utilities = utilities;
        }
        
        public void setUtilities(Object utilities) {
            this.utilities = utilities;
        }
    }

    public static class Utilities implements Serializable {
        @SerializedName("electricity")
        private double electricity;
        
        @SerializedName("water")
        private double water;
        
        @SerializedName("internet")
        private double internet;
        
        @SerializedName("other")
        private double other;

        public Utilities() {}

        public double getElectricity() {
            return electricity;
        }

        public void setElectricity(double electricity) {
            this.electricity = electricity;
        }

        public double getWater() {
            return water;
        }

        public void setWater(double water) {
            this.water = water;
        }

        public double getInternet() {
            return internet;
        }

        public void setInternet(double internet) {
            this.internet = internet;
        }

        public double getOther() {
            return other;
        }

        public void setOther(double other) {
            this.other = other;
        }
    }

    public static class RoomImage implements Serializable {
        @SerializedName("_id")
        private String id;
        
        @SerializedName("url")
        private String url;
        
        @SerializedName("caption")
        private String caption;
        
        @SerializedName("isPrimary")
        private boolean isPrimary;

        public RoomImage() {}

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public boolean isPrimary() {
            return isPrimary;
        }

        public void setPrimary(boolean primary) {
            isPrimary = primary;
        }
    }

    public static class Availability implements Serializable {
        @SerializedName("isAvailable")
        private boolean isAvailable;
        
        @SerializedName("availableFrom")
        private String availableFrom;
        
        @SerializedName("availableTo")
        private String availableTo;

        public Availability() {}

        public boolean isAvailable() {
            return isAvailable;
        }

        public void setAvailable(boolean available) {
            isAvailable = available;
        }

        public String getAvailableFrom() {
            return availableFrom;
        }

        public void setAvailableFrom(String availableFrom) {
            this.availableFrom = availableFrom;
        }

        public String getAvailableTo() {
            return availableTo;
        }

        public void setAvailableTo(String availableTo) {
            this.availableTo = availableTo;
        }
    }

    public static class ContactInfo implements Serializable {
        @SerializedName("phone")
        private String phone;
        
        @SerializedName("email")
        private String email;
        
        @SerializedName("preferredContact")
        private String preferredContact;

        public ContactInfo() {}

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPreferredContact() {
            return preferredContact;
        }

        public void setPreferredContact(String preferredContact) {
            this.preferredContact = preferredContact;
        }
    }

    public static class NearbyPlace implements Serializable {
        @SerializedName("name")
        private String name;
        
        @SerializedName("type")
        private String type;
        
        @SerializedName("distance")
        private double distance;

        public NearbyPlace() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }
    }

    public static class Rating implements Serializable {
        @SerializedName("average")
        private double average;
        
        @SerializedName("count")
        private int count;

        public Rating() {}

        public double getAverage() {
            return average;
        }

        public void setAverage(double average) {
            this.average = average;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
