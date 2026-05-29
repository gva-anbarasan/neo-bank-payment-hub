#!/bin/bash

echo "Starting NEO-BANK Payment Hub Services..."

# Set Java path
export JAVA_HOME="C:/SWSetup/java17/jdk-17.0.19+10"
export PATH="$JAVA_HOME/bin:$PATH"

# Go to project root
cd /c/MAY-ASSES/neo-bank-payment-hub

# Create logs directory if not exists
mkdir -p logs

echo "Starting services in background..."

# Start Order Service
cd order-service
nohup java -jar target/order-service-1.0.0.jar > ../logs/order-service.log 2>&1 &
echo $! > ../logs/order-service.pid
echo "Order Service started (PID: $!)"
cd ..

# Start Payment Orchestrator
cd payment-orchestrator
nohup java -jar target/payment-orchestrator-1.0.0.jar > ../logs/payment-orchestrator.log 2>&1 &
echo $! > ../logs/payment-orchestrator.pid
echo "Payment Orchestrator started (PID: $!)"
cd ..

# Start Fraud Engine
cd fraud-engine
nohup java -jar target/fraud-engine-1.0.0.jar > ../logs/fraud-engine.log 2>&1 &
echo $! > ../logs/fraud-engine.pid
echo "Fraud Engine started (PID: $!)"
cd ..

# Start Ledger Worker
cd ledger-worker
nohup java -jar target/ledger-worker-1.0.0.jar > ../logs/ledger-worker.log 2>&1 &
echo $! > ../logs/ledger-worker.pid
echo "Ledger Worker started (PID: $!)"
cd ..

# Start Auth Service
cd auth-service
nohup java -jar target/auth-service-1.0.0.jar > ../logs/auth-service.log 2>&1 &
echo $! > ../logs/auth-service.pid
echo "Auth Service started (PID: $!)"
cd ..

# Start API Gateway
cd api-gateway
nohup java -jar target/api-gateway-1.0.0.jar > ../logs/api-gateway.log 2>&1 &
echo $! > ../logs/api-gateway.pid
echo "API Gateway started (PID: $!)"
cd ..

# Start UI Backend
cd services/ui-backend
nohup java -jar target/ui-backend-1.0.0.jar > ../../logs/ui-backend.log 2>&1 &
echo $! > ../../logs/ui-backend.pid
echo "UI Backend started (PID: $!)"
cd ../..

echo ""
echo "========================================="
echo "All services started!"
echo "========================================="
echo ""
echo "Logs location: ./logs/"
echo "View logs: tail -f logs/order-service.log"
echo ""
echo "Stop all services: ./stop-all-services.sh"
echo ""
echo "Start React UI (new terminal):"
echo "  cd ui && npm install && npm start"