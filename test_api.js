const https = require('https');

const token = process.env.LARKSUITE_CLI_USER_ACCESS_TOKEN;
console.log('Token length:', token ? token.length : 0);

const options = {
  hostname: 'open.feishu.cn',
  path: '/open-apis/contact/v3/users/me',
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
};

const req = https.request(options, (res) => {
  let data = '';
  res.on('data', (chunk) => {
    data += chunk;
  });
  res.on('end', () => {
    console.log('Status:', res.statusCode);
    console.log('Response:', data);
  });
});

req.on('error', (e) => {
  console.error('Error:', e.message);
});

req.end();
