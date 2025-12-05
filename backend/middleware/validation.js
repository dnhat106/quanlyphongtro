const { body, param, query, validationResult } = require('express-validator');

const handleValidationErrors = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    console.log('=== VALIDATION ERRORS ===');
    console.log('Request body:', JSON.stringify(req.body, null, 2));
    console.log('Validation errors:', errors.array());
    
    return res.status(400).json({
      status: 'error',
      message: 'Validation failed',
      errors: errors.array().map(error => ({
        field: error.path,
        message: error.msg,
        value: error.value
      }))
    });
  }
  next();
};


const validateUserRegistration = [
  body('fullName')
    .trim()
    .notEmpty()
    .withMessage('Họ tên là bắt buộc')
    .isLength({ max: 100 })
    .withMessage('Họ tên không được vượt quá 100 ký tự'),
  
  body('email')
    .isEmail()
    .withMessage('Email không hợp lệ')
    .normalizeEmail(),
  
  body('password')
    .isLength({ min: 6 })
    .withMessage('Mật khẩu phải có ít nhất 6 ký tự')
    .matches(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)
    .withMessage('Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 số'),
  
  body('phone')
    .matches(/^[0-9]{10,11}$/)
    .withMessage('Số điện thoại không hợp lệ'),
  
  body('role')
    .optional()
    .isIn(['admin', 'landlord', 'tenant'])
    .withMessage('Vai trò không hợp lệ'),
  
  handleValidationErrors
];

const validateUserLogin = [
  body('email')
    .isEmail()
    .withMessage('Email không hợp lệ')
    .normalizeEmail(),
  
  body('password')
    .notEmpty()
    .withMessage('Mật khẩu là bắt buộc'),
  
  handleValidationErrors
];

const validateUserUpdate = [
  body('fullName')
    .optional()
    .trim()
    .isLength({ max: 100 })
    .withMessage('Họ tên không được vượt quá 100 ký tự'),
  
  body('phone')
    .optional()
    .matches(/^[0-9]{10,11}$/)
    .withMessage('Số điện thoại không hợp lệ'),
  
  body('address.street')
    .optional()
    .trim()
    .isLength({ max: 200 })
    .withMessage('Địa chỉ đường không được vượt quá 200 ký tự'),
  
  body('address.ward')
    .optional()
    .trim()
    .isLength({ max: 100 })
    .withMessage('Phường/xã không được vượt quá 100 ký tự'),
  
  body('address.district')
    .optional()
    .trim()
    .isLength({ max: 100 })
    .withMessage('Quận/huyện không được vượt quá 100 ký tự'),
  
  body('address.city')
    .optional()
    .trim()
    .isLength({ max: 100 })
    .withMessage('Thành phố không được vượt quá 100 ký tự'),
  
  handleValidationErrors
];

const validateRoomCreation = [
  body('title')
    .trim()
    .notEmpty()
    .withMessage('Tiêu đề là bắt buộc')
    .isLength({ max: 200 })
    .withMessage('Tiêu đề không được vượt quá 200 ký tự'),
  
  body('description')
    .trim()
    .notEmpty()
    .withMessage('Mô tả là bắt buộc')
    .isLength({ max: 2000 })
    .withMessage('Mô tả không được vượt quá 2000 ký tự'),
  
  body('address.street')
    .trim()
    .notEmpty()
    .withMessage('Địa chỉ đường là bắt buộc'),
  
  body('address.ward')
    .trim()
    .notEmpty()
    .withMessage('Phường/xã là bắt buộc'),
  
  body('address.district')
    .trim()
    .notEmpty()
    .withMessage('Quận/huyện là bắt buộc'),
  
  body('address.city')
    .trim()
    .notEmpty()
    .withMessage('Thành phố là bắt buộc'),
  
  body('address.coordinates.lat')
    .optional()
    .isFloat({ min: -90, max: 90 })
    .withMessage('Vĩ độ không hợp lệ'),
  
  body('address.coordinates.lng')
    .optional()
    .isFloat({ min: -180, max: 180 })
    .withMessage('Kinh độ không hợp lệ'),
  
  body('roomType')
    .isIn(['studio', '1bedroom', '2bedroom', '3bedroom', 'shared'])
    .withMessage('Loại phòng không hợp lệ'),
  
  body('area')
    .isFloat({ min: 1 })
    .withMessage('Diện tích phải lớn hơn 0'),
  
  body('price.monthly')
    .isFloat({ min: 0 })
    .withMessage('Giá thuê hàng tháng không được âm'),
  
  body('price.deposit')
    .isFloat({ min: 0 })
    .withMessage('Tiền cọc không được âm'),
  
  body('price.utilities')
    .optional()
    .custom((value) => {
      if (typeof value === 'number') {
        return value >= 0;
      }
      if (typeof value === 'object' && value !== null) {
        const validKeys = ['electricity', 'water', 'internet', 'other'];
        const hasValidKeys = Object.keys(value).some(key => validKeys.includes(key));
        if (hasValidKeys) {
          return Object.values(value).every(val => typeof val === 'number' && val >= 0);
        }
        return false;
      }
      return false;
    })
    .withMessage('Phí tiện ích không hợp lệ'),
  
  body('amenities')
    .optional()
    .isArray()
    .withMessage('Tiện ích phải là mảng'),
  
  body('amenities.*')
    .optional()
    .isIn([
      'wifi', 'air_conditioner', 'refrigerator', 'washing_machine',
      'television', 'bed', 'wardrobe', 'desk', 'chair', 'fan',
      'hot_water', 'kitchen', 'bathroom', 'balcony', 'parking',
      'elevator', 'security', 'gym', 'swimming_pool', 'garden'
    ])
    .withMessage('Tiện ích không hợp lệ'),
  
  handleValidationErrors
];

const validateRoomUpdate = [
  body('title')
    .optional()
    .trim()
    .isLength({ max: 200 })
    .withMessage('Tiêu đề không được vượt quá 200 ký tự'),
  
  body('description')
    .optional()
    .trim()
    .isLength({ max: 2000 })
    .withMessage('Mô tả không được vượt quá 2000 ký tự'),
  
  body('price.monthly')
    .optional()
    .isFloat({ min: 0 })
    .withMessage('Giá thuê hàng tháng không được âm'),
  
  body('price.deposit')
    .optional()
    .isFloat({ min: 0 })
    .withMessage('Tiền cọc không được âm'),
  
  body('price.utilities')
    .optional()
    .isFloat({ min: 0 })
    .withMessage('Phí tiện ích không được âm'),
  
  body('status')
    .optional()
    .isIn(['active', 'inactive', 'rented', 'maintenance'])
    .withMessage('Trạng thái không hợp lệ'),
  
  handleValidationErrors
];

const validateBookingCreation = [
  body('roomId')
    .isMongoId()
    .withMessage('ID phòng không hợp lệ'),
  
  body('bookingDetails.checkInDate')
    .isISO8601()
    .withMessage('Ngày nhận phòng không hợp lệ')
    .custom((value) => {
      if (new Date(value) < new Date()) {
        throw new Error('Ngày nhận phòng không được trong quá khứ');
      }
      return true;
    }),
  
  body('bookingDetails.checkOutDate')
    .isISO8601()
    .withMessage('Ngày trả phòng không hợp lệ')
    .custom((value, { req }) => {
      const checkInDate = req.body.bookingDetails?.checkInDate;
      if (checkInDate && new Date(value) <= new Date(checkInDate)) {
        throw new Error('Ngày trả phòng phải sau ngày nhận phòng');
      }
      return true;
    }),
  
  body('bookingDetails.numberOfOccupants')
    .isInt({ min: 1 })
    .withMessage('Số người ở phải ít nhất 1'),
  
  handleValidationErrors
];

const validatePayment = [
  body('amount')
    .isFloat({ min: 0 })
    .withMessage('Số tiền không được âm'),
  
  body('paymentMethod')
    .isIn(['vnpay', 'bank_transfer', 'cash', 'other'])
    .withMessage('Phương thức thanh toán không hợp lệ'),
  
  body('type')
    .isIn(['deposit', 'monthly_rent', 'utilities', 'penalty', 'refund'])
    .withMessage('Loại thanh toán không hợp lệ'),
  
  handleValidationErrors
];

const validateSearch = [
  query('page')
    .optional()
    .isInt({ min: 1 })
    .withMessage('Trang phải là số nguyên dương'),
  
  query('limit')
    .optional()
    .isInt({ min: 1, max: 100 })
    .withMessage('Giới hạn phải từ 1 đến 100'),
  
  query('minPrice')
    .optional()
    .isFloat({ min: 0 })
    .withMessage('Giá tối thiểu không được âm'),
  
  query('maxPrice')
    .optional()
    .isFloat({ min: 0 })
    .withMessage('Giá tối đa không được âm'),
  
  query('roomType')
    .optional()
    .isIn(['studio', '1bedroom', '2bedroom', '3bedroom', 'shared'])
    .withMessage('Loại phòng không hợp lệ'),
  
  query('city')
    .optional()
    .trim()
    .isLength({ max: 100 })
    .withMessage('Tên thành phố không được vượt quá 100 ký tự'),
  
  query('district')
    .optional()
    .trim()
    .isLength({ max: 100 })
    .withMessage('Tên quận/huyện không được vượt quá 100 ký tự'),
  
  handleValidationErrors
];

const validateReview = [
  body('rating.overall')
    .isInt({ min: 1, max: 5 })
    .withMessage('Đánh giá tổng thể phải từ 1 đến 5'),
  
  body('comment')
    .trim()
    .notEmpty()
    .withMessage('Bình luận là bắt buộc')
    .isLength({ max: 1000 })
    .withMessage('Bình luận không được vượt quá 1000 ký tự'),
  
  body('title')
    .optional()
    .trim()
    .isLength({ max: 100 })
    .withMessage('Tiêu đề không được vượt quá 100 ký tự'),
  
  handleValidationErrors
];

const validateNotification = [
  body('recipient')
    .isMongoId()
    .withMessage('ID người nhận không hợp lệ'),
  
  body('type')
    .isIn([
      'booking_request', 'booking_confirmed', 'booking_cancelled',
      'payment_received', 'payment_due', 'payment_overdue',
      'contract_signed', 'room_available', 'maintenance_request',
      'review_request', 'system_announcement', 'support_ticket', 'other'
    ])
    .withMessage('Loại thông báo không hợp lệ'),
  
  body('title')
    .trim()
    .notEmpty()
    .withMessage('Tiêu đề là bắt buộc')
    .isLength({ max: 200 })
    .withMessage('Tiêu đề không được vượt quá 200 ký tự'),
  
  body('message')
    .trim()
    .notEmpty()
    .withMessage('Nội dung là bắt buộc')
    .isLength({ max: 1000 })
    .withMessage('Nội dung không được vượt quá 1000 ký tự'),
  
  handleValidationErrors
];

const validateObjectId = (paramName) => [
  param(paramName)
    .isMongoId()
    .withMessage(`${paramName} không hợp lệ`),
  
  handleValidationErrors
];

module.exports = {
  handleValidationErrors,
  validateUserRegistration,
  validateUserLogin,
  validateUserUpdate,
  validateRoomCreation,
  validateRoomUpdate,
  validateBookingCreation,
  validatePayment,
  validateSearch,
  validateReview,
  validateNotification,
  validateObjectId
};
