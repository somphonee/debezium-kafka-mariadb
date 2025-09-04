-- Create Debezium user with replication privileges
CREATE USER 'debezium'@'%' IDENTIFIED BY 'debeziumpass123';
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'debezium'@'%';
GRANT ALL PRIVILEGES ON appdb.* TO 'debezium'@'%';

-- Use the application database
USE appdb;

-- Create customers table
CREATE TABLE customers (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           first_name VARCHAR(100) NOT NULL,
                           last_name VARCHAR(100) NOT NULL,
                           email VARCHAR(255) UNIQUE NOT NULL,
                           phone VARCHAR(20),
                           address TEXT,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create orders table
CREATE TABLE orders (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        customer_id INT NOT NULL,
                        order_number VARCHAR(50) UNIQUE NOT NULL,
                        total_amount DECIMAL(10,2) NOT NULL,
                        status ENUM('pending', 'confirmed', 'shipped', 'delivered', 'cancelled') DEFAULT 'pending',
                        order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Insert sample customers
INSERT INTO customers (first_name, last_name, email, phone, address) VALUES
                                                                         ('John', 'Doe', 'john.doe@email.com', '+1-555-0101', '123 Main St, Anytown, USA'),
                                                                         ('Jane', 'Smith', 'jane.smith@email.com', '+1-555-0102', '456 Oak Ave, Somewhere, USA'),
                                                                         ('Bob', 'Johnson', 'bob.johnson@email.com', '+1-555-0103', '789 Pine Rd, Elsewhere, USA'),
                                                                         ('Alice', 'Williams', 'alice.williams@email.com', '+1-555-0104', '321 Elm St, Nowhere, USA');

-- Insert sample orders
INSERT INTO orders (customer_id, order_number, total_amount, status) VALUES
                                                                         (1, 'ORD-2024-001', 129.99, 'confirmed'),
                                                                         (2, 'ORD-2024-002', 79.50, 'pending'),
                                                                         (1, 'ORD-2024-003', 299.00, 'shipped'),
                                                                         (3, 'ORD-2024-004', 45.75, 'delivered'),
                                                                         (4, 'ORD-2024-005', 199.99, 'pending');

-- Show created data
SELECT 'Customers created:' as info;
SELECT * FROM customers;

SELECT 'Orders created:' as info;
SELECT * FROM orders;

-- Flush privileges to ensure all changes take effect
FLUSH PRIVILEGES;