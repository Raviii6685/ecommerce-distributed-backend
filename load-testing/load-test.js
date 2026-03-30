import http from 'k6/http';
import { check, sleep } from 'k6';
export const options = {
  stages: [
    { duration: '30s', target: 50 },
    { duration: '1m', target: 100 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = 'http://localhost:8080';

function getRandomPage() {
  const rand = Math.random();
  if (rand < 0.5) return 0;
  if (rand < 0.7) return 1;
  if (rand < 0.85) return 2;
  return Math.floor(Math.random() * 10) + 3;
}
export function setup() {
  const tokens = {};
  
  // Admin login
  const adminRes = http.post(`${BASE_URL}/api/auth/login`,
    JSON.stringify({ username: 'admin@gmail.com', password: 'Test@1234' }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  tokens['admin'] = adminRes.json('token');

  // 20 testusers login karo
  for (let i = 1; i <= 20; i++) {
    const res = http.post(`${BASE_URL}/api/auth/login`,
      JSON.stringify({ username: `testuser${i}@gmail.com`, password: 'Test@1234' }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    if (res.status === 200) {
      tokens[`user${i}`] = res.json('token');
    }
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
    // 40% — Products (any user)
    const res = http.get(`${BASE_URL}/api/products?page=${page}&size=${size}`,
      { headers: userHeaders, tags: { name: `products-page-${page}` } });
    check(res, { 'products ok': (r) => r.status === 200 });

  } else if (rand < 0.6) {
    // 20% — Categories (any user)
    const res = http.get(`${BASE_URL}/api/categories?page=${page}&size=${size}`,
      { headers: userHeaders, tags: { name: `categories-page-${page}` } });
    check(res, { 'categories ok': (r) => r.status === 200 });

  } else if (rand < 0.8) {
    // 20% — My orders (user specific)
    const res = http.get(`${BASE_URL}/api/orders?page=${page}&size=${size}`,
      { headers: userHeaders, tags: { name: `orders-page-${page}` } });
    check(res, { 'orders ok': (r) => r.status === 200 });

  } else {
    // 20% — Admin all orders (sirf admin)
    const res = http.get(`${BASE_URL}/api/admin/orders?page=${page}&size=${size}`,
      { headers: adminHeaders, tags: { name: `admin-orders-page-${page}` } });
    check(res, { 'admin orders ok': (r) => r.status === 200 });
  }

  sleep(0.5);
}
