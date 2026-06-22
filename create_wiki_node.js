const https = require('https');

const TOKEN = process.env.LARKSUITE_CLI_USER_ACCESS_TOKEN;
const SPACE_ID = '7633341147464715453';
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

async function createWikiNode() {
  const body = JSON.stringify({
    node_type: 'origin',
    obj_type: 'docx',
    parent_node_token: PARENT_NODE_TOKEN,
    title: '智汇桥——AI驱动的产学研用一体化智慧协同系统',
  });

  const res = await request(
    {
      hostname: 'open.feishu.cn',
      path: `/open-apis/wiki/v2/spaces/${SPACE_ID}/nodes`,
      method: 'POST',
      headers: {
        Authorization: `Bearer ${TOKEN}`,
        'Content-Type': 'application/json',
      },
    },
    body
  );

  console.log(JSON.stringify(res, null, 2));
}

createWikiNode().catch(console.error);
