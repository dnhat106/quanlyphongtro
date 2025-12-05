const mongoose = require('mongoose');

const roomSchema = new mongoose.Schema({
  title: {
    type: String,
    required: [true, 'Tiêu đề là bắt buộc'],
    trim: true,
    maxlength: [200, 'Tiêu đề không được vượt quá 200 ký tự']
  },
  description: {
    type: String,
    required: [true, 'Mô tả là bắt buộc'],
    maxlength: [2000, 'Mô tả không được vượt quá 2000 ký tự']
  },
  landlord: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  address: {
    street: {
      type: String,
      required: [true, 'Địa chỉ đường là bắt buộc']
    },
    ward: {
      type: String,
      required: [true, 'Phường/xã là bắt buộc']
    },
    district: {
      type: String,
      required: [true, 'Quận/huyện là bắt buộc']
    },
    city: {
      type: String,
      required: [true, 'Thành phố là bắt buộc']
    },
    coordinates: {
      lat: {
        type: Number,
        default: 0
      },
      lng: {
        type: Number,
        default: 0
      }
    }
  },
  roomType: {
    type: String,
    enum: ['studio', '1bedroom', '2bedroom', '3bedroom', 'shared'],
    required: [true, 'Loại phòng là bắt buộc']
  },
  area: {
    type: Number,
    required: [true, 'Diện tích là bắt buộc'],
    min: [1, 'Diện tích phải lớn hơn 0']
  },
  price: {
    monthly: {
      type: Number,
      required: [true, 'Giá thuê hàng tháng là bắt buộc'],
      min: [0, 'Giá thuê không được âm']
    },
    deposit: {
      type: Number,
      required: [true, 'Tiền cọc là bắt buộc'],
      min: [0, 'Tiền cọc không được âm']
    },
    utilities: {
      type: Number,
      default: 0,
      min: [0, 'Phí tiện ích không được âm']
    }
  },
  images: [{
    url: {
      type: String,
      required: true
    },
    caption: String,
    isPrimary: {
      type: Boolean,
      default: false
    }
  }],
  amenities: [{
    type: String,
    enum: [
      'wifi', 'air_conditioner', 'refrigerator', 'washing_machine',
      'television', 'bed', 'wardrobe', 'desk', 'chair', 'fan',
      'hot_water', 'kitchen', 'bathroom', 'balcony', 'parking',
      'elevator', 'security', 'gym', 'swimming_pool', 'garden'
    ]
  }],
  rules: [{
    type: String,
    maxlength: [200, 'Quy định không được vượt quá 200 ký tự']
  }],
  availability: {
    isAvailable: {
      type: Boolean,
      default: true
    },
    availableFrom: Date,
    minimumStay: {
      type: Number,
      default: 1,
      min: [1, 'Thời gian thuê tối thiểu phải lớn hơn 0']
    }
  },
  status: {
    type: String,
    enum: ['active', 'inactive', 'rented', 'maintenance'],
    default: 'active'
  },
  views: {
    type: Number,
    default: 0
  },
  likes: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User'
  }],
  rating: {
    average: {
      type: Number,
      default: 0,
      min: 0,
      max: 5
    },
    count: {
      type: Number,
      default: 0
    }
  },
  contactInfo: {
    phone: String,
    email: String,
    preferredContact: {
      type: String,
      enum: ['phone', 'email', 'both'],
      default: 'both'
    }
  },
  nearbyPlaces: [{
    name: String,
    type: {
      type: String,
      enum: ['school', 'hospital', 'market', 'bus_station', 'restaurant', 'bank', 'other']
    },
    distance: Number,
    description: String
  }]
}, {
  timestamps: true
});

roomSchema.index({ landlord: 1 });
roomSchema.index({ status: 1 });
roomSchema.index({ 'address.city': 1, 'address.district': 1 });
roomSchema.index({ 'price.monthly': 1 });
roomSchema.index({ roomType: 1 });
roomSchema.index({ 'availability.isAvailable': 1 });
roomSchema.index({ 'rating.average': -1 });
roomSchema.index({ createdAt: -1 });

roomSchema.index({ 'address.coordinates': '2dsphere' });

roomSchema.virtual('fullAddress').get(function() {
  return `${this.address.street}, ${this.address.ward}, ${this.address.district}, ${this.address.city}`;
});

roomSchema.methods.incrementViews = function() {
  this.views += 1;
  return this.save();
};

roomSchema.methods.toggleLike = function(userId) {
  const index = this.likes.indexOf(userId);
  if (index > -1) {
    this.likes.splice(index, 1);
  } else {
    this.likes.push(userId);
  }
  return this.save();
};

roomSchema.methods.updateRating = function(newRating) {
  const totalRating = this.rating.average * this.rating.count + newRating;
  this.rating.count += 1;
  this.rating.average = totalRating / this.rating.count;
  return this.save();
};

module.exports = mongoose.model('Room', roomSchema);
