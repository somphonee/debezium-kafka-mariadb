# Local CDC Stack with Debezium + Kafka + MariaDB + Spring Boot Consumer

A complete Change Data Capture (CDC) environment for local development using:
- **MariaDB** with binary logging enabled
- **Apache Kafka** for streaming CDC events
- **Debezium** for capturing database changes
- **Kafka UI** for browsing topics and messages
- **Spring Boot Consumer** for processing CDC events in Java

## Prerequisites

- Docker & Docker Compose
- Java 17+ (for Spring Boot consumer)
- Maven 3.6+ (for Spring Boot consumer)
- curl (for API calls)
- jq (optional, for pretty JSON output)

## Quick Start

### 1. Start the Infrastructure

```bash
# Start all services
docker compose up -d

# Or use the Makefile
make up
```

Wait for all services to be healthy (check with `docker compose ps` or `make status`).

### 2. Register the Debezium Connector

```bash
# Wait for services and register connector
make wait
make register-connector

# Or manually:
curl -X POST \
  -H "Content-Type: application/json" \
  -d @debezium/mariadb-connector.json \
  http://localhost:8083/connectors
```

### 3. Start the Spring Boot Consumer

```bash
# Build and run the consumer
cd spring-consumer
mvn clean package -DskipTests
mvn spring-boot:run

# Or using Makefile
make spring-consumer
```

### 4. Test CDC Events

```bash
# Insert test data
make test-data

# Update some records
make update-data

# Delete some records
make delete-data
```

## Service Endpoints

| Service | URL | Description |
|---------|-----|-------------|
| Kafka UI | http://localhost:18080 | Browse Kafka topics and messages |
| Kafka Connect | http://localhost:8083 | REST API for connectors |
| Spring Consumer | http://localhost:8080 | Health endpoint and metrics |
| MariaDB | localhost:3306 | Database (user: `root`, password: `rootpass123`) |

## CDC Topics

When the connector is registered, Debezium creates these topics:

- `mariadb01.appdb.customers` - Customer table changes
- `mariadb01.appdb.orders` - Order table changes
- `schema-changes.mariadb` - Schema change events

## Testing CDC Flow

### 1. Generate Test Data

```sql
-- Connect to MariaDB
docker compose exec mariadb mysql -u root -prootpass123 appdb

-- Insert new customers
INSERT INTO customers (first_name, last_name, email, phone, address) VALUES 
('Alice', 'Johnson', 'alice.j@example.com', '+1-555-2001', '789 Oak St'),
('Bob', 'Wilson', 'bob.w@example.com', '+1-555-2002', '456 Pine Ave'),
('Charlie', 'Brown', 'charlie.b@example.com', '+1-555-2003', '123 Elm Rd');

-- Insert new orders
INSERT INTO orders (customer_id, order_number, total_amount, status) VALUES 
(1, 'ORD-2024-100', 249.99, 'pending'),
(2, 'ORD-2024-101', 89.50, 'confirmed'),
(1, 'ORD-2024-102', 399.00, 'pending');
```

### 2. Update Records

```sql
-- Update customer information
UPDATE customers SET 
  email = 'alice.johnson.new@example.com',
  phone = '+1-555-2099'
WHERE first_name = 'Alice' AND last_name = 'Johnson';

-- Update order status
UPDATE orders SET 
  status = 'shipped',
  total_amount = 259.99
WHERE order_number = 'ORD-2024-100';
```

### 3. Delete Records

```sql
-- Delete an order
DELETE FROM orders WHERE order_number = 'ORD-2024-102';

-- Delete a customer (will cascade delete related orders)
DELETE FROM customers WHERE email = 'charlie.b@example.com';
```

### 4. View Results

Check the Spring Boot consumer logs to see the processed events:

```bash
# View consumer logs
make logs

# Or check Spring Boot logs directly
tail -f spring-consumer/spring-consumer.log
```

Browse the Kafka topics in Kafka UI at http://localhost:18080 to see the raw CDC messages.

## Debezium Event Structure

Each CDC event contains:
- **Envelope fields**: `op` (operation), `ts_ms` (timestamp), `source` (metadata)
- **Before**: Previous row state (for updates/deletes)
- **After**: New row state (for creates/updates)

Example customer create event:
```json
{
  "id": 5,
  "first_name": "Alice",
  "last_name": "Johnson",
  "email": "alice.j@example.com",
  "__op": "c",
  "__ts_ms": 1704067200000,
  "__source_db": "appdb",
  "__source_table": "customers"
}
```

Operations:
- `r` = Read (initial snapshot)
- `c` = Create (INSERT)
- `u` = Update (UPDATE)
- `d` = Delete (DELETE)

## Spring Boot Consumer Features

The Spring Boot consumer demonstrates:

1. **Kafka Integration**: Auto-configured with Spring Kafka
2. **Model Mapping**: POJOs for Customer and Order events
3. **Event Processing**: Separate handlers for each operation type
4. **Logging**: Structured logging for all CDC events
5. **Health Checks**: REST endpoints for monitoring
6. **Error Handling**: Graceful error handling and retry logic

### Consumer Architecture

```
Kafka Topics → @KafkaListener → Event Models → Business Logic
```

Key components:
- `CdcEventConsumer`: Main consumer with @KafkaListener methods
- `CustomerEvent`/`OrderEvent`: Domain models with CDC metadata
- `KafkaConfig`: Kafka consumer configuration
- `HealthController`: Health and status endpoints

## Management Commands

### Using Makefile

```bash
make help              # Show all available commands
make up                # Start all services
make down              # Stop all services
make logs              # Show logs
make reset             # Complete reset
make register-connector # Register Debezium connector
make check-connector   # Check connector status
make test-data         # Insert test data
make spring-consumer   # Run Spring Boot consumer
```

### Manual Commands

```bash
# Check connector status
curl -s http://localhost:8083/connectors/mariadb-connector/status | jq .

# List all connectors
curl -s http://localhost:8083/connectors | jq .

# Delete connector
curl -X DELETE http://localhost:8083/connectors/mariadb-connector

# Check Spring Boot consumer health
curl http://localhost:8080/health
```

## Troubleshooting

### Common Issues

1. **Connector fails to start**
   ```bash
   # Check MariaDB binlog configuration
   docker compose exec mariadb mysql -u root -prootpass123 -e "SHOW VARIABLES LIKE 'log_bin%';"
   
   # Verify Debezium user privileges
   docker compose exec mariadb mysql -u root -prootpass123 -e "SHOW GRANTS FOR 'debezium'@'%';"
   ```

2. **No CDC events appearing**
   ```bash
   # Check connector status
   make check-connector
   
   # Verify topics exist
   docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
   ```

3. **Port conflicts**
   - Edit `.env` file to change port mappings
   - Restart services: `make reset`

4. **Spring Boot consumer not receiving messages**
   ```bash
   # Check Kafka connectivity
   curl http://localhost:8080/health
   
   # Verify consumer group
   docker compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list
   ```

### Reset Environment

```bash
# Complete cleanup and restart
make reset
make register-connector
```

### View Raw Kafka Messages

```bash
# Consume messages directly from command line
docker compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic mariadb01.appdb.customers \
  --from-beginning
```

## Configuration

### Environment Variables (.env)

- `MARIADB_PORT`: MariaDB port (default: 3306)
- `KAFKA_PORT`: Kafka port (default: 9092)
- `KAFKA_UI_PORT`: Kafka UI port (default: 18080)
- `KAFKA_CONNECT_PORT`: Connect REST API port (default: 8083)
- `SPRING_BOOT_PORT`: Spring Boot consumer port (default: 8080)

### MariaDB Configuration (maria/my.cnf)

- Binary logging enabled with ROW format
- GTID mode for reliable replication
- Optimized for Debezium CDC

### Kafka Configuration

- Single broker setup for development
- Auto-create topics enabled
- Reasonable defaults for local testing

## Production Considerations

This setup is for **LOCAL DEVELOPMENT ONLY**. For production:

1. **Security**:
   - Enable TLS/SSL for all communications
   - Configure SASL authentication
   - Use proper secrets management
   - Secure network policies

2. **Scaling**:
   - Multi-broker Kafka cluster
   - Increase replication factors
   - Optimize connector configurations
   - Use Kafka Connect in distributed mode

3. **Monitoring**:
   - Add Prometheus/Grafana
   - Configure proper alerting
   - Monitor consumer lag
   - Track connector health

4. **Data Management**:
   - Configure topic retention policies
   - Implement schema registry
   - Plan for schema evolution
   - Set up backup strategies

## Advanced Usage

### Custom Event Processing

Extend the Spring Boot consumer to add your business logic:

```java
@Service
public class OrderEventProcessor {
    
    @EventListener
    public void handleOrderCreated(OrderEvent event) {
        if (event.isCreate()) {
            // Send notification email
            // Update inventory
            // Trigger fulfillment process
        }
    }
}
```

### Schema Evolution

When changing database schema:

1. Update table structure
2. Restart connector to capture schema changes
3. Update Spring Boot models accordingly
4. Deploy updated consumer

### Multiple Consumers

Scale processing by adding consumer instances:

```bash
# Run multiple consumer instances
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8081"
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8082"
```

Each instance will join the same consumer group and process different partitions.

## 📚 Resources & Documentation

### 🔗 Official Documentation

| Technology | Documentation | API Reference |
|------------|---------------|---------------|
| 🔄 **Debezium** | [debezium.io/documentation](https://debezium.io/documentation/) | [Connector Configs](https://debezium.io/documentation/reference/2.4/connectors/mysql.html) |
| 📊 **Apache Kafka** | [kafka.apache.org/documentation](https://kafka.apache.org/documentation/) | [Producer/Consumer APIs](https://kafka.apache.org/documentation/#api) |  
| ☕ **Spring Kafka** | [Spring Kafka Reference](https://docs.spring.io/spring-kafka/docs/current/reference/html/) | [Annotations Guide](https://docs.spring.io/spring-kafka/docs/current/reference/html/#kafka-listener-annotation) |
| 🗃️ **MariaDB** | [mariadb.com/kb/en/binary-log](https://mariadb.com/kb/en/binary-log/) | [Replication Setup](https://mariadb.com/kb/en/setting-up-replication/) |

### 📖 Learning Resources

#### 🎓 **Getting Started Guides**
- [CDC Pattern Overview](https://microservices.io/patterns/data/database-per-service.html)
- [Event-Driven Architecture](https://martinfowler.com/articles/201701-event-driven.html)
- [Kafka Streams Tutorial](https://kafka.apache.org/documentation/streams/)

#### 🛠️ **Hands-on Tutorials**
- [Debezium Tutorial](https://debezium.io/documentation/reference/tutorial.html)
- [Spring Boot + Kafka](https://spring.io/guides/gs/messaging-kafka/)
- [Kafka Connect Deep Dive](https://docs.confluent.io/platform/current/connect/index.html)

#### 📹 **Video Resources**
- [Debezium in Action (Confluent)](https://www.youtube.com/watch?v=QYbXDp4Vu-8)
- [Event Streaming with Kafka](https://developer.confluent.io/learn-kafka/)
- [Microservices with Spring Boot](https://spring.io/microservices)

### 🏗️ **Architecture Patterns**

#### 📊 **Event Sourcing**
```java
@Entity
public class CustomerAggregate {
    public void apply(CustomerCreatedEvent event) {
        this.id = event.getCustomerId();
        this.name = event.getName();
        this.version++;
    }
}
```

#### 🔄 **CQRS (Command Query Responsibility Segregation)**
```java
// Command side - write operations
@Component
public class CustomerCommandHandler {
    public void handle(CreateCustomerCommand cmd) {
        // Validate and persist
    }
}

// Query side - read operations  
@Component
public class CustomerQueryHandler {
    public CustomerView findById(Long id) {
        // Read from optimized view
    }
}
```

#### 📋 **Saga Pattern**
```java
@Component
public class OrderProcessingSaga {
    
    @SagaOrchestrationStart
    public void processOrder(OrderCreatedEvent event) {
        // Step 1: Reserve inventory
        // Step 2: Process payment
        // Step 3: Ship order
    }
    
    @SagaOrchestrationEnd
    public void completeOrder(OrderShippedEvent event) {
        // Saga completion
    }
}
```

### 🤝 **Community & Support**

#### 💬 **Forums & Chat**
- [Debezium Community](https://debezium.io/community/)
- [Kafka Community](https://kafka.apache.org/contact)
- [Spring Community](https://spring.io/community)
- [Stack Overflow Tags](https://stackoverflow.com/questions/tagged/debezium+kafka)

#### 🐛 **Issue Tracking**
- [Debezium Issues](https://github.com/debezium/debezium/issues)
- [Kafka JIRA](https://issues.apache.org/jira/projects/KAFKA)
- [Spring Boot Issues](https://github.com/spring-projects/spring-boot/issues)

### 🏆 **Best Practices Summary**

#### ✅ **Development**
- Start with single-node setup for local development
- Use Docker Compose for consistent environments
- Implement proper error handling and monitoring
- Test schema evolution scenarios early

#### ✅ **Production**
- Enable authentication and encryption
- Use multi-broker Kafka clusters
- Implement comprehensive monitoring
- Plan for disaster recovery scenarios

#### ✅ **Operations** 
- Monitor consumer lag continuously
- Set up alerting for connector failures
- Implement log aggregation and analysis
- Regular backup and recovery testing

---

## 🎉 **Ready to Get Started?**

```bash
# 🚀 One command to rule them all!
make demo
```

Then open [http://localhost:18080](http://localhost:18080) and start exploring your CDC events! 🎊

---

> 💡 **Pro Tip**: Bookmark this README and the Makefile commands - they'll be your best friends when working with this CDC stack!

**Happy Streaming!** 🚀📊☕