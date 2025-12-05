const mongoose = require('mongoose');

const notificationSchema = new mongoose.Schema({
  recipient: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  sender: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User'
  },
  type: {
    type: String,
    enum: [
      'booking_request',     
      'booking_confirmed',   
      'booking_cancelled',  
      'payment_received',  
      'payment_due',         
      'payment_overdue',     
      'contract_signed',   
      'room_available',      
      'maintenance_request', 
      'review_request',      
      'system_announcement', 
      'support_ticket',     
      'other'               
    ],
    required: true
  },
  title: {
    type: String,
    required: [true, 'Tiêu đề là bắt buộc'],
    maxlength: [200, 'Tiêu đề không được vượt quá 200 ký tự']
  },
  message: {
    type: String,
    required: [true, 'Nội dung là bắt buộc'],
    maxlength: [1000, 'Nội dung không được vượt quá 1000 ký tự']
  },
  data: {
    bookingId: mongoose.Schema.Types.ObjectId,
    roomId: mongoose.Schema.Types.ObjectId,
    paymentId: mongoose.Schema.Types.ObjectId,
    invoiceId: mongoose.Schema.Types.ObjectId,
    amount: Number,
    dueDate: Date,
    url: String,
    action: String,
    metadata: mongoose.Schema.Types.Mixed
  },
  priority: {
    type: String,
    enum: ['low', 'medium', 'high', 'urgent'],
    default: 'medium'
  },
  status: {
    type: String,
    enum: ['unread', 'read', 'archived'],
    default: 'unread'
  },
  channels: [{
    type: String,
    enum: ['in_app', 'email', 'sms', 'push'],
    default: ['in_app']
  }],
  delivery: {
    inApp: {
      sent: {
        type: Boolean,
        default: true
      },
      sentAt: {
        type: Date,
        default: Date.now
      },
      readAt: Date
    },
    email: {
      sent: {
        type: Boolean,
        default: false
      },
      sentAt: Date,
      delivered: Boolean,
      opened: Boolean,
      clicked: Boolean,
      error: String
    },
    sms: {
      sent: {
        type: Boolean,
        default: false
      },
      sentAt: Date,
      delivered: Boolean,
      error: String
    },
    push: {
      sent: {
        type: Boolean,
        default: false
      },
      sentAt: Date,
      delivered: Boolean,
      clicked: Boolean,
      error: String
    }
  },
  scheduledFor: Date,
  expiresAt: Date,
  autoArchive: {
    enabled: {
      type: Boolean,
      default: true
    },
    archiveAfter: {
      type: Number,
      default: 30
    }
  },
  template: {
    name: String,
    variables: mongoose.Schema.Types.Mixed
  }
}, {
  timestamps: true
});

notificationSchema.index({ recipient: 1 });
notificationSchema.index({ sender: 1 });
notificationSchema.index({ type: 1 });
notificationSchema.index({ status: 1 });
notificationSchema.index({ priority: 1 });
notificationSchema.index({ createdAt: -1 });
notificationSchema.index({ scheduledFor: 1 });
notificationSchema.index({ expiresAt: 1 });

notificationSchema.index({ recipient: 1, status: 1, createdAt: -1 });
notificationSchema.index({ recipient: 1, type: 1, createdAt: -1 });

notificationSchema.index(
  { createdAt: 1 },
  { 
    expireAfterSeconds: 60 * 60 * 24 * 30, 
    partialFilterExpression: { 
      'autoArchive.enabled': true,
      status: { $in: ['read', 'archived'] }
    }
  }
);

notificationSchema.methods.markAsRead = function() {
  this.status = 'read';
  this.delivery.inApp.readAt = new Date();
  return this.save();
};

notificationSchema.methods.archive = function() {
  this.status = 'archived';
  return this.save();
};

notificationSchema.methods.isExpired = function() {
  return this.expiresAt && new Date() > this.expiresAt;
};

notificationSchema.methods.shouldBeSent = function() {
  return !this.isExpired() && 
         (!this.scheduledFor || new Date() >= this.scheduledFor);
};

notificationSchema.statics.getUnreadCount = async function(userId) {
  return await this.countDocuments({
    recipient: userId,
    status: 'unread'
  });
};

notificationSchema.statics.markAllAsRead = async function(userId) {
  return await this.updateMany(
    { recipient: userId, status: 'unread' },
    { 
      status: 'read',
      'delivery.inApp.readAt': new Date()
    }
  );
};

notificationSchema.statics.getUserNotifications = async function(userId, options = {}) {
  const {
    page = 1,
    limit = 20,
    status,
    type,
    priority
  } = options;
  
  const query = { recipient: userId };
  
  if (status) query.status = status;
  if (type) query.type = type;
  if (priority) query.priority = priority;
  
  const skip = (page - 1) * limit;
  
  return await this.find(query)
    .populate('sender', 'fullName avatar')
    .sort({ createdAt: -1 })
    .skip(skip)
    .limit(limit);
};

notificationSchema.statics.sendBulk = async function(notifications) {
  return await this.insertMany(notifications);
};

notificationSchema.statics.cleanupExpired = async function() {
  return await this.updateMany(
    { 
      expiresAt: { $lt: new Date() },
      status: { $ne: 'archived' }
    },
    { status: 'archived' }
  );
};

module.exports = mongoose.model('Notification', notificationSchema);
