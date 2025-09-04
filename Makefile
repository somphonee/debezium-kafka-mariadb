.PHONY: help up down logs clean reset status test-data register-connector check-connector spring-consumer

# Default target
help:
	@echo "Available commands:"
	@echo "  up              - Start all services"
	@echo "  down            - Stop all services"
	@echo "  logs            - Show logs for all services"
	@echo "  clean           - Stop services and remove volumes"
	@echo "  reset           - Complete reset (stop, clean, start)"
	@echo "  status          - Show service status"
	@echo "  register-connector - Register Debezium connector"
	@echo "  check-connector - Check connector status"
	@echo "  test-data       - Insert test data into MariaDB"
	@echo "  spring-consumer - Build and run Spring Boot consumer"

# Start all services
up:
	docker compose up -d

# Stop all services
down:
	docker compose down

# Show logs
logs:
	docker compose logs -f

# Show logs for specific service
logs-mariadb:
	docker compose logs -f mariadb

logs-kafka:
	docker compose logs -f kafka

logs-connect:
	docker compose logs -f connect

logs-ui:
	docker compose logs -f kafka-ui

# Clean up (remove volumes)
clean:
	docker compose down -v
	docker system prune -f

# Complete reset
reset: clean up
	@echo "Environment reset complete"

# Check service status
status:
	docker compose ps

# Wait for services to be ready
wait:
	@echo "Waiting for services to be ready..."
	@until docker compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 >/dev/null 2>&1; do \
		echo "Waiting for Kafka..."; \
		sleep 5; \
	done
	@until curl -f http://localhost:8083/connectors >/dev/null 2>&1; do \
		echo "Waiting for Kafka Connect..."; \
		sleep 5; \
	done
	@echo "All services are ready!"

# Register Debezium connector
register-connector: wait
	curl -X POST \
		-H "Content-Type: application/json" \
		-d @debezium/mariadb-connector.json \
		http://localhost:8083/connectors
	@echo "\nConnector registered!"

# Check connector status
check-connector:
	@echo "Connector status:"
	curl -s http://localhost:8083/connectors/mariadb-connector/status | jq .
	@echo "\n\nConnector config:"
	curl -s http://localhost:8083/connectors/mariadb-connector/config | jq .

# List all connectors
list-connectors:
	@echo "Available connectors:"
	curl -s http://localhost:8083/connectors | jq .

# Delete connector
delete-connector:
	curl -X DELETE http://localhost:8083/connectors/mariadb-connector
	@echo "Connector deleted!"

# Insert test data
test-data:
	docker compose exec mariadb mysql -u root -prootpass123 appdb -e "\
		INSERT INTO customers (first_name, last_name, email, phone, address) VALUES \
		('Test', 'User1', 'test1@example.com', '+1-555-1001', '100 Test St'); \
		INSERT INTO customers (first_name, last_name, email, phone, address) VALUES \
		('Test', 'User2', 'test2@example.com', '+1-555-1002', '200 Test Ave'); \
		INSERT INTO orders (customer_id, order_number, total_amount, status) VALUES \
		(1, 'ORD-TEST-001', 99.99, 'pending');"
	@echo "Test data inserted!"

# Update test data
update-data:
	docker compose exec mariadb mysql -u root -prootpass123 appdb -e "\
		UPDATE customers SET email='updated@example.com' WHERE first_name='Test' AND last_name='User1'; \
		UPDATE orders SET status='confirmed', total_amount=149.99 WHERE order_number='ORD-TEST-001';"
	@echo "Test data updated!"

# Delete test data
delete-data:
	docker compose exec mariadb mysql -u root -prootpass123 appdb -e "\
		DELETE FROM orders WHERE order_number='ORD-TEST-001'; \
		DELETE FROM customers WHERE email='updated@example.com';"
	@echo "Test data deleted!"

# Show MariaDB data
show-data:
	@echo "Customers:"
	docker compose exec mariadb mysql -u root -prootpass123 appdb -e "SELECT * FROM customers;"
	@echo "\nOrders:"
	docker compose exec mariadb mysql -u root -prootpass123 appdb -e "SELECT * FROM orders;"

# Open MariaDB shell
db-shell:
	docker compose exec mariadb mysql -u root -prootpass123 appdb

# Build Spring Boot consumer
spring-build:
	cd spring-consumer && mvn clean package -DskipTests

# Run Spring Boot consumer
spring-consumer:
	cd spring-consumer && mvn spring-boot:run

# Run Spring Boot consumer in background
spring-consumer-bg:
	cd spring-consumer && nohup mvn spring-boot:run > spring-consumer.log 2>&1 &
	@echo "Spring Boot consumer started in background. Check spring-consumer.log for logs."

# Stop Spring Boot consumer
spring-consumer-stop:
	pkill -f "spring-boot:run" || true
	@echo "Spring Boot consumer stopped."

# Full demo setup
demo: up wait register-connector spring-consumer-bg
	@echo "Demo environment is ready!"
	@echo "- Kafka UI: http://localhost:18080"
	@echo "- Spring Boot Consumer: http://localhost:8080"
	@echo "- Run 'make test-data' to generate CDC events"

# Cleanup demo
demo-clean: spring-consumer-stop clean
	@echo "Demo environment cleaned up!"