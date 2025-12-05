const express = require('express');
const multer = require('multer');
const path = require('path');
const bcrypt = require('bcryptjs');
const User = require('../models/User');
const { authenticate, authorize, checkOwnership } = require('../middleware/auth');
const { validateUserRegistration, validateUserUpdate, validateObjectId } = require('../middleware/validation');

const router = express.Router();

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, 'uploads/avatars/');
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, 'avatar-' + uniqueSuffix + path.extname(file.originalname));
  }
});

const upload = multer({
  storage: storage,
  limits: {
    fileSize: 5 * 1024 * 1024 
  },
  fileFilter: (req, file, cb) => {
    if (file.mimetype.startsWith('image/')) {
      cb(null, true);
    } else {
      cb(new Error('Chỉ cho phép upload file hình ảnh'), false);
    }
  }
});

/**
 * @swagger
 * /api/users:
 *   get:
 *     tags: [Users]
 *     summary: Danh sách người dùng (Admin)
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: query
 *         name: page
 *         schema: { type: integer, example: 1 }
 *       - in: query
 *         name: limit
 *         schema: { type: integer, example: 20 }
 *       - in: query
 *         name: role
 *         schema: { type: string, example: tenant }
 *       - in: query
 *         name: search
 *         schema: { type: string, example: "Nguyen" }
 *     responses:
 *       200:
 *         description: Danh sách người dùng
 *       401:
 *         description: Chưa xác thực
 *       403:
 *         description: Không đủ quyền
 */
router.post('/', authenticate, authorize('admin'), validateUserRegistration, async (req, res) => {
  try {
    const { fullName, email, password, phone, role = 'tenant', address } = req.body;

    const existingUser = await User.findOne({ email });
    if (existingUser) {
      return res.status(400).json({
        status: 'error',
        message: 'Email đã được sử dụng'
      });
    }

    const user = new User({
      fullName,
      email,
      password,
      phone,
      role,
      address,
      isActive: true,
      isVerified: true
    });

    await user.save();

    res.status(201).json({
      status: 'success',
      message: 'Tạo người dùng thành công',
      data: { user: user.getPublicProfile() }
    });
  } catch (error) {
    console.error('Create user error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi tạo người dùng'
    });
  }
});

router.get('/', authenticate, authorize('admin'), async (req, res) => {
  try {
    const {
      page = 1,
      limit = 20,
      role,
      isActive,
      search,
      sortBy = 'createdAt',
      sortOrder = 'desc'
    } = req.query;

    const query = {};
    
    if (role) query.role = role;
    if (isActive !== undefined) query.isActive = isActive === 'true';
    
    if (search) {
      query.$or = [
        { fullName: { $regex: search, $options: 'i' } },
        { email: { $regex: search, $options: 'i' } },
        { phone: { $regex: search, $options: 'i' } }
      ];
    }

    const skip = (page - 1) * limit;
    const sort = { [sortBy]: sortOrder === 'desc' ? -1 : 1 };

    const users = await User.find(query)
      .select('-password -verificationToken -resetPasswordToken -resetPasswordExpires')
      .sort(sort)
      .skip(skip)
      .limit(parseInt(limit));

    const total = await User.countDocuments(query);

    res.json({
      status: 'success',
      data: {
        users,
        pagination: {
          currentPage: parseInt(page),
          totalPages: Math.ceil(total / limit),
          totalUsers: total,
          hasNext: page < Math.ceil(total / limit),
          hasPrev: page > 1
        }
      }
    });
  } catch (error) {
    console.error('Get users error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy danh sách người dùng'
    });
  }
});

/**
 * @swagger
 * /api/users/{id}:
 *   get:
 *     tags: [Users]
 *     summary: Lấy thông tin người dùng
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
router.get('/:id', authenticate, validateObjectId('id'), async (req, res) => {
  try {
    const user = await User.findById(req.params.id)
      .select('-password -verificationToken -resetPasswordToken -resetPasswordExpires');

    if (!user) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy người dùng'
      });
    }

    if (req.user.role !== 'admin' && req.user._id.toString() !== req.params.id) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền xem thông tin người dùng này'
      });
    }

    res.json({
      status: 'success',
      data: { user }
    });
  } catch (error) {
    console.error('Get user error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thông tin người dùng'
    });
  }
});

/**
 * @swagger
 * /api/users/{id}:
 *   put:
 *     tags: [Users]
 *     summary: Cập nhật người dùng
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
 *             properties:
 *               fullName: { type: string }
 *               phone: { type: string }
 *               address: { type: object }
 *     responses:
 *       200: { description: Cập nhật thành công }
 *       401: { description: Chưa xác thực }
 *       403: { description: Không đủ quyền }
 *       404: { description: Không tìm thấy }
 */
router.put('/:id', authenticate, validateUserUpdate, validateObjectId('id'), async (req, res) => {
  try {
    const userId = req.params.id;
    
    if (req.user.role !== 'admin' && req.user._id.toString() !== userId) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền cập nhật thông tin người dùng này'
      });
    }

    const user = await User.findById(userId);

    if (!user) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy người dùng'
      });
    }

    const allowedFields = [
      'fullName', 'phone', 'address', 'landlordInfo', 'tenantInfo'
    ];

    if (req.user.role === 'admin') {
      allowedFields.push('role', 'isActive', 'isVerified');
    }

    allowedFields.forEach(field => {
      if (req.body[field] !== undefined) {
        user[field] = req.body[field];
      }
    });

    await user.save();

    res.json({
      status: 'success',
      message: 'Cập nhật thông tin thành công',
      data: { user: user.getPublicProfile() }
    });
  } catch (error) {
    console.error('Update user error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi cập nhật thông tin người dùng'
    });
  }
});

router.post('/:id/avatar', authenticate, upload.single('avatar'), async (req, res) => {
  try {
    const userId = req.params.id;
    
    if (req.user.role !== 'admin' && req.user._id.toString() !== userId) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền cập nhật avatar của người dùng này'
      });
    }

    if (!req.file) {
      return res.status(400).json({
        status: 'error',
        message: 'Vui lòng chọn file hình ảnh'
      });
    }

    const user = await User.findById(userId);

    if (!user) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy người dùng'
      });
    }

    user.avatar = `/uploads/avatars/${req.file.filename}`;
    await user.save();

    res.json({
      status: 'success',
      message: 'Cập nhật avatar thành công',
      data: { 
        user: user.getPublicProfile(),
        avatarUrl: user.avatar
      }
    });
  } catch (error) {
    console.error('Upload avatar error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi cập nhật avatar'
    });
  }
});

router.delete('/:id', authenticate, authorize('admin'), validateObjectId('id'), async (req, res) => {
  try {
    const user = await User.findById(req.params.id);

    if (!user) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy người dùng'
      });
    }

    if (user._id.toString() === req.user._id.toString()) {
      return res.status(400).json({
        status: 'error',
        message: 'Không thể xóa tài khoản của chính mình'
      });
    }

    await User.findByIdAndDelete(req.params.id);

    res.json({
      status: 'success',
      message: 'Xóa người dùng thành công'
    });
  } catch (error) {
    console.error('Delete user error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi xóa người dùng'
    });
  }
});

router.put('/:id/status', authenticate, authorize('admin'), validateObjectId('id'), async (req, res) => {
  try {
    const user = await User.findById(req.params.id);

    if (!user) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy người dùng'
      });
    }

    if (user._id.toString() === req.user._id.toString()) {
      return res.status(400).json({
        status: 'error',
        message: 'Không thể vô hiệu hóa tài khoản của chính mình'
      });
    }

    user.isActive = !user.isActive;
    await user.save();

    res.json({
      status: 'success',
      message: `${user.isActive ? 'Kích hoạt' : 'Vô hiệu hóa'} người dùng thành công`,
      data: { user: user.getPublicProfile() }
    });
  } catch (error) {
    console.error('Toggle user status error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi thay đổi trạng thái người dùng'
    });
  }
});

router.get('/:id/bookings', authenticate, validateObjectId('id'), async (req, res) => {
  try {
    const userId = req.params.id;
    
    if (req.user.role !== 'admin' && req.user._id.toString() !== userId) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền xem booking của người dùng này'
      });
    }

    const {
      page = 1,
      limit = 10,
      status,
      sortBy = 'createdAt',
      sortOrder = 'desc'
    } = req.query;

    const query = {
      $or: [
        { tenant: userId },
        { landlord: userId }
      ]
    };

    if (status) query.status = status;

    const skip = (page - 1) * limit;
    const sort = { [sortBy]: sortOrder === 'desc' ? -1 : 1 };

    const Booking = require('../models/Booking');
    const bookings = await Booking.find(query)
      .populate('room', 'title images address')
      .populate('tenant', 'fullName email phone')
      .populate('landlord', 'fullName email phone')
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
    console.error('Get user bookings error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy danh sách booking'
    });
  }
});

router.get('/:id/rooms', authenticate, validateObjectId('id'), async (req, res) => {
  try {
    const userId = req.params.id;
    
    if (req.user.role !== 'admin' && req.user._id.toString() !== userId) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền xem phòng của người dùng này'
      });
    }

    const {
      page = 1,
      limit = 10,
      status,
      sortBy = 'createdAt',
      sortOrder = 'desc'
    } = req.query;

    const query = { landlord: userId };
    if (status) query.status = status;

    const skip = (page - 1) * limit;
    const sort = { [sortBy]: sortOrder === 'desc' ? -1 : 1 };

    const Room = require('../models/Room');
    const rooms = await Room.find(query)
      .populate('landlord', 'fullName email phone')
      .sort(sort)
      .skip(skip)
      .limit(parseInt(limit));

    const total = await Room.countDocuments(query);

    res.json({
      status: 'success',
      data: {
        rooms,
        pagination: {
          currentPage: parseInt(page),
          totalPages: Math.ceil(total / limit),
          totalRooms: total,
          hasNext: page < Math.ceil(total / limit),
          hasPrev: page > 1
        }
      }
    });
  } catch (error) {
    console.error('Get user rooms error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy danh sách phòng'
    });
  }
});

router.get('/stats/overview', authenticate, authorize('admin'), async (req, res) => {
  try {
    const stats = await User.aggregate([
      {
        $group: {
          _id: '$role',
          count: { $sum: 1 },
          activeCount: {
            $sum: { $cond: [{ $eq: ['$isActive', true] }, 1, 0] }
          },
          verifiedCount: {
            $sum: { $cond: [{ $eq: ['$isVerified', true] }, 1, 0] }
          }
        }
      }
    ]);

    const totalUsers = await User.countDocuments();
    const totalActiveUsers = await User.countDocuments({ isActive: true });
    const totalVerifiedUsers = await User.countDocuments({ isVerified: true });

    res.json({
      status: 'success',
      data: {
        totalUsers,
        totalActiveUsers,
        totalVerifiedUsers,
        byRole: stats
      }
    });
  } catch (error) {
    console.error('Get user stats error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thống kê người dùng'
    });
  }
});

module.exports = router;
