package space.learn.co.la.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerEvent {
    private Integer id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String email;
    private String phone;
    private String address;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    // CDC metadata fields
    @JsonProperty("__op")
    private String operation; // c=create, u=update, d=delete, r=read (snapshot)

    @JsonProperty("__ts_ms")
    private Long timestamp;

    @JsonProperty("__source_db")
    private String sourceDb;

    @JsonProperty("__source_table")
    private String sourceTable;

    // Constructors
    public CustomerEvent() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

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
        return String.format("CustomerEvent{id=%d, name='%s %s', email='%s', operation='%s', timestamp=%s}",
                id, firstName, lastName, email, operation, getEventTimestamp());
    }
}