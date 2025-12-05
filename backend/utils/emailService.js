const nodemailer = require('nodemailer');

function createTransporter() {
    return nodemailer.createTransport({
        host: process.env.MAIL_SERVER,
        port: parseInt(process.env.MAIL_PORT, 10),
        secure: false, 
        auth: {
            user: process.env.MAIL_USERNAME,
            pass: process.env.MAIL_PASSWORD
        }
    });
}

const emailTemplates = {
  bookingConfirmation: (data) => ({
    subject: `Xác nhận đặt phòng - ${data.roomTitle}`,
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2 style="color: #2c3e50;">Xác nhận đặt phòng thành công!</h2>
        <p>Xin chào <strong>${data.tenantName}</strong>,</p>
        <p>Chúng tôi xin thông báo rằng yêu cầu đặt phòng của bạn đã được xác nhận thành công.</p>
        
        <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;">
          <h3 style="color: #2c3e50; margin-top: 0;">Thông tin đặt phòng:</h3>
          <p><strong>Phòng:</strong> ${data.roomTitle}</p>
          <p><strong>Địa chỉ:</strong> ${data.roomAddress}</p>
          <p><strong>Ngày nhận phòng:</strong> ${data.checkInDate}</p>
          <p><strong>Ngày trả phòng:</strong> ${data.checkOutDate}</p>
          <p><strong>Thời gian thuê:</strong> ${data.duration} tháng</p>
          <p><strong>Tiền cọc:</strong> ${data.deposit.toLocaleString('vi-VN')} VNĐ</p>
          <p><strong>Tiền thuê hàng tháng:</strong> ${data.monthlyRent.toLocaleString('vi-VN')} VNĐ</p>
        </div>
        
        <p>Vui lòng thanh toán tiền cọc trong vòng 24 giờ để hoàn tất quá trình đặt phòng.</p>
        <p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>
        
        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #dee2e6;">
          <p style="color: #6c757d; font-size: 14px;">
            Trân trọng,<br>
            Đội ngũ Quản lý Phòng trọ
          </p>
        </div>
      </div>
    `
  }),

  bookingNotificationToLandlord: (data) => ({
    subject: `Thông báo đặt phòng mới - ${data.roomTitle}`,
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2 style="color: #2c3e50;">Có đặt phòng mới!</h2>
        <p>Xin chào <strong>${data.landlordName}</strong>,</p>
        <p>Bạn có một yêu cầu đặt phòng mới từ khách hàng.</p>
        
        <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;">
          <h3 style="color: #2c3e50; margin-top: 0;">Thông tin đặt phòng:</h3>
          <p><strong>Phòng:</strong> ${data.roomTitle}</p>
          <p><strong>Địa chỉ:</strong> ${data.roomAddress}</p>
          <p><strong>Khách hàng:</strong> ${data.tenantName}</p>
          <p><strong>Số điện thoại:</strong> ${data.tenantPhone}</p>
          <p><strong>Email:</strong> ${data.tenantEmail}</p>
          <p><strong>Ngày nhận phòng:</strong> ${data.checkInDate}</p>
          <p><strong>Ngày trả phòng:</strong> ${data.checkOutDate}</p>
          <p><strong>Thời gian thuê:</strong> ${data.duration} tháng</p>
          <p><strong>Số người ở:</strong> ${data.numberOfOccupants}</p>
        </div>
        
        <p>Vui lòng xác nhận hoặc từ chối yêu cầu đặt phòng này trong vòng 24 giờ.</p>
        
        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #dee2e6;">
          <p style="color: #6c757d; font-size: 14px;">
            Trân trọng,<br>
            Đội ngũ Quản lý Phòng trọ
          </p>
        </div>
      </div>
    `
  }),

  paymentConfirmation: (data) => ({
    subject: `Xác nhận thanh toán - ${data.transactionId}`,
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2 style="color: #28a745;">Thanh toán thành công!</h2>
        <p>Xin chào <strong>${data.payerName}</strong>,</p>
        <p>Chúng tôi xin xác nhận rằng giao dịch thanh toán của bạn đã được xử lý thành công.</p>
        
        <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;">
          <h3 style="color: #2c3e50; margin-top: 0;">Thông tin giao dịch:</h3>
          <p><strong>Mã giao dịch:</strong> ${data.transactionId}</p>
          <p><strong>Loại thanh toán:</strong> ${data.paymentType}</p>
          <p><strong>Số tiền:</strong> ${data.amount.toLocaleString('vi-VN')} VNĐ</p>
          <p><strong>Phương thức:</strong> ${data.paymentMethod}</p>
          <p><strong>Thời gian:</strong> ${data.paidAt}</p>
          <p><strong>Trạng thái:</strong> <span style="color: #28a745;">Thành công</span></p>
        </div>
        
        <p>Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!</p>
        
        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #dee2e6;">
          <p style="color: #6c757d; font-size: 14px;">
            Trân trọng,<br>
            Đội ngũ Quản lý Phòng trọ
          </p>
        </div>
      </div>
    `
  }),

  passwordReset: (data) => ({
    subject: 'Yêu cầu đặt lại mật khẩu',
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2 style="color: #2c3e50;">Đặt lại mật khẩu</h2>
        <p>Xin chào <strong>${data.userName}</strong>,</p>
        <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
        
        <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0; text-align: center;">
          <p>Nhấp vào liên kết bên dưới để đặt lại mật khẩu:</p>
          <a href="${data.resetUrl}" style="background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">
            Đặt lại mật khẩu
          </a>
        </div>
        
        <p><strong>Lưu ý:</strong> Liên kết này sẽ hết hạn sau 1 giờ.</p>
        <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
        
        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #dee2e6;">
          <p style="color: #6c757d; font-size: 14px;">
            Trân trọng,<br>
            Đội ngũ Quản lý Phòng trọ
          </p>
        </div>
      </div>
    `
  }),

  welcome: (data) => ({
    subject: 'Chào mừng bạn đến với Quản lý Phòng trọ!',
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2 style="color: #2c3e50;">Chào mừng bạn!</h2>
        <p>Xin chào <strong>${data.userName}</strong>,</p>
        <p>Chào mừng bạn đến với hệ thống Quản lý Phòng trọ!</p>
        
        <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;">
          <h3 style="color: #2c3e50; margin-top: 0;">Thông tin tài khoản:</h3>
          <p><strong>Họ tên:</strong> ${data.userName}</p>
          <p><strong>Email:</strong> ${data.userEmail}</p>
          <p><strong>Vai trò:</strong> ${data.userRole}</p>
        </div>
        
        <p>Bạn có thể bắt đầu sử dụng các tính năng của hệ thống ngay bây giờ.</p>
        
        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #dee2e6;">
          <p style="color: #6c757d; font-size: 14px;">
            Trân trọng,<br>
            Đội ngũ Quản lý Phòng trọ
          </p>
        </div>
      </div>
    `
  })
};

const sendEmail = async (to, templateName, data) => {
  try {
    if (!to || typeof to !== 'string' || !to.includes('@')) {
      throw new Error(`Invalid email address: ${to}`);
    }
    
    const transporter = createTransporter();
    
    if (!emailTemplates[templateName]) {
      throw new Error(`Email template '${templateName}' not found`);
    }
    
    const template = emailTemplates[templateName](data);
    
    const mailOptions = {
      from: `"Quản lý Phòng trọ" <${process.env.MAIL_USERNAME}>`,
      to: to,
      subject: template.subject,
      html: template.html
    };
    
    const result = await transporter.sendMail(mailOptions);
    return result;
  } catch (error) {
    throw error;
  }
};

const sendBulkEmails = async (recipients, templateName, data) => {
  const results = [];
  
  for (const recipient of recipients) {
    try {
      const result = await sendEmail(recipient, templateName, data);
      results.push({ recipient, success: true, messageId: result.messageId });
    } catch (error) {
      results.push({ recipient, success: false, error: error.message });
    }
  }
  
  return results;
};

const testEmailConnection = async () => {
  try {
    const transporter = createTransporter();
    await transporter.verify();
    console.log('Email server connection verified');
    return true;
  } catch (error) {
    console.error('Email server connection failed:', error);
    return false;
  }
};

module.exports = {
  sendEmail,
  sendBulkEmails,
  testEmailConnection,
  emailTemplates
};
