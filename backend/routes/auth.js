const express = require('express');
const bcrypt = require('bcryptjs');
const crypto = require('crypto');
const User = require('../models/User');
const { generateToken, authenticate } = require('../middleware/auth');
const { validateUserRegistration, validateUserLogin } = require('../middleware/validation');
const { sendEmail } = require('../utils/emailService');

const router = express.Router();

/**
 * @swagger
 * /api/auth/register:
 *   post:
 *     tags: [Auth]
 *     summary: Đăng ký tài khoản
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [fullName, email, password, phone]
 *             properties:
 *               fullName:
 *                 type: string
 *                 example: Nguyen Van A
 *               email:
 *                 type: string
 *                 example: user@example.com
 *               password:
 *                 type: string
 *                 example: Abc12345
 *               phone:
 *                 type: string
 *                 example: "0912345678"
 *               role:
 *                 type: string
 *                 enum: [admin, landlord, tenant]
 *                 example: tenant
 *     responses:
 *       201:
 *         description: Đăng ký thành công
 *       400:
 *         description: Dữ liệu không hợp lệ/đã tồn tại
 */
router.post('/register', validateUserRegistration, async (req, res) => {
  try {
    const { fullName, email, password, phone, role = 'tenant' } = req.body;

    const existingUser = await User.findOne({
      $or: [{ email }, { phone }]
    });

    if (existingUser) {
      return res.status(400).json({
        status: 'error',
        message: existingUser.email === email 
          ? 'Email đã được sử dụng' 
          : 'Số điện thoại đã được sử dụng'
      });
    }

    const user = new User({
      fullName,
      email,
      password,
      phone,
      role,
      verificationToken: crypto.randomBytes(32).toString('hex')
    });

    await user.save();

    const token = generateToken(user._id);

    try {
      await sendEmail(user.email, 'welcome', {
        userName: user.fullName,
        userEmail: user.email,
        userRole: user.role
      });
    } catch (emailError) {
      console.error('Error sending welcome email:', emailError);
    }

    res.status(201).json({
      status: 'success',
      message: 'Đăng ký thành công',
      data: {
        user: user.getPublicProfile(),
        token
      }
    });
  } catch (error) {
    console.error('Registration error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi đăng ký'
    });
  }
});

/**
 * @swagger
 * /api/auth/login:
 *   post:
 *     tags: [Auth]
 *     summary: Đăng nhập
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [email, password]
 *             properties:
 *               email:
 *                 type: string
 *                 example: user@example.com
 *               password:
 *                 type: string
 *                 example: Abc12345
 *     responses:
 *       200:
 *         description: Đăng nhập thành công
 *       401:
 *         description: Sai thông tin đăng nhập
 */
router.post('/login', validateUserLogin, async (req, res) => {
  try {
    const { email, password } = req.body;

    const user = await User.findOne({ email }).select('+password');

    if (!user) {
      return res.status(401).json({
        status: 'error',
        message: 'Email hoặc mật khẩu không đúng'
      });
    }

    if (!user.isActive) {
      return res.status(401).json({
        status: 'error',
        message: 'Tài khoản đã bị vô hiệu hóa'
      });
    }

    const isPasswordValid = await user.comparePassword(password);

    if (!isPasswordValid) {
      return res.status(401).json({
        status: 'error',
        message: 'Email hoặc mật khẩu không đúng'
      });
    }

    user.lastLogin = new Date();
    user.loginCount += 1;
    await user.save();
    const token = generateToken(user._id);

    res.json({
      status: 'success',
      message: 'Đăng nhập thành công',
      data: {
        user: user.getPublicProfile(),
        token
      }
    });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi đăng nhập'
    });
  }
});

/**
 * @swagger
 * /api/auth/me:
 *   get:
 *     tags: [Auth]
 *     summary: Lấy thông tin người dùng hiện tại
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Thông tin người dùng
 *       401:
 *         description: Chưa xác thực
 */
router.get('/me', authenticate, async (req, res) => {
  try {
    res.json({
      status: 'success',
      data: {
        user: req.user.getPublicProfile()
      }
    });
  } catch (error) {
    console.error('Get current user error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thông tin người dùng'
    });
  }
});

/**
 * @swagger
 * /api/auth/forgot-password:
 *   post:
 *     tags: [Auth]
 *     summary: Yêu cầu đặt lại mật khẩu
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [email]
 *             properties:
 *               email:
 *                 type: string
 *                 example: user@example.com
 *     responses:
 *       200:
 *         description: Email đặt lại mật khẩu đã được gửi
 *       404:
 *         description: Không tìm thấy người dùng
 */
router.post('/forgot-password', async (req, res) => {
  try {
    const { email } = req.body;

    if (!email) {
      return res.status(400).json({
        status: 'error',
        message: 'Email là bắt buộc'
      });
    }

    const user = await User.findOne({ email });

    if (!user) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy người dùng với email này'
      });
    }

    const resetToken = crypto.randomBytes(32).toString('hex');
    user.resetPasswordToken = resetToken;
    user.resetPasswordExpires = Date.now() + 3600000; 
    await user.save();

    const resetUrl = `${req.protocol}://${req.get('host')}/api/auth/reset-password/${resetToken}`;
    
    try {
      await sendEmail(user.email, 'passwordReset', {
        userName: user.fullName,
        resetUrl
      });
    } catch (emailError) {
      console.error('Error sending reset email:', emailError);
      return res.status(500).json({
        status: 'error',
        message: 'Lỗi khi gửi email đặt lại mật khẩu'
      });
    }

    res.json({
      status: 'success',
      message: 'Email đặt lại mật khẩu đã được gửi'
    });
  } catch (error) {
    console.error('Forgot password error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi xử lý yêu cầu đặt lại mật khẩu'
    });
  }
});

/**
 * @swagger
 * /api/auth/reset-password/{token}:
 *   post:
 *     tags: [Auth]
 *     summary: Đặt lại mật khẩu
 *     parameters:
 *       - in: path
 *         name: token
 *         required: true
 *         schema:
 *           type: string
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [password]
 *             properties:
 *               password:
 *                 type: string
 *                 example: NewPass123
 *     responses:
 *       200:
 *         description: Đặt lại mật khẩu thành công
 *       400:
 *         description: Token không hợp lệ/hết hạn
 */
router.post('/reset-password/:token', async (req, res) => {
  try {
    const { token } = req.params;
    const { password } = req.body;

    if (!password || password.length < 6) {
      return res.status(400).json({
        status: 'error',
        message: 'Mật khẩu phải có ít nhất 6 ký tự'
      });
    }

    const user = await User.findOne({
      resetPasswordToken: token,
      resetPasswordExpires: { $gt: Date.now() }
    });

    if (!user) {
      return res.status(400).json({
        status: 'error',
        message: 'Token không hợp lệ hoặc đã hết hạn'
      });
    }

    user.password = password;
    user.resetPasswordToken = undefined;
    user.resetPasswordExpires = undefined;
    await user.save();

    res.json({
      status: 'success',
      message: 'Mật khẩu đã được đặt lại thành công'
    });
  } catch (error) {
    console.error('Reset password error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi đặt lại mật khẩu'
    });
  }
});

/**
 * @swagger
 * /api/auth/change-password:
 *   post:
 *     tags: [Auth]
 *     summary: Đổi mật khẩu
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [currentPassword, newPassword]
 *             properties:
 *               currentPassword:
 *                 type: string
 *                 example: Abc12345
 *               newPassword:
 *                 type: string
 *                 example: NewPass123
 *     responses:
 *       200:
 *         description: Đổi mật khẩu thành công
 *       400:
 *         description: Mật khẩu hiện tại sai/không hợp lệ
 *       401:
 *         description: Chưa xác thực
 */
router.post('/change-password', authenticate, async (req, res) => {
  try {
    const { currentPassword, newPassword } = req.body;

    if (!currentPassword || !newPassword) {
      return res.status(400).json({
        status: 'error',
        message: 'Mật khẩu hiện tại và mật khẩu mới là bắt buộc'
      });
    }

    if (newPassword.length < 6) {
      return res.status(400).json({
        status: 'error',
        message: 'Mật khẩu mới phải có ít nhất 6 ký tự'
      });
    }
    const user = await User.findById(req.user._id).select('+password');


    const isCurrentPasswordValid = await user.comparePassword(currentPassword);

    if (!isCurrentPasswordValid) {
      return res.status(400).json({
        status: 'error',
        message: 'Mật khẩu hiện tại không đúng'
      });
    }

    user.password = newPassword;
    await user.save();

    res.json({
      status: 'success',
      message: 'Mật khẩu đã được thay đổi thành công'
    });
  } catch (error) {
    console.error('Change password error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi thay đổi mật khẩu'
    });
  }
});

// @route   POST /api/auth/verify-email/:token
// @desc    Verify email
// @access  Public
router.post('/verify-email/:token', async (req, res) => {
  try {
    const { token } = req.params;

    const user = await User.findOne({ verificationToken: token });

    if (!user) {
      return res.status(400).json({
        status: 'error',
        message: 'Token xác thực không hợp lệ'
      });
    }

    user.isVerified = true;
    user.verificationToken = undefined;
    await user.save();

    res.json({
      status: 'success',
      message: 'Email đã được xác thực thành công'
    });
  } catch (error) {
    console.error('Email verification error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi xác thực email'
    });
  }
});

// @route   POST /api/auth/logout
// @desc    Logout user (client-side token removal)
// @access  Private
router.post('/logout', authenticate, (req, res) => {
  res.json({
    status: 'success',
    message: 'Đăng xuất thành công'
  });
});

module.exports = router;
