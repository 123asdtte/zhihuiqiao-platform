const https = require('https');

const TOKEN = process.env.LARKSUITE_CLI_USER_ACCESS_TOKEN;
const PARENT_NODE_TOKEN = 'QgGNw7Q7Piw0UykeV0xctFL5nnf';

function request(options, body = null) {
  return new Promise((resolve, reject) => {
    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => (data += chunk));
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, data: JSON.parse(data) });
        } catch (e) {
          resolve({ status: res.statusCode, data });
        }
      });
    });
    req.on('error', reject);
    if (body) req.write(body);
    req.end();
  });
}

async function createDoc() {
  const body = JSON.stringify({
    folder_token: PARENT_NODE_TOKEN,
    title: '智汇桥——AI驱动的产学研用一体化智慧协同系统',
  });

  return request(
    {
      hostname: 'open.feishu.cn',
      path: '/open-apis/docx/v1/documents',
      method: 'POST',
      headers: {
        Authorization: `Bearer ${TOKEN}`,
        'Content-Type': 'application/json',
      },
    },
    body
  );
}

async function main() {
  console.log('Creating docx...');
  const res = await createDoc();
  console.log(JSON.stringify(res, null, 2));
}

main().catch(console.error);
