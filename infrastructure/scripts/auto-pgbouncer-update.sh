#!/bin/bash
while true; do
  sudo bash /etc/pgbouncer/update-leader.sh > /dev/null 2>&1
  sleep 5
done
