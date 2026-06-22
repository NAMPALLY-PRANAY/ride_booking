CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL
);

CREATE TABLE drivers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    vehicle_number VARCHAR(255) NOT NULL,
    available BOOLEAN NOT NULL
);

CREATE TABLE rides (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pickup_location VARCHAR(255) NOT NULL,
    drop_location VARCHAR(255) NOT NULL,
    fare DOUBLE NOT NULL,
    status VARCHAR(50) NOT NULL,
    customer_id BIGINT NOT NULL,
    driver_id BIGINT,
    CONSTRAINT fk_rides_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_rides_driver FOREIGN KEY (driver_id) REFERENCES drivers(id)
);
