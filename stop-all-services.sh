#!/bin/bash

echo "Stopping all NEO-BANK services..."

cd /c/MAY-ASSES/neo-bank-payment-hub/logs

for pid_file in *.pid; do
    if [ -f "$pid_file" ]; then
        pid=$(cat "$pid_file")
        if kill -0 $pid 2>/dev/null; then
            echo "Stopping process $pid..."
            kill $pid
        fi
        rm "$pid_file"
    fi
done

echo "All services stopped."