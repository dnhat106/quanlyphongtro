const mongoose = require('mongoose');

const paymentSchema = new mongoose.Schema({
  booking: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Booking',
    required: true
  },
  payer: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  recipient: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  type: {
    type: String,
    enum: ['deposit', 'monthly_rent', 'utilities', 'penalty', 'refund'],
    required: true
  },
  amount: {
    type: Number,
    required: [true, 'Số tiền là bắt buộc'],
    min: [0, 'Số tiền không được âm']
  },
  currency: {
    type: String,
    default: 'VND'
  },
  status: {
    type: String,
    enum: ['pending', 'processing', 'completed', 'failed', 'cancelled', 'refunded'],
    default: 'pending'
  },
  paymentMethod: {
    type: String,
    enum: ['vnpay', 'bank_transfer', 'cash', 'other', 'pending'],
    required: true
  },
  vnpay: {
    txnRef: String,
    orderInfo: String,
    orderType: String,
    amount: Number,
    locale: {
      type: String,
      default: 'vn'
    },
    currCode: {
      type: String,
      default: 'VND'
    },
    returnUrl: String,
    ipAddr: String,
    createDate: String,
    expireDate: String,
    vnpTxnRef: String,
    vnpAmount: Number,
    vnpOrderInfo: String,
    vnpResponseCode: String,
    vnpTransactionNo: String,
    vnpTransactionStatus: String,
    vnpTxnRef: String,
    vnpSecureHash: String,
    vnpSecureHashType: String,
    vnpBankCode: String,
    vnpCardType: String,
    vnpPayDate: String
  },
  bankTransfer: {
    bankName: String,
    accountNumber: String,
    accountHolder: String,
    transferNote: String,
    transferDate: Date,
    receiptImage: String
  },
  transactionId: {
    type: String,
    unique: true,
    sparse: true
  },
  externalTransactionId: String,
  description: String,
  notes: String,
  initiatedAt: {
    type: Date,
    default: Date.now
  },
  processedAt: Date,
  completedAt: Date,
  failedAt: Date,
  failureReason: String,
  refund: {
    amount: Number,
    reason: String,
    processedAt: Date,
    refundTransactionId: String,
    status: {
      type: String,
      enum: ['pending', 'processing', 'completed', 'failed'],
      default: 'pending'
    }
  },
  fees: {
    platformFee: {
      type: Number,
      default: 0
    },
    processingFee: {
      type: Number,
      default: 0
    },
    totalFees: {
      type: Number,
      default: 0
    }
  },
  metadata: {
    userAgent: String,
    ipAddress: String,
    deviceInfo: String
  }
}, {
  timestamps: true
});


paymentSchema.index({ booking: 1 });
paymentSchema.index({ payer: 1 });
paymentSchema.index({ recipient: 1 });
paymentSchema.index({ status: 1 });
paymentSchema.index({ type: 1 });
paymentSchema.index({ paymentMethod: 1 });
paymentSchema.index({ transactionId: 1 });
paymentSchema.index({ 'vnpay.vnpTxnRef': 1 });
paymentSchema.index({ createdAt: -1 });

paymentSchema.pre('save', async function(next) {
  if (this.isNew && !this.transactionId) {
    const timestamp = Date.now().toString();
    const random = Math.random().toString(36).substring(2, 8).toUpperCase();
    this.transactionId = `TXN${timestamp.slice(-8)}${random}`;
  }
  next();
});

paymentSchema.methods.calculateTotalWithFees = function() {
  return this.amount + this.fees.totalFees;
};

paymentSchema.methods.isSuccessful = function() {
  return this.status === 'completed';
};

paymentSchema.methods.canBeRefunded = function() {
  return this.status === 'completed' && 
         !this.refund.status || 
         this.refund.status === 'pending';
};

paymentSchema.methods.processRefund = function(amount, reason) {
  if (!this.canBeRefunded()) {
    throw new Error('Payment cannot be refunded');
  }
  
  this.refund = {
    amount: amount || this.amount,
    reason: reason,
    processedAt: new Date(),
    status: 'processing'
  };
  
  return this.save();
};

paymentSchema.statics.getPaymentStats = async function(filters = {}) {
  const pipeline = [
    { $match: filters },
    {
      $group: {
        _id: null,
        totalAmount: { $sum: '$amount' },
        totalCount: { $sum: 1 },
        successfulAmount: {
          $sum: {
            $cond: [{ $eq: ['$status', 'completed'] }, '$amount', 0]
          }
        },
        successfulCount: {
          $sum: {
            $cond: [{ $eq: ['$status', 'completed'] }, 1, 0]
          }
        },
        pendingAmount: {
          $sum: {
            $cond: [{ $eq: ['$status', 'pending'] }, '$amount', 0]
          }
        },
        pendingCount: {
          $sum: {
            $cond: [{ $eq: ['$status', 'pending'] }, 1, 0]
          }
        }
      }
    }
  ];
  
  const result = await this.aggregate(pipeline);
  return result[0] || {
    totalAmount: 0,
    totalCount: 0,
    successfulAmount: 0,
    successfulCount: 0,
    pendingAmount: 0,
    pendingCount: 0
  };
};

module.exports = mongoose.model('Payment', paymentSchema);
