const fs = require('fs');
const path = require('path');
const https = require('https');
const FormData = require('form-data');

const TOKEN = process.env.LARKSUITE_CLI_USER_ACCESS_TOKEN;
const PARENT_NODE_TOKEN = 'QgGNw7Q7Piw0UykeV0xctFL5nnf';
const SPACE_ID = '7633341147464715453';
const FILE_PATH = 'zhihuiqiao_doc.md';
const DOC_TITLE = '智汇桥——AI驱动的产学研用一体化智慧协同系统';

function request(options, body = null) {
  return new Promise((resolve, reject) => {
    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => (data += chunk));
      res.on('end', () => {
        try {
          const json = JSON.parse(data);
          resolve({ status: res.statusCode, data: json });
        } catch (e) {
          resolve({ status: res.statusCode, data });
        }
      });
    });
    req.on('error', reject);
    if (body) {
      req.write(body);
    }
    req.end();
  });
}

async function uploadFile() {
  const form = new FormData();
  form.append('file_name', DOC_TITLE + '.md');
  form.append('parent_type', 'explorer');
  form.append('parent_node', PARENT_NODE_TOKEN);
  form.append('size', fs.statSync(FILE_PATH).size.toString());
  form.append('file', fs.createReadStream(FILE_PATH));

  const headers = {
    ...form.getHeaders(),
    Authorization: `Bearer ${TOKEN}`,
  };

  return new Promise((resolve, reject) => {
    const req = https.request(
      {
        hostname: 'open.feishu.cn',
        path: '/open-apis/drive/v1/medias/upload_all',
        method: 'POST',
        headers,
      },
      (res) => {
        let data = '';
        res.on('data', (chunk) => (data += chunk));
        res.on('end', () => {
          try {
            resolve(JSON.parse(data));
          } catch (e) {
            resolve(data);
          }
        });
      }
    );
    req.on('error', reject);
    form.pipe(req);
  });
}

async function createImportTask(fileToken) {
  const body = JSON.stringify({
    file_extension: 'md',
    file_token: fileToken,
    type: 'docx',
    file_name: DOC_TITLE,
    point_key: PARENT_NODE_TOKEN,
  });

  return request(
    {
      hostname: 'open.feishu.cn',
      path: '/open-apis/drive/v1/import_tasks',
      method: 'POST',
      headers: {
        Authorization: `Bearer ${TOKEN}`,
        'Content-Type': 'application/json',
      },
    },
    body
  );
}

async function getImportTask(ticket) {
  return request({
    hostname: 'open.feishu.cn',
    path: `/open-apis/drive/v1/import_tasks/${ticket}`,
    method: 'GET',
    headers: {
      Authorization: `Bearer ${TOKEN}`,
    },
  });
}

async function main() {
  console.log('Step 1: Uploading file...');
  const uploadRes = await uploadFile();
  console.log('Upload result:', JSON.stringify(uploadRes, null, 2));

  if (uploadRes.code !== 0) {
    console.error('Upload failed');
    return;
  }

  const fileToken = uploadRes.data.file_token;
  console.log('File token:', fileToken);

  console.log('Step 2: Creating import task...');
  const importRes = await createImportTask(fileToken);
  console.log('Import task result:', JSON.stringify(importRes, null, 2));

  if (importRes.data.code !== 0) {
    console.error('Import task creation failed');
    return;
  }

  const ticket = importRes.data.data.ticket;
  console.log('Ticket:', ticket);

  console.log('Step 3: Polling import task...');
  for (let i = 0; i < 30; i++) {
    await new Promise((r) => setTimeout(r, 2000));
    const statusRes = await getImportTask(ticket);
    console.log(`Poll ${i + 1}:`, JSON.stringify(statusRes, null, 2));

    if (statusRes.data.data.result === 'success') {
      console.log('Import success!');
      console.log('Document token:', statusRes.data.data.extra.docx_token);
      break;
    }
    if (statusRes.data.data.result === 'fail') {
      console.error('Import failed');
      break;
    }
  }
}

main().catch(console.error);
