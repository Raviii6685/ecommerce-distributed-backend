import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '2m', target: 100 },
    { duration: '2m', target: 200 },
    { duration: '2m', target: 300 },
    { duration: '2m', target: 400 },
    { duration: '2m', target: 500 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<10000'],
    http_req_failed: ['rate<0.1'],
  },
};

const BASE_URL = 'http://localhost:8080';

const PRODUCT_IDS = [
  'bfce328b-a48e-44eb-9a7a-3e87b9fe17f1',
  '9f9b3ddf-1e5c-4b9d-9311-d4445aa460d7',
  '26ddd7f0-f5e1-480c-a814-11d113e8c1ae',
  '62046a3f-fdeb-4089-a1cd-d80bf303fc37',
  '4f29a71b-49e3-4867-8575-64f78bf4b434',
  'c72a80e5-991e-4b80-aa39-78f98e6b8b40',
  '56f74a49-7e3e-4d99-9ec9-bd40f901473c',
  'd0dab5c5-65d2-4cc5-8735-3290205dd4ae',
  '4eb74646-3574-4e25-b2c2-6193f200a1b0',
  '42396ff0-8b89-4d7f-9701-4b93eb43ff12',
];

function getRandomPage() {
  const rand = Math.random();
  if (rand < 0.5) return 0;
  if (rand < 0.7) return 1;
  if (rand < 0.85) return 2;
  return Math.floor(Math.random() * 10) + 3;
}

export function setup() {
  const tokens = {};

  const adminRes = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ username: 'admin@gmail.com', password: 'Test@1234' }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  if (adminRes.status === 200) tokens['admin'] = adminRes.json('token');

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
  const adminToken = tokens['admin'];

  const userHeaders = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${userToken}`,
  };

  const adminHeaders = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${adminToken}`,
  };

  const rand = Math.random();
  const page = getRandomPage();
  const size = 20;

  if (rand < 0.4) {
    // 40% — Products read (replica)
    const res = http.get(
      `${BASE_URL}/api/products?page=${page}&size=${size}`,
      { headers: userHeaders, tags: { name: `products-page-${page}` } }
    );
    check(res, { 'products ok': (r) => r.status === 200 });

  } else if (rand < 0.6) {
    // 20% — Categories read (replica)
    const res = http.get(
      `${BASE_URL}/api/categories?page=${page}&size=${size}`,
      { headers: userHeaders, tags: { name: `categories-page-${page}` } }
    );
    check(res, { 'categories ok': (r) => r.status === 200 });

  } else if (rand < 0.7) {
    // 10% — My orders read (replica)
    const res = http.get(
      `${BASE_URL}/api/orders?page=${page}&size=${size}`,
      { headers: userHeaders, tags: { name: `orders-page-${page}` } }
    );
    check(res, { 'orders ok': (r) => r.status === 200 });

  } else if (rand < 0.8) {
    // 10% — Admin orders read (replica)
    const res = http.get(
      `${BASE_URL}/api/admin/orders?page=${page}&size=${size}`,
      { headers: adminHeaders, tags: { name: `admin-orders-page-${page}` } }
    );
    check(res, { 'admin orders ok': (r) => r.status === 200 });

  } else {
    // 20% — Order create (primary/Leader!)
    const randomProduct = PRODUCT_IDS[Math.floor(Math.random() * PRODUCT_IDS.length)];
    const orderRes = http.post(
      `${BASE_URL}/api/orders`,
      JSON.stringify({
        items: [{ productId: randomProduct, quantity: 1 }],
        shippingAddress: 'Test Street, City'
      }),
      { headers: userHeaders, tags: { name: 'create-order' } }
    );
    check(orderRes, { 'order created': (r) => r.status === 201 });
  }

  sleep(0.5);
}
