const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = new mongoose.Schema({
  fullName: {
    type: String,
    required: [true, 'Họ tên là bắt buộc'],
    trim: true,
    maxlength: [100, 'Họ tên không được vượt quá 100 ký tự']
  },
  email: {
    type: String,
    required: [true, 'Email là bắt buộc'],
    unique: true,
    lowercase: true,
    match: [/^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/, 'Email không hợp lệ']
  },
  password: {
    type: String,
    required: [true, 'Mật khẩu là bắt buộc'],
    minlength: [6, 'Mật khẩu phải có ít nhất 6 ký tự'],
    select: false
  },
  phone: {
    type: String,
    required: [true, 'Số điện thoại là bắt buộc'],
    match: [/^[0-9]{10,11}$/, 'Số điện thoại không hợp lệ']
  },
  role: {
    type: String,
    enum: ['admin', 'landlord', 'tenant'],
    default: 'tenant'
  },
  avatar: {
    type: String,
    default: null
  },
  address: {
    street: String,
    ward: String,
    district: String,
    city: String,
    coordinates: {
      lat: Number,
      lng: Number
    }
  },
  isActive: {
    type: Boolean,
    default: true
  },
  isVerified: {
    type: Boolean,
    default: false
  },
  verificationToken: String,
  resetPasswordToken: String,
  resetPasswordExpires: Date,
  landlordInfo: {
    businessLicense: String,
    taxCode: String,
    bankAccount: {
      bankName: String,
      accountNumber: String,
      accountHolder: String
    }
  },
  tenantInfo: {
    idCard: String,
    dateOfBirth: Date,
    occupation: String,
    emergencyContact: {
      name: String,
      phone: String,
      relationship: String
    }
  },
  lastLogin: Date,
  loginCount: {
    type: Number,
    default: 0
  }
}, {
  timestamps: true
});

userSchema.index({ email: 1 });
userSchema.index({ phone: 1 });
userSchema.index({ role: 1 });
userSchema.index({ isActive: 1 });

userSchema.pre('save', async function(next) {
  if (!this.isModified('password')) return next();
  
  this.password = await bcrypt.hash(this.password, 12);
  next();
});

userSchema.methods.comparePassword = async function(candidatePassword) {
  return await bcrypt.compare(candidatePassword, this.password);
};

userSchema.methods.getPublicProfile = function() {
  const userObject = this.toObject();
  delete userObject.password;
  delete userObject.verificationToken;
  delete userObject.resetPasswordToken;
  delete userObject.resetPasswordExpires;
  return userObject;
};

module.exports = mongoose.model('User', userSchema);
