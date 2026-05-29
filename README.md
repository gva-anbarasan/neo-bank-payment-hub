# NEO-BANK Payment Hub

A production-ready distributed payment processing system with real-time dashboard, fraud detection, and idempotent transactions.

## 🚀 One-Command Setup

```bash
# Clone and run everything
git clone https://github.com/gva-anbarasan/neo-bank-payment-hub.git
cd neo-bank-payment-hub
mvn clean package -DskipTests
docker-compose up -d --build
***********************************
That's it! Access: http://localhost:3000

************************************
📋 Pre-Setup Requirements
Tool	Version	Check Command
Java	17+	java -version
Maven	3.8+	mvn -version
Docker	20.10+	docker --version
Git	Latest	git --version

***************************************************
## Kafka Topics

The system requires the following topics to be created before running:

| Topic | Partitions | Replication Factor | Description |
|-------|------------|-------------------|-------------|
| `ORDER_CREATED` | 3 | 1 | Emitted when a new order is created (Outbox pattern) |
| `PAYMENT_SUCCESS` | 3 | 1 | Emitted when a payment is successfully processed |
| `PAYMENT_FAILED` | 3 | 1 | Emitted when a payment fails (triggers DLQ) |

### Create Topics (Automatically handled by docker-compose)

```bash
# Create ORDER_CREATED topic
docker exec -it neo-bank-kafka kafka-topics --create \
  --topic ORDER_CREATED --bootstrap-server localhost:9092 \
  --partitions 3 --replication-factor 1

# Create PAYMENT_SUCCESS topic
docker exec -it neo-bank-kafka kafka-topics --create \
  --topic PAYMENT_SUCCESS --bootstrap-server localhost:9092 \
  --partitions 3 --replication-factor 1

# Create PAYMENT_FAILED topic
docker exec -it neo-bank-kafka kafka-topics --create \
  --topic PAYMENT_FAILED --bootstrap-server localhost:9092 \
  --partitions 3 --replication-factor 1

# Verify all topics
docker exec -it neo-bank-kafka kafka-topics --bootstrap-server localhost:9092 --list

*******************************

***************************
🎯 Quick Test (Copy-Paste)
# 1. Check all services
docker ps --format "table {{.Names}}\t{{.Status}}"

# 2. Create a payment
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-001" \
  -d '{"userId":"user1","amount":100,"currency":"USD"}'

# 3. Test idempotency (same payment - gets rejected)
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-001" \
  -d '{"userId":"user1","amount":100,"currency":"USD"}'

# 4. Test fraud detection ($50,000 triggers fraud)
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{"userId":"suspicious","amount":50000,"currency":"USD"}'

# 5. View real-time stats
curl http://localhost:8086/api/stats

***********************************
📊 Services & Ports
Service	Port	What it does
Dashboard UI	3000	Real-time transaction monitor
API Gateway	8080	Entry point (use this for all API calls)
Order Service	8081	Creates orders with Outbox pattern
Payment Orchestrator	8082	Saga coordinator + idempotency
Fraud Engine	8083	Dynamic fraud detection rules
Ledger Worker	8084	Kafka consumer with DLQ
Auth Service	8085	Policy-based access control
UI Backend	8086	WebSocket stats server
*************************************


## 📋 Coding Test Requirements - All 20 Questions Covered

| Section | Q# | Requirement | Implementation Location |
|:-------:|:--:|:------------|:------------------------|
| **1** | Q1 | Thread-safe in-memory idempotency store | `common/src/main/java/.../idempotency/DistributedIdempotencyStore.java` |
| | Q2 | Prevent double-processing in distributed service | `payment-orchestrator/.../saga/PaymentSagaOrchestrator.java` (idempotency check) |
| | Q3 | Retry + exponential backoff utility | `common/src/main/java/.../retry/RetryUtils.java` |
| | Q4 | Pluggable rule evaluators abstraction | `fraud-engine/.../engine/DynamicFraudRulesEngine.java` (Rule interface) |

| **2** | Q5 | Payment workflow using Saga | `payment-orchestrator/.../saga/PaymentSagaOrchestrator.java` |
| | Q6 | REST vs Kafka trade-offs | `README.md` (this section) + Architecture design |
| | Q7 | Outbox pattern publisher | `order-service/.../service/OrderService.java` (Outbox polling) |
| | Q8 | Resilient API Gateway fallback | `api-gateway/.../controller/PaymentController.java` (fallback handling) |

| **3** | Q9 | Kafka consumer groups scaling & ordering | `ledger-worker/.../config/KafkaConsumerConfig.java` |
| | Q10 | Spring Kafka consumer with manual ack | `ledger-worker/.../consumer/PaymentConsumer.java` |
| | Q11 | Auto-commit risks explanation | See `docs/KAFKA_DESIGN.md` |
| | Q12 | Batch processing + commit after success | `ledger-worker/.../consumer/PaymentConsumer.java` (batch listener) |
| | Q13 | CommitFailedException causes & fix | `ledger-worker/.../config/KafkaConsumerConfig.java` (max.poll.interval.ms) |
| | Q14 | Retry + DLQ strategy | `ledger-worker/.../consumer/PaymentConsumer.java` (@RetryableTopic + @DltHandler) |
| | Q15 | Preserve ordering while scaling | Partition by key + consistent hashing strategy |

| **4** | Q16 | Generic MQ consumer contract | `common/src/main/java/.../mq/MessageConsumerContract.java` |
| | Q17 | Prevent slow consumer blocking | Thread pools + bounded queues with backpressure |


| **5** | Q18 | Dynamic rules library for access decisions | `fraud-engine/.../engine/DynamicFraudRulesEngine.java` |
| | Q19 | Risks of storing rules in database | See `docs/RULES_ENGINE_DESIGN.md` (caching, security, performance) |

| **6** | Q20 | Policy-based dynamic access control | `auth-service/.../access/DynamicAccessController.java` |


***********************************

Architecture Overview:
Client Request → API Gateway (Fallback/Circuit Breaker)
                      ↓
              Order Service (Outbox Pattern)
                      ↓
              Kafka Topics (ORDER_CREATED, PAYMENT_SUCCESS, PAYMENT_FAILED)
                      ↓
         ┌───────────┼───────────┐
         ↓           ↓           ↓
  Payment Saga  Fraud Engine  Ledger Worker
  (Orchestrator) (Rules)      (Consumer + DLQ)
         ↓           ↓           ↓
    Wallet/Fraud  Redis Cache  PostgreSQL
	


**********************************





📁 Project Structure
text
neo-bank-payment-hub/
├── api-gateway/             # Q8: Fallback + circuit breaker
├── auth-service/            # Q20: Policy-based ABAC
├── common/                  # Q1, Q3, Q4, Q16: Idempotency, Retry, Rules, MQ Contract
├── fraud-engine/            # Q4, Q18, Q19: Dynamic rules engine
├── ledger-worker/           # Q9-Q15: Kafka consumer, manual commit, DLQ
├── order-service/           # Q7: Outbox pattern
├── payment-orchestrator/    # Q2, Q5: Saga + idempotency
├── services/ui-backend/     # WebSocket server for real-time stats
├── ui/                      # React dashboard
├── scripts/                 # Helper scripts (create topics, init DB)
├── docker-compose.yml       # Complete deployment (13 containers)
└── pom.xml                  # Parent Maven configuration

# All 13 containers should be running
docker ps | wc -l  # Should show 13+

# All services should respond
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
curl http://localhost:8086/actuator/health

**********************************************

🎉 Success Criteria
You'll know it's working when:

✅ Dashboard loads at http://localhost:3000

✅ All 13 Docker containers show "Up" status

✅ Payment API returns success

✅ Idempotency prevents duplicate payments

✅ Fraud detection rejects large amounts (>$10,000)

✅ WebSocket shows real-time stats
*****************************************


📊 Key Implementation Highlights
Requirement	Implementation Detail
Q1 (Idempotency)	Redis SETNX with TTL, thread-safe using Jedis pool
Q3 (Retry)	Exponential backoff: 1000ms * 2^(attempt-1) with max 30s cap
Q5 (Saga)	Orchestrator pattern with compensation actions
Q7 (Outbox)	Polling publisher with @Scheduled, transactional boundary
Q10 (Manual Ack)	Acknowledgment.acknowledge() after successful processing
Q12 (Batch)	@KafkaListener(batch = "true") + commit after full batch
Q14 (DLQ)	@RetryableTopic + @DltHandler for failed messages
Q18 (Rules)	Priority-based conflict resolution, explainable decisions
Q20 (ABAC)	Time-based, location-based, role-based policies


📝 License
This project was developed as a coding test submission. All 20 questions from the Detailed Interviewer Guide have been implemented.



Author: Anbarasan (gva.anbarasan@gmail.com)
Repository: https://github.com/gva-anbarasan/neo-bank-payment-hub



## 📋 Coding Test Coverage - All 20 Questions

| # | Requirement | Implementation | Key Code |
|:-:|:------------|:---------------|:---------|
| **Section 1: Java Concurrency, Reliability & Design** ||||
| Q1 | Thread-safe idempotency store | `DistributedIdempotencyStore.java` | `tryAcquire()` / `release()` |
| Q2 | Prevent double-processing | `PaymentSagaOrchestrator.java` | Idempotency key check |
| Q3 | Retry + exponential backoff | `RetryUtils.java` | `retry()` with backoff |
| Q4 | Pluggable rule evaluators | `DynamicFraudRulesEngine.java` | `FraudRule` interface |
| **Section 2: Microservices Architecture** ||||
| Q5 | Saga payment workflow | `PaymentSagaOrchestrator.java` | `execute()` / `compensate()` |
| Q6 | REST vs Kafka trade-offs | `README.md` | Decision table below |
| Q7 | Outbox pattern publisher | `OrderService.java` | `publishOutboxEvents()` |
| Q8 | API Gateway fallback | `PaymentController.java` | Fallback response |
| **Section 3: Kafka Deep Dive** ||||
| Q9 | Consumer groups & ordering | `KafkaConsumerConfig.java` | Partition config |
| Q10 | Manual acknowledgment | `PaymentConsumer.java` | `Acknowledgment.acknowledge()` |
| Q11 | Auto-commit risks | `KafkaConsumerConfig.java` | Disabled auto-commit |
| Q12 | Batch commit after success | `PaymentConsumer.java` | `@KafkaListener(batch = "true")` |
| Q13 | CommitFailedException fix | `KafkaConsumerConfig.java` | `max.poll.interval.ms` |
| Q14 | Retry + DLQ strategy | `PaymentConsumer.java` | `@RetryableTopic` + `@DltHandler` |
| Q15 | Ordering while scaling | `KafkaConsumerConfig.java` | Partition key strategy |
| **Section 4: MQ Consumer** ||||
| Q16 | Generic MQ consumer contract | `MessageConsumerContract.java` | Interface contract |
| Q17 | Prevent slow consumer blocking | `MessageConsumerContract.java` | Thread pools + backpressure |
| **Section 5: Rules Engine** ||||
| Q18 | Dynamic rules library | `DynamicFraudRulesEngine.java` | Priority conflict resolution |
| Q19 | Risks of DB-stored rules | `DynamicFraudRulesEngine.java` | Caching + sandboxing |
| **Section 6: Access Control** ||||
| Q20 | Policy-based access control | `DynamicAccessController.java` | Time/Location/Role policies |


Simpler - Minimal Table
## ✅ 20 Questions - Implementation Map

| Section | Questions | Implementation Location |
|:--------|:----------|:------------------------|
| **1** | Q1-Q4 | `common/` + `payment-orchestrator/` |
| **2** | Q5-Q8 | `payment-orchestrator/` + `order-service/` + `api-gateway/` |
| **3** | Q9-Q15 | `ledger-worker/` |
| **4** | Q16-Q17 | `common/mq/` |
| **5** | Q18-Q19 | `fraud-engine/` |
| **6** | Q20 | `auth-service/` |

**********************************************
## 📊 Final :Live Dashboard

![Neo-Bank Payment Hub Dashboard](docs/images/dashboard-screenshot.png)

**System Metrics:**
- ✅ All 13 services running
- ✅ Real-time monitoring active  
- ✅ Idempotency enabled
- ⚡ Success Rate: 100%
- 🔄 Ready for transaction processing

***********************************************
📝 Author
Anbarasan - gva.anbarasan@gmail.com

GitHub: https://github.com/gva-anbarasan/neo-bank-payment-hub
