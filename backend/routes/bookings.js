const express = require('express');
const Room = require('../models/Room');
const Booking = require('../models/Booking');
const User = require('../models/User');
const Notification = require('../models/Notification');
const { authenticate, authorize, checkBookingAccess } = require('../middleware/auth');
const { validateBookingCreation, validateObjectId } = require('../middleware/validation');
const { sendEmail } = require('../utils/emailService');

const router = express.Router();

/**
 * @swagger
 * /api/bookings:
 *   get:
 *     tags: [Bookings]
 *     summary: Danh sách booking theo quyền người dùng
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: query
 *         name: page
 *         schema: { type: integer, example: 1 }
 *       - in: query
 *         name: limit
 *         schema: { type: integer, example: 10 }
 *       - in: query
 *         name: status
 *         schema: { type: string, example: pending }
 *     responses:
 *       200: { description: Thành công }
 *       401: { description: Chưa xác thực }
 */
router.get('/', authenticate, async (req, res) => {
  try {
    const {
      page = 1,
      limit = 10,
      status,
      sortBy = 'createdAt',
      sortOrder = 'desc'
    } = req.query;

    let query = {};

    if (req.user.role === 'tenant') {
      query.tenant = req.user._id;
    } else if (req.user.role === 'landlord') {
      query.landlord = req.user._id;
    }

    if (status) query.status = status;

    const skip = (page - 1) * limit;
    const sort = { [sortBy]: sortOrder === 'desc' ? -1 : 1 };

    const bookings = await Booking.find(query)
      .populate('room', 'title images address price')
      .populate('tenant', 'fullName email phone avatar')
      .populate('landlord', 'fullName email phone avatar')
      .sort(sort)
      .skip(skip)
      .limit(parseInt(limit));

    const total = await Booking.countDocuments(query);

    res.json({
      status: 'success',
      data: {
        bookings,
        pagination: {
          currentPage: parseInt(page),
          totalPages: Math.ceil(total / limit),
          totalBookings: total,
          hasNext: page < Math.ceil(total / limit),
          hasPrev: page > 1
        }
      }
    });
  } catch (error) {
    console.error('Get bookings error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy danh sách booking'
    });
  }
});

/**
 * @swagger
 * /api/bookings/{id}:
 *   get:
 *     tags: [Bookings]
 *     summary: Chi tiết booking
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200: { description: Thành công }
 *       401: { description: Chưa xác thực }
 *       403: { description: Không đủ quyền }
 *       404: { description: Không tìm thấy }
 */
/**
 * @swagger
 * /api/bookings/{id}/status:
 *   get:
 *     tags: [Bookings]
 *     summary: Kiểm tra trạng thái booking
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200: { description: Thành công }
 *       401: { description: Chưa xác thực }
 *       404: { description: Không tìm thấy }
 */
router.get('/:id/status', authenticate, validateObjectId('id'), async (req, res) => {
  try {
    const booking = await Booking.findById(req.params.id)
      .populate('room', 'title')
      .populate('tenant', 'fullName email')
      .populate('landlord', 'fullName email');

    if (!booking) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy booking'
      });
    }

    // Check permissions
    const isAdmin = req.user.role === 'admin';
    const isLandlord = req.user.role === 'landlord' && booking.landlord._id.toString() === req.user._id.toString();
    const isTenant = req.user.role === 'tenant' && booking.tenant._id.toString() === req.user._id.toString();

    if (!isAdmin && !isLandlord && !isTenant) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền xem trạng thái booking này'
      });
    }

    res.json({
      status: 'success',
      data: {
        bookingId: booking._id,
        currentStatus: booking.status,
        canBeCancelled: booking.canBeCancelled(),
        canBeConfirmed: booking.status === 'pending',
        roomTitle: booking.room.title,
        tenantName: booking.tenant.fullName,
        landlordName: booking.landlord.fullName
      }
    });
  } catch (error) {
    console.error('Get booking status error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy trạng thái booking'
    });
  }
});

router.get('/:id', authenticate, checkBookingAccess, validateObjectId('id'), async (req, res) => {
  try {
    const booking = await Booking.findById(req.params.id)
      .populate('room', 'title images address price amenities')
      .populate('tenant', 'fullName email phone avatar address tenantInfo')
      .populate('landlord', 'fullName email phone avatar address landlordInfo');

    if (!booking) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy booking'
      });
    }

    res.json({
      status: 'success',
      data: { booking }
    });
  } catch (error) {
    console.error('Get booking error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thông tin booking'
    });
  }
});

/**
 * @swagger
 * /api/bookings:
 *   post:
 *     tags: [Bookings]
 *     summary: Tạo booking (Người thuê)
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [roomId, checkInDate, checkOutDate, numberOfOccupants]
 *             properties:
 *               roomId: { type: string }
 *               checkInDate: { type: string, example: "2025-10-01" }
 *               checkOutDate: { type: string, example: "2026-10-01" }
 *               numberOfOccupants: { type: integer, example: 2 }
 *     responses:
 *       201: { description: Tạo thành công }
 *       400: { description: Dữ liệu/Trạng thái không hợp lệ }
 *       401: { description: Chưa xác thực }
 */
router.post('/', authenticate, authorize('tenant'), validateBookingCreation, async (req, res) => {
  try {
    const { roomId, bookingDetails, pricing, notes } = req.body;
    const { checkInDate, checkOutDate, numberOfOccupants, duration } = bookingDetails || {};

    const room = await Room.findById(roomId);
    if (!room) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy phòng'
      });
    }

    if (room.status !== 'active' || !room.availability.isAvailable) {
      return res.status(400).json({
        status: 'error',
        message: 'Phòng không khả dụng'
      });
    }

    if (room.landlord.toString() === req.user._id.toString()) {
      return res.status(400).json({
        status: 'error',
        message: 'Không thể đặt phòng của chính mình'
      });
    }

    const existingBooking = await Booking.findOne({
      room: roomId,
      status: { $in: ['pending', 'confirmed', 'deposit_paid', 'active'] },
      $or: [
        {
          'bookingDetails.checkInDate': { $lte: new Date(checkOutDate) },
          'bookingDetails.checkOutDate': { $gte: new Date(checkInDate) }
        }
      ]
    });

    if (existingBooking) {
      return res.status(400).json({
        status: 'error',
        message: 'Phòng đã được đặt trong khoảng thời gian này'
      });
    }
    const checkIn = new Date(checkInDate);
    const checkOut = new Date(checkOutDate);
    const calculatedDuration = Math.ceil((checkOut - checkIn) / (1000 * 60 * 60 * 24 * 30)); 

    const bookingData = {
      room: roomId,
      tenant: req.user._id,
      landlord: room.landlord,
      bookingDetails: {
        checkInDate: checkIn,
        checkOutDate: checkOut,
        duration: duration || calculatedDuration,
        numberOfOccupants: parseInt(numberOfOccupants)
      },
      pricing: {
        monthlyRent: pricing?.monthlyRent || room.price.monthly,
        deposit: pricing?.deposit || room.price.deposit,
        utilities: typeof pricing?.utilities === 'object' 
          ? (pricing.utilities.electricity + pricing.utilities.water + pricing.utilities.internet + pricing.utilities.other)
          : (pricing?.utilities || room.price.utilities || 0),
        totalAmount: pricing?.totalAmount || 0
      },
      notes: notes || {}
    };

    const booking = new Booking(bookingData);
    booking.calculateTotalAmount();

    await booking.save();

    booking.generateMonthlyPayments();
    await booking.save();
    await booking.populate([
      { path: 'room', select: 'title images address price' },
      { path: 'tenant', select: 'fullName email phone' },
      { path: 'landlord', select: 'fullName email phone' }
    ]);

    const tenantNotification = new Notification({
      recipient: req.user._id,
      type: 'booking_request',
      title: 'Yêu cầu đặt phòng đã được gửi',
      message: `Yêu cầu đặt phòng "${room.title}" đã được gửi đến chủ trọ`,
      data: {
        bookingId: booking._id,
        roomId: room._id,
        amount: booking.pricing.totalAmount
      }
    });

    const landlordNotification = new Notification({
      recipient: room.landlord,
      type: 'booking_request',
      title: 'Có yêu cầu đặt phòng mới',
      message: `Bạn có yêu cầu đặt phòng mới từ ${req.user.fullName}`,
      data: {
        bookingId: booking._id,
        roomId: room._id,
        tenantId: req.user._id,
        amount: booking.pricing.totalAmount
      }
    });

    await Promise.all([
      tenantNotification.save(),
      landlordNotification.save()
    ]);

    // Tự động tạo payment cho booking mới
    try {
      const Payment = require('../models/Payment');
      
      
      const payment = new Payment({
        booking: booking._id,
        payer: req.user._id,
        recipient: room.landlord,
        type: 'deposit',
        amount: booking.pricing.deposit,
        currency: 'VND',
        status: 'pending',
        paymentMethod: 'pending',
        description: `Đặt cọc phòng: ${room.title}`,
        initiatedAt: new Date().toISOString()
      });

      await payment.save();
    } catch (paymentError) {
    }

    try {
      if (req.user.email) {
        await sendEmail(req.user.email, 'bookingConfirmation', {
          tenantName: req.user.fullName,
          roomTitle: room.title,
          roomAddress: room.fullAddress,
          checkInDate: checkIn.toLocaleDateString('vi-VN'),
          checkOutDate: checkOut.toLocaleDateString('vi-VN'),
          duration: duration,
          deposit: booking.pricing.deposit,
          monthlyRent: booking.pricing.monthlyRent
        });
      }

      if (room.landlord && room.landlord.email) {
        await sendEmail(room.landlord.email, 'bookingNotificationToLandlord', {
          landlordName: room.landlord.fullName,
          roomTitle: room.title,
          roomAddress: room.fullAddress,
          tenantName: req.user.fullName,
          tenantPhone: req.user.phone,
          tenantEmail: req.user.email,
          checkInDate: checkIn.toLocaleDateString('vi-VN'),
          checkOutDate: checkOut.toLocaleDateString('vi-VN'),
          duration: duration,
          numberOfOccupants: numberOfOccupants
        });
      }
    } catch (emailError) {
    }

    res.status(201).json({
      status: 'success',
      message: 'Tạo booking thành công',
      data: { booking }
    });
  } catch (error) {
    console.error('Create booking error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi tạo booking'
    });
  }
});

/**
 * @swagger
 * /api/bookings/{id}/confirm:
 *   put:
 *     tags: [Bookings]
 *     summary: Xác nhận booking (Chủ trọ)
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200: { description: Thành công }
 *       400: { description: Trạng thái không hợp lệ }
 *       401: { description: Chưa xác thực }
 *       403: { description: Không đủ quyền }
 *       404: { description: Không tìm thấy }
 */
router.put('/:id/confirm', authenticate, authorize('landlord'), validateObjectId('id'), async (req, res) => {
  try {
    const booking = await Booking.findById(req.params.id)
      .populate('room', 'title')
      .populate('tenant', 'fullName email')
      .populate('landlord', 'fullName email');

    if (!booking) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy booking'
      });
    }

    if (booking.landlord._id.toString() !== req.user._id.toString()) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền xác nhận booking này'
      });
    }

    if (booking.status !== 'pending') {
      return res.status(400).json({
        status: 'error',
        message: 'Booking không ở trạng thái chờ xác nhận'
      });
    }

    booking.status = 'confirmed';
    await booking.save();

    // Cập nhật payment status khi booking được confirm
    try {
      const Payment = require('../models/Payment');
      const payment = await Payment.findOne({ booking: booking._id });
      
      if (payment && payment.status === 'pending') {
        payment.status = 'completed';
        payment.completedAt = new Date().toISOString();
        payment.paymentMethod = 'bank_transfer'; // Chủ trọ xác nhận
        await payment.save();
      }
    } catch (paymentError) {
    }

    const tenantNotification = new Notification({
      recipient: booking.tenant._id,
      type: 'booking_confirmed',
      title: 'Booking đã được xác nhận',
      message: `Booking phòng "${booking.room.title}" đã được chủ trọ xác nhận`,
      data: {
        bookingId: booking._id,
        roomId: booking.room._id
      }
    });

    await tenantNotification.save();

    try {
      if (booking.tenant && booking.tenant.email) {
        await sendEmail(booking.tenant.email, 'bookingConfirmation', {
          tenantName: booking.tenant.fullName,
          roomTitle: booking.room.title,
          roomAddress: booking.room.fullAddress,
          checkInDate: booking.bookingDetails.checkInDate.toLocaleDateString('vi-VN'),
          checkOutDate: booking.bookingDetails.checkOutDate.toLocaleDateString('vi-VN'),
          duration: booking.bookingDetails.duration,
          deposit: booking.pricing.deposit,
          monthlyRent: booking.pricing.monthlyRent
        });
      }
    } catch (emailError) {
    }

    res.json({
      status: 'success',
      message: 'Xác nhận booking thành công',
      data: { booking }
    });
  } catch (error) {
    console.error('Confirm booking error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi xác nhận booking'
    });
  }
});

/**
 * @swagger
 * /api/bookings/{id}/status:
 *   put:
 *     tags: [Bookings]
 *     summary: Cập nhật trạng thái booking (Admin/Landlord)
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [status]
 *             properties:
 *               status: { type: string, enum: [pending, confirmed, deposit_paid, active, completed, cancelled] }
 *     responses:
 *       200: { description: Thành công }
 *       400: { description: Trạng thái không hợp lệ }
 *       401: { description: Chưa xác thực }
 *       403: { description: Không đủ quyền }
 *       404: { description: Không tìm thấy }
 */
router.put('/:id/status', authenticate, validateObjectId('id'), async (req, res) => {
  try {
    const { status } = req.body;
    const validStatuses = ['pending', 'confirmed', 'deposit_paid', 'active', 'completed', 'cancelled'];
    
    if (!status || !validStatuses.includes(status)) {
      return res.status(400).json({
        status: 'error',
        message: 'Trạng thái không hợp lệ'
      });
    }

    const booking = await Booking.findById(req.params.id)
      .populate('room', 'title')
      .populate('tenant', 'fullName email')
      .populate('landlord', 'fullName email');

    if (!booking) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy booking'
      });
    }

    // Check permissions
    const isAdmin = req.user.role === 'admin';
    const isLandlord = req.user.role === 'landlord' && booking.landlord._id.toString() === req.user._id.toString();
    const isTenant = req.user.role === 'tenant' && booking.tenant._id.toString() === req.user._id.toString();

    if (!isAdmin && !isLandlord && !isTenant) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền cập nhật trạng thái booking này'
      });
    }

    // Additional validation for specific status changes
    if (status === 'confirmed' && booking.status !== 'pending') {
      return res.status(400).json({
        status: 'error',
        message: `Chỉ có thể xác nhận booking đang ở trạng thái pending. Trạng thái hiện tại: ${booking.status}`
      });
    }

    if (status === 'cancelled' && !booking.canBeCancelled()) {
      return res.status(400).json({
        status: 'error',
        message: `Booking không thể hủy. Trạng thái hiện tại: ${booking.status}. Chỉ có thể hủy booking ở trạng thái: pending, confirmed, deposit_paid`
      });
    }

    const oldStatus = booking.status;
    booking.status = status;
    
    // Determine who cancelled (if cancelling)
    let cancelledBy;
    if (status === 'cancelled') {
      if (isAdmin) {
        cancelledBy = 'admin';
      } else if (isTenant) {
        cancelledBy = 'tenant';
      } else if (isLandlord) {
        cancelledBy = 'landlord';
      }
      
      booking.cancellation = {
        cancelledBy,
        cancelledAt: new Date(),
        reason: req.body.reason || 'Không có lý do'
      };
    }

    if (status === 'deposit_paid' && oldStatus !== 'deposit_paid') {
      try {
        const Payment = require('../models/Payment');
        const paymentMethod = req.body.paymentMethod || 'bank_transfer';
        const paymentDescription = req.body.paymentDescription || `Đặt cọc phòng: ${booking.room.title} - Xác nhận bởi chủ trọ`;
        const paymentSource = req.body.paymentSource;
        const txnRef = req.body.txnRef || req.body.transactionId || (req.body.vnpay && req.body.vnpay.txnRef);
        
        let payment = await Payment.findOne({ booking: booking._id, type: 'deposit' });
        
        if (!payment) {
          payment = new Payment({
            booking: booking._id,
            payer: booking.tenant._id,
            recipient: booking.landlord._id,
            type: 'deposit',
            amount: booking.pricing ? booking.pricing.deposit : 0,
            currency: 'VND',
            status: 'completed',
            paymentMethod,
            description: paymentDescription,
            processedAt: new Date(),
            completedAt: new Date(),
            initiatedAt: new Date()
          });
        } else {
          payment.status = 'completed';
          payment.paymentMethod = paymentMethod;
          payment.processedAt = new Date();
          payment.completedAt = new Date();
          payment.description = payment.description || paymentDescription;
        }
        
        if (txnRef) {
          payment.vnpay = payment.vnpay || {};
          payment.vnpay.txnRef = txnRef;
          if (!payment.vnpay.orderInfo) {
            payment.vnpay.orderInfo = paymentDescription;
          }
        }
        
        if (paymentSource) {
          payment.metadata = payment.metadata || {};
          payment.metadata.paymentSource = paymentSource;
        }
        
        await payment.save();
        
        if (!booking.paymentStatus) {
          booking.paymentStatus = {};
        }
        booking.paymentStatus.deposit = {
          status: 'paid',
          amount: payment.amount,
          paidAt: new Date(),
          paymentMethod,
          transactionId: txnRef || payment.transactionId || payment._id.toString()
        };
      } catch (paymentError) {
      }
    }

    await booking.save();

    const notifications = [];
    
    if (oldStatus === 'pending' && status === 'confirmed') {
      notifications.push(new Notification({
        recipient: booking.tenant._id,
        type: 'booking_confirmed',
        title: 'Booking đã được xác nhận',
        message: `Booking phòng "${booking.room.title}" đã được xác nhận`,
        data: {
          bookingId: booking._id,
          roomId: booking.room._id
        }
      }));
    } else if (status === 'deposit_paid' && oldStatus !== 'deposit_paid') {
      notifications.push(new Notification({
        recipient: booking.tenant._id,
        type: 'payment_received',
        title: 'Thanh toán đã được xác nhận',
        message: `Chủ trọ đã xác nhận thanh toán đặt cọc cho phòng "${booking.room.title}"`,
        data: {
          bookingId: booking._id,
          roomId: booking.room._id,
          amount: booking.pricing ? booking.pricing.deposit : 0
        }
      }));
    } else if (status === 'cancelled') {
      const whoCancelled = cancelledBy || (booking.cancellation && booking.cancellation.cancelledBy);
      
      if (whoCancelled === 'landlord' || whoCancelled === 'admin') {
        notifications.push(new Notification({
          recipient: booking.tenant._id,
          type: 'booking_cancelled',
          title: 'Booking đã bị hủy',
          message: `Booking phòng "${booking.room.title}" đã bị hủy`,
          data: {
            bookingId: booking._id,
            roomId: booking.room._id,
            reason: booking.cancellation ? booking.cancellation.reason : 'Không có lý do'
          }
        }));
      } else if (whoCancelled === 'tenant') {
        notifications.push(new Notification({
          recipient: booking.landlord._id,
          type: 'booking_cancelled',
          title: 'Booking đã bị hủy',
          message: `Booking phòng "${booking.room.title}" đã bị người thuê hủy`,
          data: {
            bookingId: booking._id,
            roomId: booking.room._id,
            reason: booking.cancellation ? booking.cancellation.reason : 'Không có lý do'
          }
        }));
      }
    }

    if (notifications.length > 0) {
      await Notification.insertMany(notifications);
    }

    res.json({
      status: 'success',
      message: 'Cập nhật trạng thái booking thành công',
      data: { booking }
    });
  } catch (error) {
    console.error('Update booking status error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi cập nhật trạng thái booking'
    });
  }
});

router.put('/:id/cancel', authenticate, checkBookingAccess, validateObjectId('id'), async (req, res) => {
  try {
    const { reason } = req.body;

    const booking = await Booking.findById(req.params.id)
      .populate('room', 'title')
      .populate('tenant', 'fullName email')
      .populate('landlord', 'fullName email');

    if (!booking) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy booking'
      });
    }

    if (!booking.canBeCancelled()) {
      return res.status(400).json({
        status: 'error',
        message: 'Booking không thể hủy'
      });
    }

    let cancelledBy;
    if (req.user.role === 'admin') {
      cancelledBy = 'admin';
    } else if (booking.tenant._id.toString() === req.user._id.toString()) {
      cancelledBy = 'tenant';
    } else if (booking.landlord._id.toString() === req.user._id.toString()) {
      cancelledBy = 'landlord';
    } else {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền hủy booking này'
      });
    }

    booking.status = 'cancelled';
    booking.cancellation = {
      cancelledBy,
      cancelledAt: new Date(),
      reason: reason || 'Không có lý do'
    };

    await booking.save();

    // Cập nhật payment status khi booking bị cancel
    try {
      const Payment = require('../models/Payment');
      const payment = await Payment.findOne({ booking: booking._id });
      
      if (payment && payment.status === 'pending') {
        payment.status = 'failed';
        payment.failedAt = new Date().toISOString();
        payment.failureReason = 'Đặt phòng bị hủy';
        await payment.save();
      }
    } catch (paymentError) {
    }

    const notifications = [];
    
    if (cancelledBy === 'tenant') {
      notifications.push(new Notification({
        recipient: booking.landlord._id,
        type: 'booking_cancelled',
        title: 'Booking đã bị hủy',
        message: `Booking phòng "${booking.room.title}" đã bị người thuê hủy`,
        data: {
          bookingId: booking._id,
          roomId: booking.room._id,
          reason
        }
      }));
    } else if (cancelledBy === 'landlord') {
      notifications.push(new Notification({
        recipient: booking.tenant._id,
        type: 'booking_cancelled',
        title: 'Booking đã bị hủy',
        message: `Booking phòng "${booking.room.title}" đã bị chủ trọ hủy`,
        data: {
          bookingId: booking._id,
          roomId: booking.room._id,
          reason
        }
      }));
    }

    if (notifications.length > 0) {
      await Notification.insertMany(notifications);
    }

    res.json({
      status: 'success',
      message: 'Hủy booking thành công',
      data: { booking }
    });
  } catch (error) {
    console.error('Cancel booking error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi hủy booking'
    });
  }
});

router.put('/:id/contract', authenticate, checkBookingAccess, validateObjectId('id'), async (req, res) => {
  try {
    const { contractFile, terms } = req.body;

    const booking = await Booking.findById(req.params.id);

    if (!booking) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy booking'
      });
    }

    if (booking.status !== 'confirmed') {
      return res.status(400).json({
        status: 'error',
        message: 'Booking phải được xác nhận trước khi ký hợp đồng'
      });
    }

    booking.contract = {
      ...booking.contract,
      signedAt: new Date(),
      contractFile: contractFile || booking.contract.contractFile,
      terms: terms || booking.contract.terms
    };

    await booking.save();

    res.json({
      status: 'success',
      message: 'Ký hợp đồng thành công',
      data: { booking }
    });
  } catch (error) {
    console.error('Sign contract error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi ký hợp đồng'
    });
  }
});

router.get('/:id/contract', authenticate, checkBookingAccess, validateObjectId('id'), async (req, res) => {
  try {
    const booking = await Booking.findById(req.params.id)
      .populate('room', 'title address price')
      .populate('tenant', 'fullName email phone address')
      .populate('landlord', 'fullName email phone address');

    if (!booking) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy booking'
      });
    }

    res.json({
      status: 'success',
      data: { 
        contract: booking.contract,
        booking: {
          id: booking._id,
          contractNumber: booking.contract.contractNumber,
          room: booking.room,
          tenant: booking.tenant,
          landlord: booking.landlord,
          bookingDetails: booking.bookingDetails,
          pricing: booking.pricing
        }
      }
    });
  } catch (error) {
    console.error('Get contract error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thông tin hợp đồng'
    });
  }
});

router.get('/stats/overview', authenticate, async (req, res) => {
  try {
    let matchQuery = {};

    if (req.user.role === 'tenant') {
      matchQuery.tenant = req.user._id;
    } else if (req.user.role === 'landlord') {
      matchQuery.landlord = req.user._id;
    }

    const stats = await Booking.aggregate([
      { $match: matchQuery },
      {
        $group: {
          _id: null,
          totalBookings: { $sum: 1 },
          pendingBookings: {
            $sum: { $cond: [{ $eq: ['$status', 'pending'] }, 1, 0] }
          },
          confirmedBookings: {
            $sum: { $cond: [{ $eq: ['$status', 'confirmed'] }, 1, 0] }
          },
          activeBookings: {
            $sum: { $cond: [{ $eq: ['$status', 'active'] }, 1, 0] }
          },
          completedBookings: {
            $sum: { $cond: [{ $eq: ['$status', 'completed'] }, 1, 0] }
          },
          cancelledBookings: {
            $sum: { $cond: [{ $eq: ['$status', 'cancelled'] }, 1, 0] }
          },
          totalRevenue: {
            $sum: { $cond: [{ $eq: ['$status', 'completed'] }, '$pricing.totalAmount', 0] }
          }
        }
      }
    ]);

    res.json({
      status: 'success',
      data: {
        stats: stats[0] || {
          totalBookings: 0,
          pendingBookings: 0,
          confirmedBookings: 0,
          activeBookings: 0,
          completedBookings: 0,
          cancelledBookings: 0,
          totalRevenue: 0
        }
      }
    });
  } catch (error) {
    console.error('Get booking stats error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thống kê booking'
    });
  }
});

module.exports = router;
