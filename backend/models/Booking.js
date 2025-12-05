const mongoose = require('mongoose');

const bookingSchema = new mongoose.Schema({
  room: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Room',
    required: true
  },
  tenant: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  landlord: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  bookingDetails: {
    checkInDate: {
      type: Date,
      required: [true, 'Ngày nhận phòng là bắt buộc']
    },
    checkOutDate: {
      type: Date,
      required: [true, 'Ngày trả phòng là bắt buộc']
    },
    duration: {
      type: Number, 
      required: true,
      min: [1, 'Thời gian thuê phải ít nhất 1 tháng']
    },
    numberOfOccupants: {
      type: Number,
      required: true,
      min: [1, 'Số người ở phải ít nhất 1']
    }
  },
  pricing: {
    monthlyRent: {
      type: Number,
      required: true
    },
    deposit: {
      type: Number,
      required: true
    },
    utilities: {
      type: Number,
      default: 0
    },
    totalAmount: {
      type: Number,
      required: true
    }
  },
  status: {
    type: String,
    enum: [
      'pending',      
      'confirmed',    
      'deposit_paid',
      'active',       
      'completed',    
      'cancelled',  
      'expired'     
    ],
    default: 'pending'
  },
  paymentStatus: {
    deposit: {
      status: {
        type: String,
        enum: ['pending', 'paid', 'refunded'],
        default: 'pending'
      },
      amount: Number,
      paidAt: Date,
      paymentMethod: String,
      transactionId: String
    },
    monthly: [{
      month: String, 
      amount: Number,
      status: {
        type: String,
        enum: ['pending', 'paid', 'overdue'],
        default: 'pending'
      },
      dueDate: Date,
      paidAt: Date,
      paymentMethod: String,
      transactionId: String
    }]
  },
  contract: {
    contractNumber: {
      type: String,
      unique: true,
      sparse: true
    },
    signedAt: Date,
    contractFile: String,
    terms: [{
      clause: String,
      description: String
    }]
  },
  documents: [{
    type: {
      type: String,
      enum: ['id_card', 'employment_letter', 'bank_statement', 'other'],
      required: true
    },
    fileName: String,
    fileUrl: String,
    uploadedAt: {
      type: Date,
      default: Date.now
    }
  }],
  notes: {
    tenant: String,
    landlord: String,
    admin: String
  },
  cancellation: {
    cancelledBy: {
      type: String,
      enum: ['tenant', 'landlord', 'admin']
    },
    cancelledAt: Date,
    reason: String,
    refundAmount: Number,
    refundStatus: {
      type: String,
      enum: ['pending', 'processed', 'completed'],
      default: 'pending'
    }
  },
  reviews: {
    tenantReview: {
      rating: {
        type: Number,
        min: 1,
        max: 5
      },
      comment: String,
      createdAt: Date
    },
    landlordReview: {
      rating: {
        type: Number,
        min: 1,
        max: 5
      },
      comment: String,
      createdAt: Date
    }
  }
}, {
  timestamps: true
});

bookingSchema.index({ room: 1 });
bookingSchema.index({ tenant: 1 });
bookingSchema.index({ landlord: 1 });
bookingSchema.index({ status: 1 });
bookingSchema.index({ 'bookingDetails.checkInDate': 1 });
bookingSchema.index({ 'bookingDetails.checkOutDate': 1 });
bookingSchema.index({ createdAt: -1 });

bookingSchema.virtual('totalDays').get(function() {
  const diffTime = Math.abs(this.bookingDetails.checkOutDate - this.bookingDetails.checkInDate);
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
});

bookingSchema.pre('save', async function(next) {
  if (this.isNew && !this.contract.contractNumber) {
    const count = await this.constructor.countDocuments();
    this.contract.contractNumber = `HD${String(count + 1).padStart(6, '0')}`;
  }
  next();
});

bookingSchema.methods.calculateTotalAmount = function() {
  const monthlyTotal = this.pricing.monthlyRent * this.bookingDetails.duration;
  const utilitiesTotal = this.pricing.utilities * this.bookingDetails.duration;
  this.pricing.totalAmount = monthlyTotal + utilitiesTotal + this.pricing.deposit;
  return this.pricing.totalAmount;
};

bookingSchema.methods.isActive = function() {
  const now = new Date();
  return this.status === 'active' && 
         this.bookingDetails.checkInDate <= now && 
         this.bookingDetails.checkOutDate >= now;
};

bookingSchema.methods.canBeCancelled = function() {
  return ['pending', 'confirmed', 'deposit_paid'].includes(this.status);
};

bookingSchema.methods.generateMonthlyPayments = function() {
  const payments = [];
  const startDate = new Date(this.bookingDetails.checkInDate);
  
  for (let i = 0; i < this.bookingDetails.duration; i++) {
    const dueDate = new Date(startDate);
    dueDate.setMonth(dueDate.getMonth() + i);
    
    payments.push({
      month: `${dueDate.getFullYear()}-${String(dueDate.getMonth() + 1).padStart(2, '0')}`,
      amount: this.pricing.monthlyRent + this.pricing.utilities,
      status: 'pending',
      dueDate: dueDate
    });
  }
  
  this.paymentStatus.monthly = payments;
  return payments;
};

module.exports = mongoose.model('Booking', bookingSchema);
