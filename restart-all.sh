#!/bin/bash

# Stop and remove all containers
echo "Stopping all containers..."
docker stop $(docker ps -aq) 2>/dev/null
docker rm $(docker ps -aq) 2>/dev/null

# Create network
docker network create neo-bank-network 2>/dev/null

# Start infrastructure
echo "Starting infrastructure services..."
docker run -d --name neo-bank-postgres --network neo-bank-network -p 5432:5432 -e POSTGRES_DB=neobank -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:15-alpine
docker run -d --name neo-bank-redis --network neo-bank-network -p 6379:6379 redis:7-alpine
docker run -d --name neo-bank-rabbitmq --network neo-bank-network -p 5672:5672 -p 15672:15672 rabbitmq:3.12-management-alpine
docker run -d --name neo-bank-zookeeper --network neo-bank-network -e ZOOKEEPER_CLIENT_PORT=2181 confluentinc/cp-zookeeper:7.4.0

# Wait for Zookeeper
sleep 10

# Start Kafka
echo "Starting Kafka..."
docker run -d --name neo-bank-kafka --network neo-bank-network -p 9092:9092 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://neo-bank-kafka:9092,PLAINTEXT://localhost:9092 -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 confluentinc/cp-kafka:7.4.0

# Wait for Kafka
sleep 15

# Start services
echo "Starting microservices..."
docker run -d --name neo-bank-auth-service --network neo-bank-network -p 8085:8085 -e spring.kafka.bootstrap-servers=neo-bank-kafka:9092 neo-bank-payment-hub-auth-service:latest
docker run -d --name neo-bank-api-gateway --network neo-bank-network -p 8080:8080 -e spring.kafka.bootstrap-servers=neo-bank-kafka:9092 neo-bank-payment-hub-api-gateway:latest
docker run -d --name neo-bank-order-service --network neo-bank-network -p 8081:8081 -e spring.kafka.bootstrap-servers=neo-bank-kafka:9092 neo-bank-payment-hub-order-service:latest
docker run -d --name neo-bank-payment-orchestrator --network neo-bank-network -p 8082:8082 -e spring.kafka.bootstrap-servers=neo-bank-kafka:9092 neo-bank-payment-hub-payment-orchestrator:latest
docker run -d --name neo-bank-fraud-engine --network neo-bank-network -p 8083:8083 -e spring.kafka.bootstrap-servers=neo-bank-kafka:9092 neo-bank-payment-hub-fraud-engine:latest
docker run -d --name neo-bank-ledger-worker --network neo-bank-network -p 8084:8084 -e spring.kafka.bootstrap-servers=neo-bank-kafka:9092 neo-bank-payment-hub-ledger-worker:latest
docker run -d --name neo-bank-ui-backend --network neo-bank-network -p 8086:8086 -e spring.kafka.bootstrap-servers=neo-bank-kafka:9092 neo-bank-payment-hub-ui-backend:latest
docker run -d --name neo-bank-ui --network neo-bank-network -p 3000:80 neo-bank-payment-hub-ui:latest

echo "All services started!"
echo "Check status with: docker ps"
echo "Access UI at: http://localhost:3000"
