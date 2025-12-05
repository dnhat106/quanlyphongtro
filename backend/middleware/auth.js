const jwt = require('jsonwebtoken');
const User = require('../models/User');

const generateToken = (userId) => {
  return jwt.sign({ userId }, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRE || '7d'
  });
};

const verifyToken = (token) => {
  return jwt.verify(token, process.env.JWT_SECRET);
};

const authenticate = async (req, res, next) => {
  try {
    let token;
    
    if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {
      token = req.headers.authorization.split(' ')[1];
    }
    
    if (!token) {
      return res.status(401).json({
        status: 'error',
        message: 'Access denied. No token provided.'
      });
    }
    
    const decoded = verifyToken(token);
    const user = await User.findById(decoded.userId).select('-password');
    
    if (!user) {
      return res.status(401).json({
        status: 'error',
        message: 'Token is not valid. User not found.'
      });
    }
    
    if (!user.isActive) {
      return res.status(401).json({
        status: 'error',
        message: 'Account is deactivated.'
      });
    }
    
    req.user = user;
    next();
  } catch (error) {
    console.error('Auth middleware error:', error);
    return res.status(401).json({
      status: 'error',
      message: 'Token is not valid.'
    });
  }
};

const authorize = (...roles) => {
  return (req, res, next) => {
    if (!req.user) {
      return res.status(401).json({
        status: 'error',
        message: 'Access denied. Please authenticate first.'
      });
    }
    
    if (!roles.includes(req.user.role)) {
      return res.status(403).json({
        status: 'error',
        message: 'Access denied. Insufficient permissions.'
      });
    }
    
    next();
  };
};

const optionalAuth = async (req, res, next) => {
  try {
    let token;
    
    if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {
      token = req.headers.authorization.split(' ')[1];
    }
    
    if (token) {
      const decoded = verifyToken(token);
      const user = await User.findById(decoded.userId).select('-password');
      
      if (user && user.isActive) {
        req.user = user;
      }
    }
    
    next();
  } catch (error) {
    next();
  }
};

const checkOwnership = (resourceUserIdField = 'userId') => {
  return (req, res, next) => {
    if (!req.user) {
      return res.status(401).json({
        status: 'error',
        message: 'Access denied. Please authenticate first.'
      });
    }
    
    if (req.user.role === 'admin') {
      return next();
    }
    
    const resourceUserId = req.params[resourceUserIdField] || req.body[resourceUserIdField];
    
    if (resourceUserId && resourceUserId !== req.user._id.toString()) {
      return res.status(403).json({
        status: 'error',
        message: 'Access denied. You can only access your own resources.'
      });
    }
    
    next();
  };
};

const checkBookingAccess = async (req, res, next) => {
  try {
    if (!req.user) {
      return res.status(401).json({
        status: 'error',
        message: 'Access denied. Please authenticate first.'
      });
    }
    
    if (req.user.role === 'admin') {
      return next();
    }
    
    const bookingId = req.params.id || req.params.bookingId;
    
    if (bookingId) {
      const Booking = require('../models/Booking');
      const booking = await Booking.findById(bookingId);
      
      if (!booking) {
        return res.status(404).json({
          status: 'error',
          message: 'Booking not found.'
        });
      }

      if (booking.tenant.toString() !== req.user._id.toString() && 
          booking.landlord.toString() !== req.user._id.toString()) {
        return res.status(403).json({
          status: 'error',
          message: 'Access denied. You can only access your own bookings.'
        });
      }
    }
    
    next();
  } catch (error) {
    console.error('Booking access check error:', error);
    return res.status(500).json({
      status: 'error',
      message: 'Error checking booking access.'
    });
  }
};

const checkRoomAccess = async (req, res, next) => {
  try {
    if (!req.user) {
      return res.status(401).json({
        status: 'error',
        message: 'Access denied. Please authenticate first.'
      });
    }
    
    if (req.user.role === 'admin') {
      return next();
    }
    
    const roomId = req.params.id || req.params.roomId;
    
    if (roomId) {
      const Room = require('../models/Room');
      const room = await Room.findById(roomId);
      
      if (!room) {
        return res.status(404).json({
          status: 'error',
          message: 'Room not found.'
        });
      }
      
      if (room.landlord.toString() !== req.user._id.toString()) {
        return res.status(403).json({
          status: 'error',
          message: 'Access denied. You can only access your own rooms.'
        });
      }
    }
    
    next();
  } catch (error) {
    console.error('Room access check error:', error);
    return res.status(500).json({
      status: 'error',
      message: 'Error checking room access.'
    });
  }
};

module.exports = {
  generateToken,
  verifyToken,
  authenticate,
  authorize,
  optionalAuth,
  checkOwnership,
  checkBookingAccess,
  checkRoomAccess
};
