const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const User = require('../models/User');
require('dotenv').config({ path: './config.env' });

async function createAdmin() {
  try {
    await mongoose.connect(process.env.MONGODB_URI, {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    });
    console.log('Connected to MongoDB');

    const existingAdmin = await User.findOne({ role: 'admin' });
    if (existingAdmin) {
      console.log('Admin user already exists:', existingAdmin.email);
      return;
    }

    const adminData = {
      fullName: 'Admin',
      email: 'admin@gmail.com',
      password: 'Matkhau123@',
      phone: '0123456789',
      role: 'admin',
      isActive: true,
      isVerified: true,
      address: {
        street: '123 Đường Admin',
        ward: 'Phường Admin',
        district: 'Quận Admin',
        city: 'TP. Hồ Chí Minh'
      }
    };

    const admin = new User(adminData);
    await admin.save();

    console.log('Admin user created successfully:');
    console.log('Email:', admin.email);
    console.log('Password: admin123456');
    console.log('Role:', admin.role);

  } catch (error) {
    console.error('Error creating admin user:', error);
  } finally {
    await mongoose.disconnect();
    console.log('Disconnected from MongoDB');
  }
}

createAdmin();
