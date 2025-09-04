package space.learn.co.la.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderEvent {
    private Integer id;

    @JsonProperty("customer_id")
    private Integer customerId;

    @JsonProperty("order_number")
    private String orderNumber;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    private String status;

    @JsonProperty("order_date")
    private String orderDate;

    @JsonProperty("updated_at")
    private String updatedAt;

    // CDC metadata fields
    @JsonProperty("__op")
    private String operation;

    @JsonProperty("__ts_ms")
    private Long timestamp;

    @JsonProperty("__source_db")
    private String sourceDb;

    @JsonProperty("__source_table")
    private String sourceTable;

    // Constructors
    public OrderEvent() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public String getSourceDb() { return sourceDb; }
    public void setSourceDb(String sourceDb) { this.sourceDb = sourceDb; }

    public String getSourceTable() { return sourceTable; }
    public void setSourceTable(String sourceTable) { this.sourceTable = sourceTable; }

    // Helper methods
    public boolean isCreate() { return "c".equals(operation); }
    public boolean isUpdate() { return "u".equals(operation); }
    public boolean isDelete() { return "d".equals(operation); }
    public boolean isRead() { return "r".equals(operation); }

    public Instant getEventTimestamp() {
        return timestamp != null ? Instant.ofEpochMilli(timestamp) : null;
    }

    @Override
    public String toString() {
        return String.format("OrderEvent{id=%d, orderNumber='%s', customerId=%d, amount=%s, status='%s', operation='%s', timestamp=%s}",
                id, orderNumber, customerId, totalAmount, status, operation, getEventTimestamp());
    }
}
