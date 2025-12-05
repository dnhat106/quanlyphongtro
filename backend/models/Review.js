const mongoose = require('mongoose');

const reviewSchema = new mongoose.Schema({
  booking: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Booking',
    required: true
  },
  room: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Room',
    required: true
  },
  reviewer: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  reviewee: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  reviewType: {
    type: String,
    enum: ['tenant_to_landlord', 'landlord_to_tenant', 'tenant_to_room', 'landlord_to_room'],
    required: true
  },
  rating: {
    overall: {
      type: Number,
      required: true,
      min: 1,
      max: 5
    },
    cleanliness: {
      type: Number,
      min: 1,
      max: 5
    },
    location: {
      type: Number,
      min: 1,
      max: 5
    },
    amenities: {
      type: Number,
      min: 1,
      max: 5
    },
    value: {
      type: Number,
      min: 1,
      max: 5
    },
    communication: {
      type: Number,
      min: 1,
      max: 5
    },
    reliability: {
      type: Number,
      min: 1,
      max: 5
    },
    friendliness: {
      type: Number,
      min: 1,
      max: 5
    }
  },
  title: {
    type: String,
    maxlength: [100, 'Tiêu đề không được vượt quá 100 ký tự']
  },
  comment: {
    type: String,
    required: [true, 'Bình luận là bắt buộc'],
    maxlength: [1000, 'Bình luận không được vượt quá 1000 ký tự']
  },
  images: [{
    url: String,
    caption: String
  }],
  status: {
    type: String,
    enum: ['pending', 'approved', 'rejected', 'hidden'],
    default: 'pending'
  },
  isAnonymous: {
    type: Boolean,
    default: false
  },
  helpful: [{
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User'
    },
    isHelpful: Boolean,
    votedAt: {
      type: Date,
      default: Date.now
    }
  }],
  response: {
    comment: String,
    respondedAt: Date,
    respondedBy: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User'
    }
  },
  moderatedBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User'
  },
  moderatedAt: Date,
  moderationNotes: String,
  reports: [{
    reportedBy: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User'
    },
    reason: {
      type: String,
      enum: [
        'inappropriate_content',
        'spam',
        'fake_review',
        'harassment',
        'other'
      ]
    },
    description: String,
    reportedAt: {
      type: Date,
      default: Date.now
    },
    status: {
      type: String,
      enum: ['pending', 'investigating', 'resolved', 'dismissed'],
      default: 'pending'
    }
  }]
}, {
  timestamps: true
});

reviewSchema.index({ booking: 1 });
reviewSchema.index({ room: 1 });
reviewSchema.index({ reviewer: 1 });
reviewSchema.index({ reviewee: 1 });
reviewSchema.index({ reviewType: 1 });
reviewSchema.index({ status: 1 });
reviewSchema.index({ 'rating.overall': 1 });
reviewSchema.index({ createdAt: -1 });

reviewSchema.index({ room: 1, status: 1 });
reviewSchema.index({ reviewee: 1, reviewType: 1, status: 1 });

reviewSchema.index({ booking: 1, reviewer: 1, reviewType: 1 }, { unique: true });

reviewSchema.methods.calculateAverageRating = function() {
  const ratings = Object.values(this.rating).filter(r => typeof r === 'number');
  if (ratings.length === 0) return 0;
  
  const sum = ratings.reduce((acc, rating) => acc + rating, 0);
  return Math.round((sum / ratings.length) * 10) / 10; 
};

reviewSchema.methods.addHelpfulVote = function(userId, isHelpful) {
  this.helpful = this.helpful.filter(vote => !vote.user.equals(userId));
  
  this.helpful.push({
    user: userId,
    isHelpful: isHelpful
  });
  
  return this.save();
};

reviewSchema.methods.getHelpfulScore = function() {
  const helpfulVotes = this.helpful.filter(vote => vote.isHelpful).length;
  const totalVotes = this.helpful.length;
  
  return {
    helpful: helpfulVotes,
    total: totalVotes,
    percentage: totalVotes > 0 ? Math.round((helpfulVotes / totalVotes) * 100) : 0
  };
};

reviewSchema.methods.addResponse = function(comment, respondedBy) {
  this.response = {
    comment: comment,
    respondedAt: new Date(),
    respondedBy: respondedBy
  };
  
  return this.save();
};

reviewSchema.methods.reportReview = function(reportedBy, reason, description) {
  this.reports.push({
    reportedBy: reportedBy,
    reason: reason,
    description: description
  });
  
  return this.save();
};

reviewSchema.statics.getRoomReviews = async function(roomId, options = {}) {
  const {
    page = 1,
    limit = 10,
    status = 'approved',
    sortBy = 'createdAt',
    sortOrder = 'desc'
  } = options;
  
  const query = { 
    room: roomId,
    reviewType: { $in: ['tenant_to_room', 'landlord_to_room'] },
    status: status
  };
  
  const skip = (page - 1) * limit;
  const sort = { [sortBy]: sortOrder === 'desc' ? -1 : 1 };
  
  return await this.find(query)
    .populate('reviewer', 'fullName avatar')
    .populate('response.respondedBy', 'fullName avatar')
    .sort(sort)
    .skip(skip)
    .limit(limit);
};

reviewSchema.statics.getUserReviews = async function(userId, reviewType, options = {}) {
  const {
    page = 1,
    limit = 10,
    status = 'approved'
  } = options;
  
  const query = { 
    reviewee: userId,
    reviewType: reviewType,
    status: status
  };
  
  const skip = (page - 1) * limit;
  
  return await this.find(query)
    .populate('reviewer', 'fullName avatar')
    .populate('room', 'title images')
    .populate('response.respondedBy', 'fullName avatar')
    .sort({ createdAt: -1 })
    .skip(skip)
    .limit(limit);
};

reviewSchema.statics.getRoomAverageRating = async function(roomId) {
  const result = await this.aggregate([
    {
      $match: {
        room: mongoose.Types.ObjectId(roomId),
        reviewType: { $in: ['tenant_to_room', 'landlord_to_room'] },
        status: 'approved'
      }
    },
    {
      $group: {
        _id: null,
        averageRating: { $avg: '$rating.overall' },
        totalReviews: { $sum: 1 },
        cleanliness: { $avg: '$rating.cleanliness' },
        location: { $avg: '$rating.location' },
        amenities: { $avg: '$rating.amenities' },
        value: { $avg: '$rating.value' }
      }
    }
  ]);
  
  return result[0] || {
    averageRating: 0,
    totalReviews: 0,
    cleanliness: 0,
    location: 0,
    amenities: 0,
    value: 0
  };
};

reviewSchema.statics.getUserAverageRating = async function(userId, reviewType) {
  const result = await this.aggregate([
    {
      $match: {
        reviewee: mongoose.Types.ObjectId(userId),
        reviewType: reviewType,
        status: 'approved'
      }
    },
    {
      $group: {
        _id: null,
        averageRating: { $avg: '$rating.overall' },
        totalReviews: { $sum: 1 },
        communication: { $avg: '$rating.communication' },
        reliability: { $avg: '$rating.reliability' },
        friendliness: { $avg: '$rating.friendliness' }
      }
    }
  ]);
  
  return result[0] || {
    averageRating: 0,
    totalReviews: 0,
    communication: 0,
    reliability: 0,
    friendliness: 0
  };
};

module.exports = mongoose.model('Review', reviewSchema);
