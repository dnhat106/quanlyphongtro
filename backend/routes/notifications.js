const express = require('express');
const Notification = require('../models/Notification');
const { authenticate, authorize } = require('../middleware/auth');
const { validateNotification, validateObjectId } = require('../middleware/validation');

const router = express.Router();

/**
 * @swagger
 * /api/notifications:
 *   get:
 *     tags: [Notifications]
 *     summary: Danh sách thông báo của người dùng
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: query
 *         name: page
 *         schema: { type: integer, example: 1 }
 *       - in: query
 *         name: limit
 *         schema: { type: integer, example: 20 }
 *     responses:
 *       200: { description: Thành công }
 *       401: { description: Chưa xác thực }
 */
router.get('/', authenticate, async (req, res) => {
  try {
    const {
      page = 1,
      limit = 20,
      status,
      type,
      priority,
      sortBy = 'createdAt',
      sortOrder = 'desc'
    } = req.query;

    const options = {
      page: parseInt(page),
      limit: parseInt(limit),
      status,
      type,
      priority,
      sortBy,
      sortOrder
    };

    const result = await Notification.getUserNotifications(req.user._id, options);

    res.json({
      status: 'success',
      data: {
        notifications: result,
        pagination: {
          currentPage: parseInt(page),
          totalPages: Math.ceil(result.length / limit),
          hasNext: page < Math.ceil(result.length / limit),
          hasPrev: page > 1
        }
      }
    });
  } catch (error) {
    console.error('Get notifications error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy danh sách thông báo'
    });
  }
});

router.get('/unread-count', authenticate, async (req, res) => {
  try {
    const count = await Notification.getUnreadCount(req.user._id);

    res.json({
      status: 'success',
      data: { unreadCount: count }
    });
  } catch (error) {
    console.error('Get unread count error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy số thông báo chưa đọc'
    });
  }
});

router.get('/:id', authenticate, validateObjectId('id'), async (req, res) => {
  try {
    const notification = await Notification.findById(req.params.id)
      .populate('sender', 'fullName avatar');

    if (!notification) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy thông báo'
      });
    }

    if (notification.recipient.toString() !== req.user._id.toString()) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền xem thông báo này'
      });
    }

    res.json({
      status: 'success',
      data: { notification }
    });
  } catch (error) {
    console.error('Get notification error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thông tin thông báo'
    });
  }
});

router.put('/:id/read', authenticate, validateObjectId('id'), async (req, res) => {
  try {
    const notification = await Notification.findById(req.params.id);

    if (!notification) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy thông báo'
      });
    }

    if (notification.recipient.toString() !== req.user._id.toString()) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền đánh dấu thông báo này'
      });
    }

    await notification.markAsRead();

    res.json({
      status: 'success',
      message: 'Đánh dấu thông báo đã đọc thành công',
      data: { notification }
    });
  } catch (error) {
    console.error('Mark notification as read error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi đánh dấu thông báo'
    });
  }
});

router.put('/:id/archive', authenticate, validateObjectId('id'), async (req, res) => {
  try {
    const notification = await Notification.findById(req.params.id);

    if (!notification) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy thông báo'
      });
    }

    if (notification.recipient.toString() !== req.user._id.toString()) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền lưu trữ thông báo này'
      });
    }

    await notification.archive();

    res.json({
      status: 'success',
      message: 'Lưu trữ thông báo thành công',
      data: { notification }
    });
  } catch (error) {
    console.error('Archive notification error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lưu trữ thông báo'
    });
  }
});

router.put('/mark-all-read', authenticate, async (req, res) => {
  try {
    const result = await Notification.markAllAsRead(req.user._id);

    res.json({
      status: 'success',
      message: 'Đánh dấu tất cả thông báo đã đọc thành công',
      data: { 
        modifiedCount: result.modifiedCount 
      }
    });
  } catch (error) {
    console.error('Mark all notifications as read error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi đánh dấu tất cả thông báo'
    });
  }
});

router.post('/', authenticate, authorize('admin'), validateNotification, async (req, res) => {
  try {
    const notification = new Notification({
      ...req.body,
      sender: req.user._id
    });

    await notification.save();

    await notification.populate('recipient', 'fullName email');

    res.status(201).json({
      status: 'success',
      message: 'Tạo thông báo thành công',
      data: { notification }
    });
  } catch (error) {
    console.error('Create notification error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi tạo thông báo'
    });
  }
});

router.post('/bulk', authenticate, authorize('admin'), async (req, res) => {
  try {
    const { recipients, type, title, message, data, priority = 'medium' } = req.body;

    if (!recipients || !Array.isArray(recipients) || recipients.length === 0) {
      return res.status(400).json({
        status: 'error',
        message: 'Danh sách người nhận là bắt buộc'
      });
    }

    if (!type || !title || !message) {
      return res.status(400).json({
        status: 'error',
        message: 'Loại, tiêu đề và nội dung thông báo là bắt buộc'
      });
    }

    const notifications = recipients.map(recipientId => ({
      recipient: recipientId,
      sender: req.user._id,
      type,
      title,
      message,
      data: data || {},
      priority,
      channels: ['in_app']
    }));

    const result = await Notification.sendBulk(notifications);

    const successCount = result.filter(r => r.success).length;
    const failureCount = result.filter(r => !r.success).length;

    res.json({
      status: 'success',
      message: `Gửi thông báo hàng loạt thành công: ${successCount} thành công, ${failureCount} thất bại`,
      data: { 
        total: recipients.length,
        success: successCount,
        failed: failureCount,
        results: result
      }
    });
  } catch (error) {
    console.error('Send bulk notifications error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi gửi thông báo hàng loạt'
    });
  }
});

router.get('/stats/overview', authenticate, async (req, res) => {
  try {
    const stats = await Notification.aggregate([
      {
        $match: { recipient: req.user._id }
      },
      {
        $group: {
          _id: null,
          total: { $sum: 1 },
          unread: {
            $sum: { $cond: [{ $eq: ['$status', 'unread'] }, 1, 0] }
          },
          read: {
            $sum: { $cond: [{ $eq: ['$status', 'read'] }, 1, 0] }
          },
          archived: {
            $sum: { $cond: [{ $eq: ['$status', 'archived'] }, 1, 0] }
          }
        }
      }
    ]);

    const typeStats = await Notification.aggregate([
      {
        $match: { recipient: req.user._id }
      },
      {
        $group: {
          _id: '$type',
          count: { $sum: 1 },
          unread: {
            $sum: { $cond: [{ $eq: ['$status', 'unread'] }, 1, 0] }
          }
        }
      },
      { $sort: { count: -1 } }
    ]);

    const priorityStats = await Notification.aggregate([
      {
        $match: { recipient: req.user._id }
      },
      {
        $group: {
          _id: '$priority',
          count: { $sum: 1 },
          unread: {
            $sum: { $cond: [{ $eq: ['$status', 'unread'] }, 1, 0] }
          }
        }
      }
    ]);

    res.json({
      status: 'success',
      data: {
        overview: stats[0] || {
          total: 0,
          unread: 0,
          read: 0,
          archived: 0
        },
        byType: typeStats,
        byPriority: priorityStats
      }
    });
  } catch (error) {
    console.error('Get notification stats error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thống kê thông báo'
    });
  }
});

router.delete('/:id', authenticate, validateObjectId('id'), async (req, res) => {
  try {
    const notification = await Notification.findById(req.params.id);

    if (!notification) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy thông báo'
      });
    }

    if (notification.recipient.toString() !== req.user._id.toString()) {
      return res.status(403).json({
        status: 'error',
        message: 'Không có quyền xóa thông báo này'
      });
    }

    await Notification.findByIdAndDelete(req.params.id);

    res.json({
      status: 'success',
      message: 'Xóa thông báo thành công'
    });
  } catch (error) {
    console.error('Delete notification error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi xóa thông báo'
    });
  }
});

router.delete('/cleanup', authenticate, authorize('admin'), async (req, res) => {
  try {
    const result = await Notification.cleanupExpired();

    res.json({
      status: 'success',
      message: 'Dọn dẹp thông báo hết hạn thành công',
      data: { 
        modifiedCount: result.modifiedCount 
      }
    });
  } catch (error) {
    console.error('Cleanup notifications error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi dọn dẹp thông báo'
    });
  }
});

module.exports = router;
