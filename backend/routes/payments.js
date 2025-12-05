const express = require('express');
const Payment = require('../models/Payment');
const Booking = require('../models/Booking');
const Notification = require('../models/Notification');
const { authenticate, authorize } = require('../middleware/auth');
const { validatePayment, validateObjectId } = require('../middleware/validation');
const vnpayService = require('../utils/vnpayService');
const { sendEmail } = require('../utils/emailService');

const router = express.Router();

/**
 * @swagger
 * /api/payments:
 *   get:
 *     tags: [Payments]
 *     summary: Danh sách thanh toán theo quyền người dùng
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
 *         schema: { type: string, example: completed }
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
      type,
      paymentMethod,
      sortBy = 'createdAt',
      sortOrder = 'desc'
    } = req.query;

    let query = {};

    if (req.user.role === 'tenant') {
      query.payer = req.user._id;
    } else if (req.user.role === 'landlord') {
      query.recipient = req.user._id;
    }

    if (status) query.status = status;
    if (type) query.type = type;
    if (paymentMethod) query.paymentMethod = paymentMethod;

    const skip = (page - 1) * limit;
    const sort = { [sortBy]: sortOrder === 'desc' ? -1 : 1 };

    const payments = await Payment.find(query)
      .populate('booking', 'contractNumber status')
      .populate('payer', 'fullName email phone')
      .populate('recipient', 'fullName email phone')
      .sort(sort)
      .skip(skip)
      .limit(parseInt(limit));

    // Tự động tạo payment record cho các booking đã deposit_paid nhưng chưa có payment
    // (để xử lý các booking đã được chủ trọ xác nhận trước khi có logic tạo payment)
    try {
      let bookingQuery = {};
      if (req.user.role === 'tenant') {
        bookingQuery.tenant = req.user._id;
      } else if (req.user.role === 'landlord') {
        bookingQuery.landlord = req.user._id;
      }
      
      bookingQuery.status = 'deposit_paid';
      
      // Lấy danh sách booking IDs đã có payment
      const existingPaymentBookingIds = payments
        .map(p => p.booking?._id?.toString())
        .filter(Boolean);
      
      // Tìm các booking deposit_paid chưa có payment
      const bookingsWithoutPayment = await Booking.find({
        ...bookingQuery,
        _id: { $nin: existingPaymentBookingIds }
      })
        .populate('tenant', 'fullName email phone')
        .populate('landlord', 'fullName email phone')
        .populate('room', 'title');
      
      // Tạo payment record cho các booking này
      const newPayments = [];
      for (const booking of bookingsWithoutPayment) {
        try {
          const payment = new Payment({
            booking: booking._id,
            payer: booking.tenant._id,
            recipient: booking.landlord._id,
            type: 'deposit',
            amount: booking.pricing ? booking.pricing.deposit : 0,
            currency: 'VND',
            status: 'completed',
            paymentMethod: 'bank_transfer',
            description: `Đặt cọc phòng: ${booking.room.title} - Xác nhận bởi chủ trọ`,
            processedAt: booking.paymentStatus?.deposit?.paidAt || booking.updatedAt || new Date(),
            completedAt: booking.paymentStatus?.deposit?.paidAt || booking.updatedAt || new Date(),
            initiatedAt: booking.createdAt || new Date()
          });
          
          await payment.save();
          newPayments.push(payment);
        } catch (paymentError) {
          console.error('Error creating payment for booking:', booking._id, paymentError);
        }
      }
      
      // Nếu có payment mới được tạo, thêm vào danh sách payments hiện tại
      if (newPayments.length > 0) {
        // Populate thông tin cho các payment mới
        const populatedNewPayments = await Payment.find({
          _id: { $in: newPayments.map(p => p._id) }
        })
          .populate('booking', 'contractNumber status')
          .populate('payer', 'fullName email phone')
          .populate('recipient', 'fullName email phone');
        
        // Thêm vào đầu danh sách payments (mới nhất trước)
        payments.unshift(...populatedNewPayments);
        
        // Sắp xếp lại theo sortBy và sortOrder
        if (sortBy === 'createdAt') {
          payments.sort((a, b) => {
            const aVal = a.createdAt || new Date(0);
            const bVal = b.createdAt || new Date(0);
            return sortOrder === 'desc' ? bVal - aVal : aVal - bVal;
          });
        }
        
        // Giới hạn lại theo limit
        if (payments.length > parseInt(limit)) {
          payments.splice(parseInt(limit));
        }
      }
    } catch (migrationError) {
      console.error('Error migrating bookings to payments:', migrationError);
      // Không throw error, chỉ log để không ảnh hưởng đến response
    }

    // Lấy bookings chưa có payment (chỉ cho admin)
    let unpaidBookings = [];
    if (req.user.role === 'admin') {
      // Tìm bookings có status 'pending' nhưng chưa có payment
      const existingPaymentBookings = payments.map(p => p.booking?._id).filter(Boolean);
      
      let bookingQuery = { 
        status: 'pending',
        _id: { $nin: existingPaymentBookings }
      };
      
      unpaidBookings = await Booking.find(bookingQuery)
        .populate('tenant', 'fullName email phone')
        .populate('landlord', 'fullName email phone')
        .populate('room', 'title price')
        .sort(sort)
        .skip(skip)
        .limit(parseInt(limit));
    }

    const totalPayments = await Payment.countDocuments(query);
    const totalUnpaidBookings = req.user.role === 'admin' ? await Booking.countDocuments({ 
      status: 'pending',
      _id: { $nin: payments.map(p => p.booking?._id).filter(Boolean) }
    }) : 0;
    
    const totalItems = totalPayments + totalUnpaidBookings;

    res.json({
      status: 'success',
      data: {
        payments,
        unpaidBookings,
        pagination: {
          currentPage: parseInt(page),
          totalPages: Math.ceil(totalItems / limit),
          totalPayments,
          totalUnpaidBookings,
          totalItems,
          hasNext: page < Math.ceil(totalItems / limit),
          hasPrev: page > 1
        }
      }
    });
  } catch (error) {
    console.error('Get payments error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy danh sách thanh toán'
    });
  }
});

router.get('/:id', authenticate, validateObjectId('id'), async (req, res) => {
  try {
    const payment = await Payment.findById(req.params.id)
      .populate('booking', 'contractNumber bookingDetails pricing')
      .populate({
        path: 'booking',
        populate: {
          path: 'room',
          select: 'title address'
        }
      })
      .populate('payer', 'fullName email phone')
      .populate('recipient', 'fullName email phone address');

    if (!payment) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy thanh toán'
      });
    }

    if (req.user.role !== 'admin' && 
        payment.payer._id.toString() !== req.user._id.toString() &&
        payment.recipient._id.toString() !== req.user._id.toString()) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền xem thanh toán này'
      });
    }

    res.json({
      status: 'success',
      data: { payment }
    });
  } catch (error) {
    console.error('Get payment error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thông tin thanh toán'
    });
  }
});

// API endpoint để lấy thông tin chủ trọ sau khi thanh toán thành công
router.get('/success/:txnRef/landlord-info', async (req, res) => {
  try {
    const { txnRef } = req.params;
    
    const payment = await Payment.findOne({ 'vnpay.txnRef': txnRef })
      .populate('recipient', 'fullName phone address')
      .populate({
        path: 'booking',
        populate: {
          path: 'room',
          select: 'title address'
        }
      });

    if (!payment) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy thanh toán'
      });
    }

    // Lấy địa chỉ từ chủ trọ hoặc từ phòng
    let address = '';
    if (payment.recipient.address) {
      const addr = payment.recipient.address;
      address = `${addr.street || ''}, ${addr.ward || ''}, ${addr.district || ''}, ${addr.city || ''}`.replace(/^,\s*|,\s*$/g, '');
    }
    
    if (!address && payment.booking && payment.booking.room && payment.booking.room.address) {
      const roomAddr = payment.booking.room.address;
      address = `${roomAddr.street || ''}, ${roomAddr.ward || ''}, ${roomAddr.district || ''}, ${roomAddr.city || ''}`.replace(/^,\s*|,\s*$/g, '');
    }

    res.json({
      status: 'success',
      data: {
        landlord: {
          fullName: payment.recipient.fullName,
          phone: payment.recipient.phone,
          address: address
        },
        paymentId: payment._id,
        bookingId: payment.booking ? payment.booking._id : null
      }
    });
  } catch (error) {
    console.error('Get landlord info error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thông tin chủ trọ'
    });
  }
});

/**
 * @swagger
 * /api/payments/vnpay/create:
 *   post:
 *     tags: [Payments]
 *     summary: Tạo URL thanh toán VNPay cho booking
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [bookingId, amount]
 *             properties:
 *               bookingId: { type: string }
 *               type: { type: string, example: deposit }
 *               amount: { type: number, example: 1000000 }
 *               orderInfo: { type: string }
 *     responses:
 *       200: { description: Thành công }
 *       400: { description: Dữ liệu không hợp lệ }
 *       401: { description: Chưa xác thực }
 */
router.post('/vnpay/create', authenticate, async (req, res) => {
  try {
    const { bookingId, type = 'deposit', amount, orderInfo } = req.body;

    if (!bookingId || !amount) {
      return res.status(400).json({
        status: 'error',
        message: 'Booking ID và số tiền là bắt buộc'
      });
    }

    const booking = await Booking.findById(bookingId)
      .populate('room', 'title')
      .populate('tenant', 'fullName email')
      .populate('landlord', 'fullName email');

    if (!booking) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy booking'
      });
    }

    if (req.user.role !== 'admin' && 
        booking.tenant._id.toString() !== req.user._id.toString()) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền thanh toán cho booking này'
      });
    }

    const txnRef = vnpayService.generateTxnRef('BOOK');
    const payment = new Payment({
      booking: bookingId,
      payer: req.user._id,
      recipient: booking.landlord._id,
      type: type,
      amount: parseFloat(amount),
      paymentMethod: 'vnpay',
      status: 'pending',
      vnpay: {
        txnRef: txnRef,
        orderInfo: orderInfo || `Thanh toan dat coc phong ${booking.room.title}`,
        orderType: 'other',
        amount: parseFloat(amount)
      },
      description: `Thanh toán ${type} cho booking ${booking.contract.contractNumber}`,
      metadata: {
        userAgent: req.get('User-Agent'),
        ipAddress: req.ip || req.connection.remoteAddress
      }
    });

    await payment.save();

    const paymentUrl = vnpayService.createPaymentUrl({
      amount: parseFloat(amount),
      orderInfo: payment.vnpay.orderInfo,
      txnRef: txnRef,
      ipAddr: req.ip || req.connection.remoteAddress
    });

    res.json({
      status: 'success',
      message: 'Tạo URL thanh toán thành công',
      data: {
        paymentUrl,
        txnRef,
        amount: parseFloat(amount)
      }
    });
  } catch (error) {
    console.error('Create VNPay payment error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi tạo thanh toán VNPay'
    });
  }
});

/**
 * @swagger
 * /api/payments/vnpay-return:
 *   get:
 *     tags: [Payments]
 *     summary: URL trả về từ VNPay (redirect)
 *     responses:
 *       302:
 *         description: Redirect về trang client
 */
router.get('/vnpay-return', async (req, res) => {
  try {
    const vnpParams = req.query;

    const paymentResult = vnpayService.parsePaymentResult(vnpParams);

    if (!paymentResult.isValid) {
      return res.redirect(`${process.env.CLIENT_URL}/payment/failed?message=Invalid signature`);
    }

    const payment = await Payment.findOne({ 'vnpay.txnRef': paymentResult.txnRef })
      .populate('booking', 'contractNumber')
      .populate({
        path: 'booking',
        populate: {
          path: 'room',
          select: 'title address'
        }
      })
      .populate('payer', 'fullName email phone')
      .populate('recipient', 'fullName email phone address');

    if (!payment) {
      return res.redirect(`${process.env.CLIENT_URL}/payment/failed?message=Payment not found`);
    }

    if (paymentResult.responseCode === '00') {
      payment.status = 'completed';
      payment.processedAt = new Date();
      payment.completedAt = new Date();
      payment.externalTransactionId = paymentResult.transactionNo;
      payment.vnpay = {
        ...payment.vnpay,
        ...vnpParams
      };

      if (payment.type === 'deposit') {
        const booking = await Booking.findById(payment.booking._id);
        if (booking) {
          booking.status = 'deposit_paid';
          booking.paymentStatus.deposit = {
            status: 'paid',
            amount: payment.amount,
            paidAt: new Date(),
            paymentMethod: 'vnpay',
            transactionId: payment.transactionId
          };
          await booking.save();
        }
      }

      const payerNotification = new Notification({
        recipient: payment.payer._id,
        type: 'payment_received',
        title: 'Thanh toán thành công',
        message: `Thanh toán ${payment.amount.toLocaleString('vi-VN')} VNĐ đã được xử lý thành công`,
        data: {
          paymentId: payment._id,
          bookingId: payment.booking._id,
          amount: payment.amount
        }
      });

      const recipientNotification = new Notification({
        recipient: payment.recipient._id,
        type: 'payment_received',
        title: 'Nhận thanh toán',
        message: `Bạn đã nhận thanh toán ${payment.amount.toLocaleString('vi-VN')} VNĐ từ ${payment.payer.fullName}`,
        data: {
          paymentId: payment._id,
          bookingId: payment.booking._id,
          amount: payment.amount
        }
      });

      await Promise.all([
        payerNotification.save(),
        recipientNotification.save()
      ]);

      try {
        await sendEmail(payment.payer.email, 'paymentConfirmation', {
          payerName: payment.payer.fullName,
          transactionId: payment.transactionId,
          paymentType: payment.type,
          amount: payment.amount,
          paymentMethod: 'VNPay',
          paidAt: new Date().toLocaleString('vi-VN')
        });

        await sendEmail(payment.recipient.email, 'paymentConfirmation', {
          payerName: payment.recipient.fullName,
          transactionId: payment.transactionId,
          paymentType: payment.type,
          amount: payment.amount,
          paymentMethod: 'VNPay',
          paidAt: new Date().toLocaleString('vi-VN')
        });
      } catch (emailError) {
        console.error('Error sending payment confirmation emails:', emailError);
      }

      await payment.save();

      // Tạo URL redirect với thông tin chủ trọ để hiển thị cho khách hàng
      const landlordPhone = payment.recipient.phone || '';
      const landlordAddress = payment.recipient.address ? 
        `${payment.recipient.address.street || ''}, ${payment.recipient.address.ward || ''}, ${payment.recipient.address.district || ''}, ${payment.recipient.address.city || ''}`.replace(/^,\s*|,\s*$/g, '') : '';
      
      // Nếu không có địa chỉ của chủ trọ, lấy địa chỉ từ phòng
      let finalAddress = landlordAddress;
      if (!finalAddress && payment.booking && payment.booking.room && payment.booking.room.address) {
        const roomAddr = payment.booking.room.address;
        finalAddress = `${roomAddr.street || ''}, ${roomAddr.ward || ''}, ${roomAddr.district || ''}, ${roomAddr.city || ''}`.replace(/^,\s*|,\s*$/g, '');
      }

      const successUrl = `${process.env.CLIENT_URL}/payment/success?txnRef=${paymentResult.txnRef}&landlordPhone=${encodeURIComponent(landlordPhone)}&landlordAddress=${encodeURIComponent(finalAddress)}&paymentId=${payment._id}`;
      
      return res.redirect(successUrl);
    } else {
      payment.status = 'failed';
      payment.failedAt = new Date();
      payment.failureReason = paymentResult.message;
      await payment.save();

      return res.redirect(`${process.env.CLIENT_URL}/payment/failed?message=${encodeURIComponent(paymentResult.message)}`);
    }
  } catch (error) {
    console.error('VNPay return error:', error);
    return res.redirect(`${process.env.CLIENT_URL}/payment/failed?message=Payment processing error`);
  }
});

router.post('/vnpay/ipn', async (req, res) => {
  try {
    const vnpParams = req.body;

    const paymentResult = vnpayService.parsePaymentResult(vnpParams);

    if (!paymentResult.isValid) {
      return res.status(400).json({ RspCode: '97', Message: 'Invalid signature' });
    }

    const payment = await Payment.findOne({ 'vnpay.txnRef': paymentResult.txnRef });

    if (!payment) {
      return res.status(400).json({ RspCode: '01', Message: 'Payment not found' });
    }

    payment.vnpay = {
      ...payment.vnpay,
      ...vnpParams
    };

    if (paymentResult.responseCode === '00') {
      if (payment.status !== 'completed') {
        payment.status = 'completed';
        payment.processedAt = new Date();
        payment.completedAt = new Date();
        payment.externalTransactionId = paymentResult.transactionNo;

        if (payment.type === 'deposit') {
          const booking = await Booking.findById(payment.booking);
          if (booking && booking.status === 'confirmed') {
            booking.status = 'deposit_paid';
            booking.paymentStatus.deposit = {
              status: 'paid',
              amount: payment.amount,
              paidAt: new Date(),
              paymentMethod: 'vnpay',
              transactionId: payment.transactionId
            };
            await booking.save();
          }
        }
      }
    } else {
      payment.status = 'failed';
      payment.failedAt = new Date();
      payment.failureReason = paymentResult.message;
    }

    await payment.save();

    res.json({ RspCode: '00', Message: 'Success' });
  } catch (error) {
    console.error('VNPay IPN error:', error);
    res.status(500).json({ RspCode: '99', Message: 'Internal error' });
  }
});

router.post('/bank-transfer', authenticate, async (req, res) => {
  try {
    const { 
      bookingId, 
      type = 'deposit', 
      amount, 
      bankName, 
      accountNumber, 
      accountHolder,
      transferNote,
      receiptImage 
    } = req.body;

    if (!bookingId || !amount) {
      return res.status(400).json({
        status: 'error',
        message: 'Booking ID và số tiền là bắt buộc'
      });
    }
    const booking = await Booking.findById(bookingId)
      .populate('landlord', 'fullName email');

    if (!booking) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy booking'
      });
    }

    if (req.user.role !== 'admin' && 
        booking.tenant.toString() !== req.user._id.toString()) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền thanh toán cho booking này'
      });
    }

    const payment = new Payment({
      booking: bookingId,
      payer: req.user._id,
      recipient: booking.landlord._id,
      type: type,
      amount: parseFloat(amount),
      paymentMethod: 'bank_transfer',
      status: 'pending',
      bankTransfer: {
        bankName,
        accountNumber,
        accountHolder,
        transferNote,
        receiptImage
      },
      description: `Chuyển khoản ${type} cho booking ${booking.contract.contractNumber}`,
      metadata: {
        userAgent: req.get('User-Agent'),
        ipAddress: req.ip || req.connection.remoteAddress
      }
    });

    await payment.save();

    res.json({
      status: 'success',
      message: 'Tạo thanh toán chuyển khoản thành công',
      data: { payment }
    });
  } catch (error) {
    console.error('Create bank transfer payment error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi tạo thanh toán chuyển khoản'
    });
  }
});

router.put('/:id/confirm', authenticate, authorize('landlord', 'admin'), validateObjectId('id'), async (req, res) => {
  try {
    const payment = await Payment.findById(req.params.id)
      .populate('booking', 'contractNumber')
      .populate('payer', 'fullName email')
      .populate('recipient', 'fullName email');

    if (!payment) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy thanh toán'
      });
    }

    if (req.user.role !== 'admin' && 
        payment.recipient._id.toString() !== req.user._id.toString()) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền xác nhận thanh toán này'
      });
    }

    if (payment.status !== 'pending') {
      return res.status(400).json({
        status: 'error',
        message: 'Thanh toán không ở trạng thái chờ xác nhận'
      });
    }

    payment.status = 'completed';
    payment.processedAt = new Date();
    payment.completedAt = new Date();

    if (payment.type === 'deposit') {
      const booking = await Booking.findById(payment.booking._id);
      if (booking) {
        booking.status = 'deposit_paid';
        booking.paymentStatus.deposit = {
          status: 'paid',
          amount: payment.amount,
          paidAt: new Date(),
          paymentMethod: 'bank_transfer',
          transactionId: payment.transactionId
        };
        await booking.save();
      }
    }

    const payerNotification = new Notification({
      recipient: payment.payer._id,
      type: 'payment_received',
      title: 'Thanh toán đã được xác nhận',
      message: `Thanh toán ${payment.amount.toLocaleString('vi-VN')} VNĐ đã được xác nhận`,
      data: {
        paymentId: payment._id,
        bookingId: payment.booking._id,
        amount: payment.amount
      }
    });

    await payerNotification.save();

    await payment.save();

    res.json({
      status: 'success',
      message: 'Xác nhận thanh toán thành công',
      data: { payment }
    });
  } catch (error) {
    console.error('Confirm payment error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi xác nhận thanh toán'
    });
  }
});

router.get('/stats/overview', authenticate, async (req, res) => {
  try {
    let matchQuery = {};
    if (req.user.role === 'tenant') {
      matchQuery.payer = req.user._id;
    } else if (req.user.role === 'landlord') {
      matchQuery.recipient = req.user._id;
    }

    const stats = await Payment.getPaymentStats(matchQuery);

    res.json({
      status: 'success',
      data: { stats }
    });
  } catch (error) {
    console.error('Get payment stats error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thống kê thanh toán'
    });
  }
});

module.exports = router;
