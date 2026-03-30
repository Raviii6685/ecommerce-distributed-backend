#!/bin/bash

ETCD_ENDPOINT="http://10.128.0.2:2379"
PGBOUNCER_INI="/etc/pgbouncer/pgbouncer.ini"

# etcd se Leader name fetch karo
LEADER=$(etcdctl --endpoints=$ETCD_ENDPOINT get /db/postgres-cluster/leader 2>/dev/null | tr -d '[:space:]')

if [ -z "$LEADER" ]; then
    echo "No leader found in etcd!"
    exit 1
fi

echo "Leader found: $LEADER"

# Leader ka IP fetch karo
LEADER_DATA=$(etcdctl --endpoints=$ETCD_ENDPOINT get /db/postgres-cluster/members/$LEADER 2>/dev/null)
LEADER_IP=$(echo $LEADER_DATA | python3 -c "import sys,json; d=json.loads(sys.stdin.read()); print(d['conn_url'].split('//')[1].split(':')[0])")

echo "Leader IP: $LEADER_IP"

# PgBouncer config update karo
sudo sed -i "s|host=[0-9.]*|host=$LEADER_IP|g" $PGBOUNCER_INI

# PgBouncer restart karo — reload nahi kaam karta!
sudo pkill pgbouncer 2>/dev/null
sleep 1
sudo -u postgres pgbouncer -d $PGBOUNCER_INI

echo "PgBouncer updated to Leader: $LEADER_IP"
