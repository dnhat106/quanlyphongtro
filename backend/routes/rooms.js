const express = require('express');
const multer = require('multer');
const path = require('path');
const Room = require('../models/Room');
const { authenticate, authorize, checkRoomAccess } = require('../middleware/auth');
const { validateRoomCreation, validateRoomUpdate, validateObjectId, validateSearch } = require('../middleware/validation');

const router = express.Router();

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const uploadPath = path.join(__dirname, '..', 'uploads', 'rooms');
    
    const fs = require('fs');
    if (!fs.existsSync(uploadPath)) {
      fs.mkdirSync(uploadPath, { recursive: true });
    }
    
    cb(null, uploadPath);
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    const filename = 'room-' + uniqueSuffix + path.extname(file.originalname);
    cb(null, filename);
  }
});

const upload = multer({
  storage: storage,
  limits: {
    fileSize: 10 * 1024 * 1024 
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
 * /api/rooms:
 *   get:
 *     tags: [Rooms]
 *     summary: Danh sách phòng (tìm kiếm & lọc)
 *     parameters:
 *       - in: query
 *         name: page
 *         schema: { type: integer, example: 1 }
 *       - in: query
 *         name: limit
 *         schema: { type: integer, example: 12 }
 *       - in: query
 *         name: city
 *         schema: { type: string, example: "Hà Nội" }
 *       - in: query
 *         name: minPrice
 *         schema: { type: number, example: 2000000 }
 *       - in: query
 *         name: maxPrice
 *         schema: { type: number, example: 7000000 }
 *     responses:
 *       200: { description: Thành công }
 */
router.get('/', validateSearch, async (req, res) => {
  try {
    const {
      page = 1,
      limit = 12,
      city,
      district,
      ward,
      minPrice,
      maxPrice,
      roomType,
      minArea,
      maxArea,
      amenities,
      status = 'active',
      sortBy = 'createdAt',
      sortOrder = 'desc',
      search,
      lat,
      lng,
      radius = 5000,
      excludeBooked = false
    } = req.query;

    const query = { status };

    const createAccentInsensitiveRegex = (str) => {
      if (!str) return null;
      const normalized = str.toLowerCase();
      let pattern = '';
      for (let i = 0; i < normalized.length; i++) {
        const char = normalized[i];
        switch (char) {
          case 'a':
            pattern += '[aàáảãạăằắẳẵặâầấẩẫậ]';
            break;
          case 'e':
            pattern += '[eèéẻẽẹêềếểễệ]';
            break;
          case 'i':
            pattern += '[iìíỉĩị]';
            break;
          case 'o':
            pattern += '[oòóỏõọôồốổỗộơờớởỡợ]';
            break;
          case 'u':
            pattern += '[uùúủũụưừứửữự]';
            break;
          case 'y':
            pattern += '[yỳýỷỹỵ]';
            break;
          case 'd':
            pattern += '[dđ]';
            break;
          default:
            if (/[.*+?^${}()|[\]\\]/.test(char)) {
              pattern += '\\' + char;
            } else {
              pattern += char;
            }
            break;
        }
      }
      return new RegExp(pattern, 'i');
    };

    if (city) {
      const cityRegex = createAccentInsensitiveRegex(city.trim());
      if (cityRegex) {
        query['address.city'] = cityRegex;
      }
    }
    if (district) {
      const districtRegex = createAccentInsensitiveRegex(district.trim());
      if (districtRegex) {
        query['address.district'] = districtRegex;
      }
    }
    if (ward) {
      const wardRegex = createAccentInsensitiveRegex(ward.trim());
      if (wardRegex) {
        query['address.ward'] = wardRegex;
      }
    }

    if (minPrice || maxPrice) {
      query['price.monthly'] = {};
      if (minPrice) query['price.monthly'].$gte = parseFloat(minPrice);
      if (maxPrice) query['price.monthly'].$lte = parseFloat(maxPrice);
    }

    if (minArea || maxArea) {
      query.area = {};
      if (minArea) query.area.$gte = parseFloat(minArea);
      if (maxArea) query.area.$lte = parseFloat(maxArea);
    }

    if (roomType) query.roomType = roomType;

    if (amenities) {
      const amenityArray = Array.isArray(amenities) ? amenities : amenities.split(',');
      query.amenities = { $all: amenityArray };
    }

    if (search) {
      query.$or = [
        { title: new RegExp(search, 'i') },
        { description: new RegExp(search, 'i') },
        { 'address.street': new RegExp(search, 'i') }
      ];
    }

    if (lat && lng) {
      query['address.coordinates'] = {
        $near: {
          $geometry: {
            type: 'Point',
            coordinates: [parseFloat(lng), parseFloat(lat)]
          },
          $maxDistance: parseInt(radius)
        }
      };
    }

    const skip = (page - 1) * limit;
    const sort = { [sortBy]: sortOrder === 'desc' ? -1 : 1 };

    let rooms;
    
    if (excludeBooked === 'true') {
      const Booking = require('../models/Booking');
      const roomsWithActiveOrNonAdminCancelledBookings = await Booking.distinct('room', {
        $or: [
          { status: { $in: ['pending', 'confirmed', 'deposit_paid', 'active'] } },
          { status: 'cancelled', 'cancellation.cancelledBy': { $ne: 'admin' } }
        ]
      });

      // Chỉ hiển thị các phòng KHÔNG nằm trong danh sách trên
      query._id = { $nin: roomsWithActiveOrNonAdminCancelledBookings };
    }

    rooms = await Room.find(query)
      .populate('landlord', 'fullName phone email avatar')
      .sort(sort)
      .skip(skip)
      .limit(parseInt(limit));

    const total = await Room.countDocuments(query);

    if (rooms.length > 0) {
      rooms.forEach((room, index) => {
        console.log(`Room ${index + 1}:`, {
          title: room.title,
          city: room.address?.city,
          district: room.address?.district,
          ward: room.address?.ward
        });
      });
    }

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
    console.error('Get rooms error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy danh sách phòng'
    });
  }
});

router.get('/featured', async (req, res) => {
  try {
    const { limit = 8 } = req.query;

    const rooms = await Room.find({ 
      status: 'active',
      'rating.average': { $gte: 4 }
    })
      .populate('landlord', 'fullName phone email avatar')
      .sort({ 'rating.average': -1, views: -1 })
      .limit(parseInt(limit));

    res.json({
      status: 'success',
      data: { rooms }
    });
  } catch (error) {
    console.error('Get featured rooms error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy phòng nổi bật'
    });
  }
});

/**
 * @swagger
 * /api/rooms/{id}:
 *   get:
 *     tags: [Rooms]
 *     summary: Chi tiết phòng
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200: { description: Thành công }
 *       404: { description: Không tìm thấy }
 */
router.get('/:id', validateObjectId('id'), async (req, res) => {
  try {
    const room = await Room.findById(req.params.id)
      .populate('landlord', 'fullName phone email avatar address landlordInfo');

    if (!room) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy phòng'
      });
    }

    await room.incrementViews();

    res.json({
      status: 'success',
      data: { room }
    });
  } catch (error) {
    console.error('Get room error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thông tin phòng'
    });
  }
});

/**
 * @swagger
 * /api/rooms:
 *   post:
 *     tags: [Rooms]
 *     summary: Tạo phòng (Chủ trọ/Admin)
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/Room'
 *     responses:
 *       201: { description: Tạo thành công }
 *       401: { description: Chưa xác thực }
 *       403: { description: Không đủ quyền }
 */
router.post('/', authenticate, authorize('landlord', 'admin'), validateRoomCreation, async (req, res) => {
  try {
    
    const roomData = {
      ...req.body,
      landlord: req.user._id
    };

    // Handle utilities - convert from object to number if needed
    if (roomData.price && roomData.price.utilities && typeof roomData.price.utilities === 'object') {
      const utilities = roomData.price.utilities;
      const totalUtilities = (utilities.electricity || 0) + (utilities.water || 0) + (utilities.internet || 0) + (utilities.other || 0);
      roomData.price.utilities = totalUtilities;
    }


    const room = new Room(roomData);
    await room.save();

    await room.populate('landlord', 'fullName phone email avatar');


    res.status(201).json({
      status: 'success',
      message: 'Tạo phòng thành công',
      data: { room }
    });
  } catch (error) {
    console.error('=== CREATE ROOM ERROR ===');
    console.error('Error details:', error);
    console.error('Error message:', error.message);
    console.error('Error stack:', error.stack);
    
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi tạo phòng: ' + error.message
    });
  }
});

router.put('/:id', authenticate, checkRoomAccess, validateRoomUpdate, validateObjectId('id'), async (req, res) => {
  try {
    const room = await Room.findById(req.params.id);

    if (!room) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy phòng'
      });
    }

    const allowedFields = [
      'title', 'description', 'address', 'roomType', 'area',
      'price', 'amenities', 'rules', 'availability', 'status',
      'contactInfo', 'nearbyPlaces'
    ];

    allowedFields.forEach(field => {
      if (req.body[field] !== undefined) {
        room[field] = req.body[field];
      }
    });

    await room.save();
    await room.populate('landlord', 'fullName phone email avatar');

    res.json({
      status: 'success',
      message: 'Cập nhật phòng thành công',
      data: { room }
    });
  } catch (error) {
    console.error('Update room error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi cập nhật phòng'
    });
  }
});

router.post('/:id/images', authenticate, checkRoomAccess, upload.array('images', 10), async (req, res) => {
  try {
    
    const room = await Room.findById(req.params.id);

    if (!room) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy phòng'
      });
    }

    if (!req.files || req.files.length === 0) {
      return res.status(400).json({
        status: 'error',
        message: 'Vui lòng chọn ít nhất một hình ảnh'
      });
    }

    const newImages = req.files.map((file, index) => ({
      url: `/uploads/rooms/${file.filename}`,
      caption: req.body.captions ? req.body.captions[index] : '',
      isPrimary: index === 0 && room.images.length === 0
    }));


    room.images.push(...newImages);
    await room.save();


    res.json({
      status: 'success',
      message: 'Upload hình ảnh thành công',
      data: { 
        room,
        newImages: newImages.length
      }
    });
  } catch (error) {
    console.error('Upload room images error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi upload hình ảnh'
    });
  }
});

router.delete('/:id/images/:imageId', authenticate, checkRoomAccess, async (req, res) => {
  try {
    const room = await Room.findById(req.params.id);

    if (!room) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy phòng'
      });
    }

    const imageIndex = room.images.findIndex(img => img._id.toString() === req.params.imageId);

    if (imageIndex === -1) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy hình ảnh'
      });
    }

    room.images.splice(imageIndex, 1);

    if (room.images.length > 0 && !room.images.some(img => img.isPrimary)) {
      room.images[0].isPrimary = true;
    }

    await room.save();

    res.json({
      status: 'success',
      message: 'Xóa hình ảnh thành công',
      data: { room }
    });
  } catch (error) {
    console.error('Delete room image error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi xóa hình ảnh'
    });
  }
});

router.post('/:id/like', authenticate, validateObjectId('id'), async (req, res) => {
  try {
    const room = await Room.findById(req.params.id);

    if (!room) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy phòng'
      });
    }

    await room.toggleLike(req.user._id);

    res.json({
      status: 'success',
      message: 'Cập nhật yêu thích thành công',
      data: { 
        isLiked: room.likes.includes(req.user._id),
        likesCount: room.likes.length
      }
    });
  } catch (error) {
    console.error('Toggle room like error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi cập nhật yêu thích'
    });
  }
});

router.delete('/:id', authenticate, checkRoomAccess, validateObjectId('id'), async (req, res) => {
  try {
    const room = await Room.findById(req.params.id);

    if (!room) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy phòng'
      });
    }

    const Booking = require('../models/Booking');
    const activeBookings = await Booking.countDocuments({
      room: room._id,
      status: { $in: ['confirmed', 'deposit_paid', 'active'] }
    });

    if (activeBookings > 0) {
      return res.status(400).json({
        status: 'error',
        message: 'Không thể xóa phòng đang có booking hoạt động'
      });
    }

    await Room.findByIdAndDelete(req.params.id);

    res.json({
      status: 'success',
      message: 'Xóa phòng thành công'
    });
  } catch (error) {
    console.error('Delete room error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi xóa phòng'
    });
  }
});

router.get('/:id/similar', validateObjectId('id'), async (req, res) => {
  try {
    const room = await Room.findById(req.params.id);

    if (!room) {
      return res.status(404).json({
        status: 'error',
        message: 'Không tìm thấy phòng'
      });
    }

    const { limit = 6 } = req.query;

    const similarRooms = await Room.find({
      _id: { $ne: room._id },
      status: 'active',
      $or: [
        { roomType: room.roomType },
        { 'address.district': room.address.district },
        { 'address.city': room.address.city }
      ]
    })
      .populate('landlord', 'fullName phone email avatar')
      .sort({ 'rating.average': -1 })
      .limit(parseInt(limit));

    res.json({
      status: 'success',
      data: { rooms: similarRooms }
    });
  } catch (error) {
    console.error('Get similar rooms error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy phòng tương tự'
    });
  }
});

router.get('/stats/overview', async (req, res) => {
  try {
    const stats = await Room.aggregate([
      {
        $group: {
          _id: null,
          totalRooms: { $sum: 1 },
          activeRooms: {
            $sum: { $cond: [{ $eq: ['$status', 'active'] }, 1, 0] }
          },
          averagePrice: { $avg: '$price.monthly' },
          averageRating: { $avg: '$rating.average' },
          totalViews: { $sum: '$views' }
        }
      }
    ]);

    const roomTypeStats = await Room.aggregate([
      {
        $group: {
          _id: '$roomType',
          count: { $sum: 1 },
          averagePrice: { $avg: '$price.monthly' }
        }
      }
    ]);

    const cityStats = await Room.aggregate([
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
        overview: stats[0] || {
          totalRooms: 0,
          activeRooms: 0,
          averagePrice: 0,
          averageRating: 0,
          totalViews: 0
        },
        byRoomType: roomTypeStats,
        byCity: cityStats
      }
    });
  } catch (error) {
    console.error('Get room stats error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Lỗi server khi lấy thống kê phòng'
    });
  }
});

module.exports = router;
