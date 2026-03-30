#!/bin/bash

BASE_URL="http://localhost:8080"
LOG_FILE="failover-log.txt"
K6_RESULTS="failover-k6-results.json"

echo "=== FAILOVER MEASUREMENT ===" | tee $LOG_FILE
echo "Start: $(date)" | tee -a $LOG_FILE

# Step 1 — Token lo
TOKEN=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin@gmail.com","password":"Test@1234"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
echo "Token obtained!" | tee -a $LOG_FILE

# Step 2 — PgBouncer update karo pehle
echo ">>> Updating PgBouncer before test..." | tee -a $LOG_FILE
sudo bash /etc/pgbouncer/update-leader.sh >> $LOG_FILE 2>&1
sudo pkill pgbouncer 2>/dev/null
sleep 1
sudo -u postgres pgbouncer -d /etc/pgbouncer/pgbouncer.ini
sleep 2

# Step 3 — Pre-failover verify
echo ">>> Pre-failover write check..." | tee -a $LOG_FILE
PRE=$(curl -s -o /dev/null -w "%{http_code}" \
  --max-time 3 \
  -X POST $BASE_URL/api/orders \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"items":[{"productId":"bfce328b-a48e-44eb-9a7a-3e87b9fe17f1","quantity":1}],"shippingAddress":"Test"}')

echo "Pre-failover write status: $PRE" | tee -a $LOG_FILE

if [ "$PRE" != "201" ]; then
  echo "❌ Writes not working before failover! Fix PgBouncer first." | tee -a $LOG_FILE
  exit 1
fi

echo "✅ Writes working! Starting test..." | tee -a $LOG_FILE

# Step 4 — K6 start karo
echo ">>> Starting k6..." | tee -a $LOG_FILE
k6 run --out json=$K6_RESULTS ~/failover-writes-test.js > /tmp/k6-output.txt 2>&1 &
K6_PID=$!
echo "k6 PID: $K6_PID" | tee -a $LOG_FILE

# Warmup
echo ">>> Warming up 30 seconds..." | tee -a $LOG_FILE
sleep 30

# Step 5 — Current Leader note karo
LEADER=$(etcdctl --endpoints=http://10.128.0.2:2379 get /db/postgres-cluster/leader 2>/dev/null | tr -d '[:space:]')
echo "" | tee -a $LOG_FILE
echo ">>> Current Leader: $LEADER" | tee -a $LOG_FILE

# Step 6 — Crash Leader
echo ">>> CRASHING LEADER NOW!" | tee -a $LOG_FILE
CRASH_TIME=$(date +%s)
CRASH_TIME_HUMAN=$(date)
echo "Crash time: $CRASH_TIME_HUMAN" | tee -a $LOG_FILE

if [ "$LEADER" = "postgresql0" ]; then
  gcloud compute ssh instance-20260327-092016 --zone=us-central1-c \
    --command="sudo systemctl stop patroni && sudo systemctl stop postgresql@14-main" 2>/dev/null
elif [ "$LEADER" = "postgresql1" ]; then
  gcloud compute ssh postgres-replica --zone=us-central1-c \
    --command="sudo systemctl stop patroni && sudo systemctl stop postgresql@14-main" 2>/dev/null
elif [ "$LEADER" = "postgresql2" ]; then
  gcloud compute ssh postgres-replica2 --zone=us-central1-c \
    --command="sudo systemctl stop patroni && sudo systemctl stop postgresql@14-main" 2>/dev/null
fi

echo "Leader crashed!" | tee -a $LOG_FILE

# Step 7 — Monitor
echo "" | tee -a $LOG_FILE
echo ">>> Monitoring writes every second..." | tee -a $LOG_FILE

RECOVERED=false
ELAPSED=0
FIRST_FAIL=0
LAST_FAIL=0
PGBOUNCER_UPDATED=false

while [ $ELAPSED -lt 120 ]; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    --max-time 2 \
    -X POST $BASE_URL/api/orders \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"items":[{"productId":"bfce328b-a48e-44eb-9a7a-3e87b9fe17f1","quantity":1}],"shippingAddress":"Test"}')

  CURRENT_TIME=$(date +%s)
  DIFF=$((CURRENT_TIME - CRASH_TIME))

  # Naya Leader check karo
  NEW_LEADER=$(etcdctl --endpoints=http://10.128.0.2:2379 \
    get /db/postgres-cluster/leader 2>/dev/null | tr -d '[:space:]')

  if [ ! -z "$NEW_LEADER" ] && [ "$NEW_LEADER" != "$LEADER" ] && [ "$PGBOUNCER_UPDATED" = "false" ]; then
    echo "[${DIFF}s] 🔄 New Leader: $NEW_LEADER — Updating PgBouncer..." | tee -a $LOG_FILE
    sudo bash /etc/pgbouncer/update-leader.sh >> $LOG_FILE 2>&1
    sudo pkill -HUP pgbouncer 2>/dev/null || true
    PGBOUNCER_UPDATED=true
    LEADER=$NEW_LEADER
  fi

  if [ "$STATUS" = "201" ]; then
    if [ "$RECOVERED" = "false" ] && [ $FIRST_FAIL -gt 0 ]; then
      RECOVERED=true
      RECOVERY_TIME=$CURRENT_TIME
      RTO=$((RECOVERY_TIME - CRASH_TIME))
      DOWNTIME=$((LAST_FAIL - FIRST_FAIL + 1))
      echo "[${DIFF}s] ✅ WRITES RECOVERED! Status: $STATUS" | tee -a $LOG_FILE
      echo "" | tee -a $LOG_FILE
      echo "=== RESULTS ===" | tee -a $LOG_FILE
      echo "Crash time:    $CRASH_TIME_HUMAN" | tee -a $LOG_FILE
      echo "Recovery time: $(date)" | tee -a $LOG_FILE
      echo "RTO:           ${RTO} seconds" | tee -a $LOG_FILE
      echo "Downtime:      ${DOWNTIME} seconds" | tee -a $LOG_FILE
      break
    else
      echo "[${DIFF}s] ✅ Write OK: $STATUS" | tee -a $LOG_FILE
    fi
  else
    if [ $FIRST_FAIL -eq 0 ]; then
      FIRST_FAIL=$CURRENT_TIME
      echo "[${DIFF}s] ❌ FIRST WRITE FAILURE!" | tee -a $LOG_FILE
    else
      echo "[${DIFF}s] ❌ Still failing: $STATUS" | tee -a $LOG_FILE
    fi
    LAST_FAIL=$CURRENT_TIME
  fi

  ELAPSED=$((ELAPSED + 1))
  sleep 1
done

# K6 stop
echo "" | tee -a $LOG_FILE
echo ">>> Stopping k6..." | tee -a $LOG_FILE
kill $K6_PID 2>/dev/null
sleep 5

# K6 results
echo "" | tee -a $LOG_FILE
echo "=== K6 FAILED REQUESTS ===" | tee -a $LOG_FILE
python3 << 'PYEOF'
import json

total = 0
failed = 0

try:
    with open('/home/ravikhichar9715/failover-k6-results.json') as f:
        for line in f:
            try:
                d = json.loads(line)
                if d.get('metric') == 'http_req_failed' and d.get('type') == 'Point':
                    total += 1
                    if d['data']['value'] == 1:
                        failed += 1
            except:
                pass
    success = total - failed
    print(f"Total requests:   {total}")
    print(f"Failed requests:  {failed}")
    print(f"Success requests: {success}")
    print(f"Success rate:     {(success/total*100):.2f}%")
    print(f"Error rate:       {(failed/total*100):.2f}%")
except Exception as e:
    print(f"Error: {e}")
PYEOF

# Cluster state
echo "" | tee -a $LOG_FILE
echo "=== FINAL CLUSTER STATE ===" | tee -a $LOG_FILE
patronictl -c /etc/patroni.yml list 2>/dev/null | tee -a $LOG_FILE

echo "" | tee -a $LOG_FILE
echo "=== COMPLETE! Log: $LOG_FILE ==="
