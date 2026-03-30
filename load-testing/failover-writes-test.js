import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },  // warmup
    { duration: '5m', target: 50 },   // failover yahan karenge
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<30000'],
    http_req_failed: ['rate<0.5'],
  },
};

const BASE_URL = 'http://localhost:8080';

const PRODUCT_IDS = [
  'bfce328b-a48e-44eb-9a7a-3e87b9fe17f1',
  '9f9b3ddf-1e5c-4b9d-9311-d4445aa460d7',
  '26ddd7f0-f5e1-480c-a814-11d113e8c1ae',
  '62046a3f-fdeb-4089-a1cd-d80bf303fc37',
  '4f29a71b-49e3-4867-8575-64f78bf4b434',
];

export function setup() {
  const tokens = {};

  for (let i = 1; i <= 20; i++) {
    const res = http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({ username: `testuser${i}@gmail.com`, password: 'Test@1234' }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    if (res.status === 200) tokens[`user${i}`] = res.json('token');
  }
  return tokens;
}

export default function (tokens) {
  const userIndex = (__VU % 20) + 1;
  const userToken = tokens[`user${userIndex}`];

  const userHeaders = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${userToken}`,
  };

  const randomProduct = PRODUCT_IDS[Math.floor(Math.random() * PRODUCT_IDS.length)];

  const res = http.post(
    `${BASE_URL}/api/orders`,
    JSON.stringify({
      items: [{ productId: randomProduct, quantity: 1 }],
      shippingAddress: 'Test Street, City'
    }),
    { headers: userHeaders, tags: { name: 'create-order' } }
  );

  check(res, { 'order created': (r) => r.status === 201 });
  sleep(1);
}
