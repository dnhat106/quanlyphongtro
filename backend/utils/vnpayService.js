const crypto = require('crypto');
const querystring = require('querystring');

class VNPayService {
  constructor() {
    this.tmnCode = process.env.VNPAY_TMN_CODE;
    this.hashSecret = process.env.VNPAY_HASH_SECRET;
    this.url = process.env.VNPAY_URL;
    this.returnUrl = process.env.VNPAY_RETURN_URL;
  }

  sortObject(obj) {
    const sorted = {};
    const str = [];
    let key;
    for (key in obj) {
      if (obj.hasOwnProperty(key)) {
        str.push(encodeURIComponent(key));
      }
    }
    str.sort();
    for (key = 0; key < str.length; key++) {
      sorted[str[key]] = encodeURIComponent(obj[str[key]]).replace(/%20/g, '+');
    }
    return sorted;
  }

  createPaymentUrl(params) {
    const {
      amount,
      orderInfo,
      orderType = 'other',
      txnRef,
      bankCode = '',
      language = 'vn',
      ipAddr = '127.0.0.1'
    } = params;

    const date = new Date();
    const createDate = date.getFullYear() + 
      String(date.getMonth() + 1).padStart(2, '0') + 
      String(date.getDate()).padStart(2, '0') + 
      String(date.getHours()).padStart(2, '0') + 
      String(date.getMinutes()).padStart(2, '0') + 
      String(date.getSeconds()).padStart(2, '0');

    const expireDate = new Date(date.getTime() + 15 * 60 * 1000); 
    const expireDateStr = expireDate.getFullYear() + 
      String(expireDate.getMonth() + 1).padStart(2, '0') + 
      String(expireDate.getDate()).padStart(2, '0') + 
      String(expireDate.getHours()).padStart(2, '0') + 
      String(expireDate.getMinutes()).padStart(2, '0') + 
      String(expireDate.getSeconds()).padStart(2, '0');

    const vnpParams = {
      vnp_Version: '2.1.0',
      vnp_Command: 'pay',
      vnp_TmnCode: this.tmnCode,
      vnp_Locale: language,
      vnp_CurrCode: 'VND',
      vnp_TxnRef: txnRef,
      vnp_OrderInfo: orderInfo,
      vnp_OrderType: orderType,
      vnp_Amount: amount * 100,
      vnp_ReturnUrl: this.returnUrl,
      vnp_IpAddr: ipAddr,
      vnp_CreateDate: createDate,
      vnp_ExpireDate: expireDateStr
    };

    if (bankCode !== null && bankCode !== '') {
      vnpParams.vnp_BankCode = bankCode;
    }

    const sortedParams = this.sortObject(vnpParams);
    const signData = querystring.stringify(sortedParams, { encode: false });
    const hmac = crypto.createHmac('sha512', this.hashSecret);
    const signed = hmac.update(Buffer.from(signData, 'utf-8')).digest('hex');
    vnpParams.vnp_SecureHash = signed;

    return this.url + '?' + querystring.stringify(vnpParams, { encode: false });
  }

  verifyPaymentResult(vnpParams) {
    const secureHash = vnpParams.vnp_SecureHash;
    delete vnpParams.vnp_SecureHash;
    delete vnpParams.vnp_SecureHashType;

    const sortedParams = this.sortObject(vnpParams);
    const signData = querystring.stringify(sortedParams, { encode: false });
    const hmac = crypto.createHmac('sha512', this.hashSecret);
    const signed = hmac.update(Buffer.from(signData, 'utf-8')).digest('hex');

    return secureHash === signed;
  }

  parsePaymentResult(vnpParams) {
    const isValid = this.verifyPaymentResult(vnpParams);
    
    return {
      isValid,
      txnRef: vnpParams.vnp_TxnRef,
      amount: parseInt(vnpParams.vnp_Amount) / 100, 
      orderInfo: vnpParams.vnp_OrderInfo,
      responseCode: vnpParams.vnp_ResponseCode,
      transactionNo: vnpParams.vnp_TransactionNo,
      transactionStatus: vnpParams.vnp_TransactionStatus,
      bankCode: vnpParams.vnp_BankCode,
      cardType: vnpParams.vnp_CardType,
      payDate: vnpParams.vnp_PayDate,
      message: this.getResponseMessage(vnpParams.vnp_ResponseCode)
    };
  }

  getResponseMessage(responseCode) {
    const messages = {
      '00': 'Giao dịch thành công',
      '07': 'Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).',
      '09': 'Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking của ngân hàng.',
      '10': 'Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần',
      '11': 'Đã hết hạn chờ thanh toán. Xin vui lòng thực hiện lại giao dịch.',
      '12': 'Giao dịch bị hủy.',
      '24': 'Giao dịch không thành công do: Khách hàng hủy giao dịch',
      '51': 'Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.',
      '65': 'Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.',
      '75': 'Ngân hàng thanh toán đang bảo trì.',
      '79': 'Nhập sai mật khẩu thanh toán quá số lần quy định. Xin vui lòng thực hiện lại giao dịch.',
      '99': 'Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)'
    };

    return messages[responseCode] || 'Lỗi không xác định';
  }

  createRefundUrl(params) {
    const {
      txnRef,
      amount,
      transactionNo,
      transactionDate,
      user,
      orderInfo = 'Hoan tien giao dich'
    } = params;

    const date = new Date();
    const createDate = date.getFullYear() + 
      String(date.getMonth() + 1).padStart(2, '0') + 
      String(date.getDate()).padStart(2, '0') + 
      String(date.getHours()).padStart(2, '0') + 
      String(date.getMinutes()).padStart(2, '0') + 
      String(date.getSeconds()).padStart(2, '0');

    const vnpParams = {
      vnp_Version: '2.1.0',
      vnp_Command: 'refund',
      vnp_TmnCode: this.tmnCode,
      vnp_TransactionType: '03',
      vnp_TxnRef: txnRef,
      vnp_Amount: amount * 100,
      vnp_OrderInfo: orderInfo,
      vnp_TransactionNo: transactionNo,
      vnp_TransactionDate: transactionDate,
      vnp_CreateBy: user,
      vnp_CreateDate: createDate,
      vnp_IpAddr: '127.0.0.1'
    };

    const sortedParams = this.sortObject(vnpParams);
    const signData = querystring.stringify(sortedParams, { encode: false });
    const hmac = crypto.createHmac('sha512', this.hashSecret);
    const signed = hmac.update(Buffer.from(signData, 'utf-8')).digest('hex');
    vnpParams.vnp_SecureHash = signed;

    return this.url + '?' + querystring.stringify(vnpParams, { encode: false });
  }

  generateTxnRef(prefix = 'TXN') {
    const timestamp = Date.now();
    const random = Math.random().toString(36).substring(2, 8).toUpperCase();
    return `${prefix}${timestamp}${random}`;
  }

  formatAmount(amount) {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  }

  getSupportedBanks() {
    return [
      { code: 'NCB', name: 'Ngân hàng Quốc Dân (NCB)' },
      { code: 'VIETCOMBANK', name: 'Ngân hàng TMCP Ngoại Thương Việt Nam' },
      { code: 'VIETINBANK', name: 'Ngân hàng TMCP Công Thương Việt Nam' },
      { code: 'BIDV', name: 'Ngân hàng TMCP Đầu tư và Phát triển Việt Nam' },
      { code: 'AGRIBANK', name: 'Ngân hàng Nông nghiệp và Phát triển Nông thôn Việt Nam' },
      { code: 'TECHCOMBANK', name: 'Ngân hàng TMCP Kỹ thương Việt Nam' },
      { code: 'ACB', name: 'Ngân hàng TMCP Á Châu' },
      { code: 'SACOMBANK', name: 'Ngân hàng TMCP Sài Gòn Thương Tín' },
      { code: 'DONGABANK', name: 'Ngân hàng TMCP Đông Á' },
      { code: 'EXIMBANK', name: 'Ngân hàng TMCP Xuất Nhập khẩu Việt Nam' },
      { code: 'MBBANK', name: 'Ngân hàng TMCP Quân đội' },
      { code: 'TPBANK', name: 'Ngân hàng TMCP Tiên Phong' },
      { code: 'OCB', name: 'Ngân hàng TMCP Phương Đông' },
      { code: 'SHB', name: 'Ngân hàng TMCP Sài Gòn - Hà Nội' },
      { code: 'VPBANK', name: 'Ngân hàng TMCP Việt Nam Thịnh Vượng' }
    ];
  }
}

module.exports = new VNPayService();
