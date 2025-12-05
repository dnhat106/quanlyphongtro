const express = require('express');
const User = require('../models/User');
const Room = require('../models/Room');
const Booking = require('../models/Booking');
const Payment = require('../models/Payment');
const Notification = require('../models/Notification');
const { authenticate, authorize } = require('../middleware/auth');

const router = express.Router();

/**
 * @swagger
 * /api/statistics/dashboard:
 *   get:
 *     tags: [Statistics]
 *     summary: Thống kê tổng quan theo vai trò người dùng
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200: { description: Thành công }
 *       401: { description: Chưa xác thực }
 */
router.get('/dashboard', authenticate, async (req, res) => {
  try {
    const userRole = req.user.role;
    let stats = {};

    if (userRole === 'admin') {
      stats = await getAdminDashboardStats();
    } else if (userRole === 'landlord') {
      stats = await getLandlordDashboardStats(req.user._id);
    } else if (userRole === 'tenant') {
      stats = await getTenantDashboardStats(req.user._id);
    }

    res.json({
      status: 'success',
      data: { stats }
    });
  } catch (error) {
    console.error('Get dashboard statistics error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thống kê dashboard'
    });
  }
});

router.get('/revenue', authenticate, async (req, res) => {
  try {
    const { period = 'month', startDate, endDate } = req.query;
    const userRole = req.user.role;
    let matchQuery = {};

    if (userRole === 'landlord') {
      matchQuery.recipient = req.user._id;
    } else if (userRole === 'tenant') {
      matchQuery.payer = req.user._id;
    }

    if (startDate && endDate) {
      matchQuery.createdAt = {
        $gte: new Date(startDate),
        $lte: new Date(endDate)
      };
    }

    const revenueStats = await Payment.aggregate([
      { $match: { ...matchQuery, status: 'completed' } },
      {
        $group: {
          _id: {
            year: { $year: '$createdAt' },
            month: { $month: '$createdAt' },
            day: period === 'day' ? { $dayOfMonth: '$createdAt' } : null
          },
          totalRevenue: { $sum: '$amount' },
          transactionCount: { $sum: 1 },
          averageAmount: { $avg: '$amount' }
        }
      },
      { $sort: { '_id.year': 1, '_id.month': 1, '_id.day': 1 } }
    ]);

    const totalRevenue = await Payment.aggregate([
      { $match: { ...matchQuery, status: 'completed' } },
      {
        $group: {
          _id: null,
          total: { $sum: '$amount' },
          count: { $sum: 1 }
        }
      }
    ]);

    res.json({
      status: 'success',
      data: {
        revenueStats,
        totalRevenue: totalRevenue[0] || { total: 0, count: 0 }
      }
    });
  } catch (error) {
    console.error('Get revenue statistics error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thống kê doanh thu'
    });
  }
});

router.get('/bookings', authenticate, async (req, res) => {
  try {
    const { period = 'month' } = req.query;
    const userRole = req.user.role;
    let matchQuery = {};

    if (userRole === 'landlord') {
      matchQuery.landlord = req.user._id;
    } else if (userRole === 'tenant') {
      matchQuery.tenant = req.user._id;
    }

    const bookingStats = await Booking.aggregate([
      { $match: matchQuery },
      {
        $group: {
          _id: {
            year: { $year: '$createdAt' },
            month: { $month: '$createdAt' },
            day: period === 'day' ? { $dayOfMonth: '$createdAt' } : null,
            status: '$status'
          },
          count: { $sum: 1 },
          totalRevenue: { $sum: '$pricing.totalAmount' }
        }
      },
      { $sort: { '_id.year': 1, '_id.month': 1, '_id.day': 1 } }
    ]);

    const statusStats = await Booking.aggregate([
      { $match: matchQuery },
      {
        $group: {
          _id: '$status',
          count: { $sum: 1 },
          totalRevenue: { $sum: '$pricing.totalAmount' }
        }
      }
    ]);

    res.json({
      status: 'success',
      data: {
        bookingStats,
        statusStats
      }
    });
  } catch (error) {
    console.error('Get booking statistics error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thống kê booking'
    });
  }
});

router.get('/rooms', authenticate, async (req, res) => {
  try {
    const userRole = req.user.role;
    let matchQuery = {};

    if (userRole === 'landlord') {
      matchQuery.landlord = req.user._id;
    }

    const roomStats = await Room.aggregate([
      { $match: matchQuery },
      {
        $group: {
          _id: {
            status: '$status',
            roomType: '$roomType'
          },
          count: { $sum: 1 },
          averagePrice: { $avg: '$price.monthly' },
          totalViews: { $sum: '$views' },
          averageRating: { $avg: '$rating.average' }
        }
      }
    ]);

    const cityStats = await Room.aggregate([
      { $match: matchQuery },
      {
        $group: {
          _id: '$address.city',
          count: { $sum: 1 },
          averagePrice: { $avg: '$price.monthly' }
        }
      },
      { $sort: { count: -1 } },
      { $limit: 10 }
    ]);

    res.json({
      status: 'success',
      data: {
        roomStats,
        cityStats
      }
    });
  } catch (error) {
    console.error('Get room statistics error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thống kê phòng'
    });
  }
});

router.get('/users', authenticate, authorize('admin'), async (req, res) => {
  try {
    const userStats = await User.aggregate([
      {
        $group: {
          _id: {
            role: '$role',
            isActive: '$isActive',
            isVerified: '$isVerified'
          },
          count: { $sum: 1 }
        }
      }
    ]);

    const registrationStats = await User.aggregate([
      {
        $group: {
          _id: {
            year: { $year: '$createdAt' },
            month: { $month: '$createdAt' }
          },
          count: { $sum: 1 }
        }
      },
      { $sort: { '_id.year': 1, '_id.month': 1 } }
    ]);

    const totalUsers = await User.countDocuments();
    const activeUsers = await User.countDocuments({ isActive: true });
    const verifiedUsers = await User.countDocuments({ isVerified: true });

    res.json({
      status: 'success',
      data: {
        userStats,
        registrationStats,
        overview: {
          totalUsers,
          activeUsers,
          verifiedUsers
        }
      }
    });
  } catch (error) {
    console.error('Get user statistics error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thống kê người dùng'
    });
  }
});

async function getAdminDashboardStats() {
  const [
    userStats,
    roomStats,
    bookingStats,
    paymentStats,
    recentBookings,
    recentPayments
  ] = await Promise.all([
    User.aggregate([
      {
        $group: {
          _id: '$role',
          count: { $sum: 1 },
          activeCount: {
            $sum: { $cond: [{ $eq: ['$isActive', true] }, 1, 0] }
          }
        }
      }
    ]),
    Room.aggregate([
      {
        $group: {
          _id: null,
          totalRooms: { $sum: 1 },
          activeRooms: {
            $sum: { $cond: [{ $eq: ['$status', 'active'] }, 1, 0] }
          },
          averagePrice: { $avg: '$price.monthly' },
          totalViews: { $sum: '$views' }
        }
      }
    ]),
    Booking.aggregate([
      {
        $group: {
          _id: null,
          totalBookings: { $sum: 1 },
          pendingBookings: {
            $sum: { $cond: [{ $eq: ['$status', 'pending'] }, 1, 0] }
          },
          activeBookings: {
            $sum: { $cond: [{ $eq: ['$status', 'active'] }, 1, 0] }
          },
          totalRevenue: { $sum: '$pricing.totalAmount' }
        }
      }
    ]),
    Payment.aggregate([
      {
        $group: {
          _id: null,
          totalPayments: { $sum: 1 },
          completedPayments: {
            $sum: { $cond: [{ $eq: ['$status', 'completed'] }, 1, 0] }
          },
          totalAmount: { $sum: '$amount' }
        }
      }
    ]),
    Booking.find()
      .populate('tenant', 'fullName email')
      .populate('landlord', 'fullName email')
      .populate('room', 'title')
      .sort({ createdAt: -1 })
      .limit(5),
    Payment.find()
      .populate('payer', 'fullName email')
      .populate('recipient', 'fullName email')
      .sort({ createdAt: -1 })
      .limit(5)
  ]);

  return {
    users: userStats,
    rooms: roomStats[0] || { totalRooms: 0, activeRooms: 0, averagePrice: 0, totalViews: 0 },
    bookings: bookingStats[0] || { totalBookings: 0, pendingBookings: 0, activeBookings: 0, totalRevenue: 0 },
    payments: paymentStats[0] || { totalPayments: 0, completedPayments: 0, totalAmount: 0 },
    recentBookings,
    recentPayments
  };
}

async function getLandlordDashboardStats(landlordId) {
  const [
    roomStats,
    bookingStats,
    paymentStats,
    recentBookings,
    recentPayments,
    monthlyRevenue
  ] = await Promise.all([
    Room.aggregate([
      { $match: { landlord: landlordId } },
      {
        $group: {
          _id: null,
          totalRooms: { $sum: 1 },
          activeRooms: {
            $sum: { $cond: [{ $eq: ['$status', 'active'] }, 1, 0] }
          },
          averagePrice: { $avg: '$price.monthly' },
          totalViews: { $sum: '$views' }
        }
      }
    ]),
    Booking.aggregate([
      { $match: { landlord: landlordId } },
      {
        $group: {
          _id: null,
          totalBookings: { $sum: 1 },
          pendingBookings: {
            $sum: { $cond: [{ $eq: ['$status', 'pending'] }, 1, 0] }
          },
          activeBookings: {
            $sum: { $cond: [{ $eq: ['$status', 'active'] }, 1, 0] }
          },
          totalRevenue: { $sum: '$pricing.totalAmount' }
        }
      }
    ]),
    Payment.aggregate([
      { $match: { recipient: landlordId, status: 'completed' } },
      {
        $group: {
          _id: null,
          totalAmount: { $sum: '$amount' },
          count: { $sum: 1 }
        }
      }
    ]),
    Booking.find({ landlord: landlordId })
      .populate('tenant', 'fullName email')
      .populate('room', 'title')
      .sort({ createdAt: -1 })
      .limit(5),
    Payment.find({ recipient: landlordId })
      .populate('payer', 'fullName email')
      .sort({ createdAt: -1 })
      .limit(5),
    Payment.aggregate([
      { 
        $match: { 
          recipient: landlordId, 
          status: 'completed',
          createdAt: { $gte: new Date(new Date().getFullYear(), new Date().getMonth(), 1) }
        } 
      },
      {
        $group: {
          _id: { $month: '$createdAt' },
          amount: { $sum: '$amount' }
        }
      }
    ])
  ]);

  return {
    rooms: roomStats[0] || { totalRooms: 0, activeRooms: 0, averagePrice: 0, totalViews: 0 },
    bookings: bookingStats[0] || { totalBookings: 0, pendingBookings: 0, activeBookings: 0, totalRevenue: 0 },
    payments: paymentStats[0] || { totalAmount: 0, count: 0 },
    recentBookings,
    recentPayments,
    monthlyRevenue
  };
}

async function getTenantDashboardStats(tenantId) {
  const [
    bookingStats,
    paymentStats,
    recentBookings,
    recentPayments
  ] = await Promise.all([
    Booking.aggregate([
      { $match: { tenant: tenantId } },
      {
        $group: {
          _id: null,
          totalBookings: { $sum: 1 },
          pendingBookings: {
            $sum: { $cond: [{ $eq: ['$status', 'pending'] }, 1, 0] }
          },
          activeBookings: {
            $sum: { $cond: [{ $eq: ['$status', 'active'] }, 1, 0] }
          },
          completedBookings: {
            $sum: { $cond: [{ $eq: ['$status', 'completed'] }, 1, 0] }
          },
          totalDeposit: { $sum: '$pricing.deposit' },
          totalSpent: { $sum: '$pricing.totalAmount' }
        }
      }
    ]),
    Payment.aggregate([
      { $match: { payer: tenantId, status: 'completed' } },
      {
        $group: {
          _id: null,
          totalAmount: { $sum: '$amount' },
          count: { $sum: 1 }
        }
      }
    ]),
    Booking.find({ tenant: tenantId })
      .populate('landlord', 'fullName email')
      .populate('room', 'title images')
      .sort({ createdAt: -1 })
      .limit(5),
    Payment.find({ payer: tenantId })
      .populate('recipient', 'fullName email')
      .sort({ createdAt: -1 })
      .limit(5)
  ]);

  return {
    bookings: bookingStats[0] || { totalBookings: 0, activeBookings: 0, completedBookings: 0, totalSpent: 0 },
    payments: paymentStats[0] || { totalAmount: 0, count: 0 },
    recentBookings,
    recentPayments
  };
}

module.exports = router;
