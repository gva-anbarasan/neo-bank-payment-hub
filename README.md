# NEO-BANK Payment Hub

A complete, production-ready distributed payment processing system. This project demonstrates a microservices architecture implementing key patterns like Saga, 
Outbox, and Idempotency, with real-time dashboards and event streaming.



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

## 🚀 Quick Start

### Prerequisites
- **Java 17** ([Download](https://adoptium.net/))
- **Maven** ([Download](https://maven.apache.org/download.cgi))
- **Docker Desktop** ([Download](https://www.docker.com/products/docker-desktop/))

### Installation & Setup

```bash
# 1. Clone the repository
git clone https://github.com/gva-anbarasan/neo-bank-payment-hub.git
cd neo-bank-payment-hub

# 2. Build all JAR files (this compiles all microservices)
mvn clean package -DskipTests

# 3. Start all services with Docker (13 containers total)
docker-compose up -d --build

# 4. Access the application
#    React Dashboard: http://localhost:3000
#    API Gateway: http://localhost:8080


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


🔌 Services & Ports
Service	      			Port	Description									 Related Q#
React UI	  			3000	Real-time transaction dashboard				    UI
API Gateway	  			8080	Entry point with fallback/circuit breaker	    Q8
Order Service			8081	Outbox pattern for reliable events		   		Q7
Payment Orchestrator	8082	Saga coordinator with idempotency				Q2, Q5
Fraud Engine			8083	Dynamic rules engine	                    	Q4, Q18
Ledger Worker			8084	Kafka consumer with manual commit + DLQ	    	Q10, Q12, Q14
Auth Service			8085	Policy-based access control (ABAC)	        	Q20
UI Backend				8086	WebSocket server for real-time stats	



🧪 Testing the System
1. Create a successful payment
bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-001" \
  -d '{"userId":"user123","amount":100,"currency":"USD"}'


2. Test Idempotency (Q2) - Same key returns "ALREADY_PROCESSED"
bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-001" \
  -d '{"userId":"user123","amount":100,"currency":"USD"}'


3. Test Fraud Detection (Q18) - Amount > $10,000
bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{"userId":"suspicious","amount":50000,"currency":"USD"}'


4. Check Real-time Stats (WebSocket - Q10/Q12)
bash
curl http://localhost:8086/api/stats



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



🔧 REST vs Kafka Trade-offs (Q6)
Aspect	Synchronous REST	Asynchronous Kafka
Latency	Low (immediate response)	Higher (eventual consistency)
Coupling	Tight (client knows service)	Loose (decoupled via events)
Ordering	Not guaranteed	Guaranteed per partition
Backpressure	Hard to implement	Built-in (consumer lag)
Transaction	Difficult across services	Outbox pattern + idempotency
Use Case	Real-time queries	Event-driven workflows


🛠 Troubleshooting
Docker Build Fails (lstat /target: no such file or directory)
Fix: Run mvn clean package -DskipTests before docker-compose up -d --build

Kafka Cluster ID Mismatch (Q13)
Fix: Reset volumes and restart




bash
docker-compose down -v && docker-compose up -d


CommitFailedException (Q13)
Fix: Increase max.poll.interval.ms in KafkaConsumerConfig.java

Port Already in Use
Fix: Change exposed ports in docker-compose.yml



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
