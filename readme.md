# Ecommerce Distributed Backend

A production-grade distributed ecommerce backend built with Spring Boot, PostgreSQL HA cluster, Redis caching, and PgBouncer connection pooling.

---

## Architecture
```
                    Internet
                       ↓
              [Spring Boot App]
              (e2-medium, port 8080)
                       ↓
              [PgBouncer :5432]
              (Connection Pooling + Auto Leader Routing)
                       ↓
        ┌──────────────────────────────┐
        │     PostgreSQL HA Cluster     │
        │                              │
        │  postgresql0 (10.128.0.2)    │
        │  postgresql1 (10.128.0.3)    │
        │  postgresql2 (10.128.0.4)    │
        │                              │
        │  Patroni + etcd              │
        │  Automatic Failover          │
        └──────────────────────────────┘
        
Read/Write Split:
  Writes → PgBouncer → Leader (automatic)
  Reads  → Replica1 / Replica2 (round robin)
  Cache  → Redis (products, categories, orders)
```

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Backend | Spring Boot 3.4.4, Java 21 |
| Database | PostgreSQL 14 |
| HA & Failover | Patroni 4.1.0 + etcd |
| Connection Pooling | PgBouncer |
| Caching | Redis |
| Load Testing | k6 |
| Cloud | GCP (us-central1-c) |

---

## Infrastructure

### VMs
| VM | IP | Role | Type |
|----|----|------|------|
| instance-20260327 | 10.128.0.2 | PostgreSQL + Patroni + etcd | e2-micro |
| postgres-replica | 10.128.0.3 | PostgreSQL + Patroni + etcd | e2-micro |
| postgres-replica2 | 10.128.0.4 | PostgreSQL + Patroni | e2-micro |
| spring-boot-app | 10.128.0.5 | Spring Boot + Redis + PgBouncer | e2-medium |

### PostgreSQL HA Cluster
- **Patroni** — automatic leader election and failover
- **etcd** — distributed consensus store
- **pg_rewind** — automatic timeline sync on node restart
- **Streaming replication** — WAL based, async

---

## Performance Results

### Load Test (100 VUs)
```
p95:          227ms ✅
avg:          53ms
throughput:   93 req/s
error rate:   0%
```

### Stress Test (100-500 VUs, reads + writes)
```
100 VUs → p95 = 483ms  ✅ Comfortable
200 VUs → p95 = 973ms  ⚠️ Stressed
300 VUs → p95 = 1263ms ⚠️ Stressed
400 VUs → p95 = 1746ms ⚠️ Stressed
500 VUs → p95 = 4848ms ❌ Breaking Point
```

### Cache Impact
```
Before Redis caching:
  Breaking point: 400 VUs
  p95 at 100 VUs: 2.5s

After Redis caching + DB indexes:
  Breaking point: 500 VUs
  p95 at 100 VUs: 227ms
  
Improvement: 10x response time improvement!
```

---

## Failover Results

### Automatic Failover Test
```
Crash time:      10:52:38
Recovery time:   10:52:47
RTO:             9 seconds ✅
Failed requests: 3
```

### How it works
```
1. Leader crashes
2. Patroni detects via etcd TTL (30s)
3. New Leader elected via Raft consensus
4. PgBouncer auto-updates via polling script
5. Writes resume automatically
```

---

## Read/Write Split
```java
// DataSourceConfig.java
// Reads → Replica (Patroni health check)
// Writes → Primary (PgBouncer)

protected Object determineCurrentLookupKey() {
    if (!TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
        return "primary"; // → PgBouncer → Leader
    }
    // Check all 3 nodes via Patroni API
    // Route to healthy replica
    return "replica1" or "replica2";
}
```

---

## Dataset

Real Brazilian ecommerce data (Olist):
```
Categories:  71
Products:    32,328
Users:       99,441
Orders:      99,441
Order Items: 50,000
Total:       ~280,000 records
```

---

## Setup Guide

### Prerequisites
- GCP account
- 4 VMs (3x e2-micro + 1x e2-medium)
- Java 21, Maven

### 1. PostgreSQL HA Setup
```bash
# Install on each node
sudo apt install postgresql-14 patroni etcd

# Copy configs
cp infrastructure/patroni/postgresql0.yml /etc/patroni.yml  # node 0
cp infrastructure/patroni/postgresql1.yml /etc/patroni.yml  # node 1
cp infrastructure/patroni/postgresql2.yml /etc/patroni.yml  # node 2

# Start
sudo systemctl start etcd
sudo systemctl start patroni
```

### 2. Spring Boot Setup
```bash
# Clone repo
git clone https://github.com/Raviii6685/ecommerce-distributed-backend

# Build
mvn clean package -DskipTests

# Run
nohup java -jar target/*.jar --spring.profiles.active=prod > app.log 2>&1 &
```

### 3. PgBouncer Setup
```bash
sudo apt install pgbouncer
cp infrastructure/pgbouncer/pgbouncer.ini /etc/pgbouncer/
sudo -u postgres pgbouncer -d /etc/pgbouncer/pgbouncer.ini

# Auto update script
nohup bash infrastructure/scripts/auto-pgbouncer-update.sh &
```

### 4. Redis Setup
```bash
sudo apt install redis-server
sudo systemctl start redis
```

---

## Load Testing
```bash
# Install k6
sudo apt install k6

# Load test
k6 run load-testing/load-test.js

# Stress test
k6 run --out json=results.json load-testing/stress-test.js

# Failover test
bash infrastructure/scripts/failover-measure.sh
```

---

## DDIA Concepts Implemented

| Chapter | Concept | Implementation |
|---------|---------|----------------|
| Chapter 5 | Streaming Replication | WAL logs, async replication |
| Chapter 5 | Leader/Follower | Patroni automatic election |
| Chapter 5 | Replication Lag | LSN monitoring |
| Chapter 8 | Failure Detection | Patroni TTL (30s) |
| Chapter 9 | Consensus | etcd Raft algorithm |
| Chapter 9 | Split Brain Prevention | Timeline fencing |

---

## Future Improvements

- Multi-zone deployment (3 GCP zones)
- Kubernetes + Patroni Operator
- Kafka event streaming
- Prometheus + Grafana monitoring
- Cursor-based pagination
- HAProxy/Consul for automatic replica routing
- Rate limiting
- Distributed tracing (Jaeger)

---

## Key Numbers
```
RTO:              9 seconds
Breaking point:   500 VUs (reads + writes)
Cache improvement: 10x response time
Dataset:          280,000+ records
Nodes:            4 VMs
Replication:      Async streaming
```


## Note :-> 
There is also FrontEnd and there are some bugs also in the front end ...like on order placed stock dont update ...due to caching and also some minor bugs ...which can be fixed easily ...but i was focused on the backend part ...and also i have not added the docker-compose file for this project ...which i will do later ... 