package space.learn.co.la.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import space.learn.co.la.model.CustomerEvent;
import space.learn.co.la.model.OrderEvent;

@Service
public class CdcEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CdcEventConsumer.class);
    private final ObjectMapper objectMapper;

    public CdcEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "mariadb01.appdb.customers",
            groupId = "cdc-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCustomerEvent(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            logger.info("Received customer event from topic: {}, partition: {}, offset: {}", topic, partition, offset);

            CustomerEvent customerEvent = objectMapper.readValue(payload, CustomerEvent.class);

            switch (customerEvent.getOperation()) {
                case "r":
                    logger.info("Customer snapshot: {}", customerEvent);
                    handleCustomerSnapshot(customerEvent);
                    break;
                case "c":
                    logger.info("Customer created: {}", customerEvent);
                    handleCustomerCreated(customerEvent);
                    break;
                case "u":
                    logger.info("Customer updated: {}", customerEvent);
                    handleCustomerUpdated(customerEvent);
                    break;
                case "d":
                    logger.info("Customer deleted: {}", customerEvent);
                    handleCustomerDeleted(customerEvent);
                    break;
                default:
                    logger.warn("Unknown operation for customer event: {}", customerEvent.getOperation());
            }

        } catch (Exception e) {
            logger.error("Error processing customer event: {}", payload, e);
        }
    }

    @KafkaListener(
            topics = "mariadb01.appdb.orders",
            groupId = "cdc-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderEvent(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            logger.info("Received order event from topic: {}, partition: {}, offset: {}", topic, partition, offset);

            OrderEvent orderEvent = objectMapper.readValue(payload, OrderEvent.class);

            switch (orderEvent.getOperation()) {
                case "r":
                    logger.info("Order snapshot: {}", orderEvent);
                    handleOrderSnapshot(orderEvent);
                    break;
                case "c":
                    logger.info("Order created: {}", orderEvent);
                    handleOrderCreated(orderEvent);
                    break;
                case "u":
                    logger.info("Order updated: {}", orderEvent);
                    handleOrderUpdated(orderEvent);
                    break;
                case "d":
                    logger.info("Order deleted: {}", orderEvent);
                    handleOrderDeleted(orderEvent);
                    break;
                default:
                    logger.warn("Unknown operation for order event: {}", orderEvent.getOperation());
            }

        } catch (Exception e) {
            logger.error("Error processing order event: {}", payload, e);
        }
    }

    // Customer event handlers
    private void handleCustomerSnapshot(CustomerEvent event) {
        // Handle initial snapshot data
        logger.debug("Processing customer snapshot for ID: {}", event.getId());
        // Your business logic here (e.g., sync to cache, update search index, etc.)
    }

    private void handleCustomerCreated(CustomerEvent event) {
        // Handle new customer creation
        logger.debug("Processing new customer: {} {}", event.getFirstName(), event.getLastName());
        // Your business logic here (e.g., send welcome email, update analytics, etc.)
    }

    private void handleCustomerUpdated(CustomerEvent event) {
        // Handle customer updates
        logger.debug("Processing customer update for ID: {}", event.getId());
        // Your business logic here (e.g., update cache, trigger notifications, etc.)
    }

    private void handleCustomerDeleted(CustomerEvent event) {
        // Handle customer deletion
        logger.debug("Processing customer deletion for ID: {}", event.getId());
        // Your business logic here (e.g., cleanup related data, send notifications, etc.)
    }

    // Order event handlers
    private void handleOrderSnapshot(OrderEvent event) {
        // Handle initial snapshot data
        logger.debug("Processing order snapshot for order: {}", event.getOrderNumber());
        // Your business logic here
    }

    private void handleOrderCreated(OrderEvent event) {
        // Handle new order creation
        logger.debug("Processing new order: {} for customer: {}", event.getOrderNumber(), event.getCustomerId());
        // Your business logic here (e.g., inventory management, payment processing, etc.)
    }

    private void handleOrderUpdated(OrderEvent event) {
        // Handle order updates
        logger.debug("Processing order update: {} - Status: {}", event.getOrderNumber(), event.getStatus());
        // Your business logic here (e.g., status notifications, fulfillment triggers, etc.)
    }

    private void handleOrderDeleted(OrderEvent event) {
        // Handle order deletion
        logger.debug("Processing order deletion: {}", event.getOrderNumber());
        // Your business logic here (e.g., refund processing, inventory adjustment, etc.)
    }
}