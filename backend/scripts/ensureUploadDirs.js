const fs = require('fs');
const path = require('path');

const uploadDirs = [
  'uploads',
  'uploads/rooms',
  'uploads/users',
  'uploads/temp'
];

uploadDirs.forEach(dir => {
  const fullPath = path.join(__dirname, '..', dir);
  if (!fs.existsSync(fullPath)) {
    fs.mkdirSync(fullPath, { recursive: true });
    console.log(`Created directory: ${fullPath}`);
  } else {
    console.log(`Directory already exists: ${fullPath}`);
  }
});

console.log('Upload directories check completed!');
