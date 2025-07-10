-- SQL Server Database Setup Script for Parking Counter App

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    account_non_expired BOOLEAN DEFAULT TRUE NOT NULL,
    account_non_locked BOOLEAN DEFAULT TRUE NOT NULL,
    credentials_non_expired BOOLEAN DEFAULT TRUE NOT NULL
);

-- Create entry table
CREATE TABLE IF NOT EXISTS entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gate VARCHAR(255) NOT NULL,
    entry_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_entry_timestamp ON entry(timestamp);
CREATE INDEX IF NOT EXISTS idx_entry_gate ON entry(gate);
CREATE INDEX IF NOT EXISTS idx_entry_is_deleted ON entry(is_deleted);
CREATE INDEX IF NOT EXISTS idx_entry_created_by ON entry(created_by);

-- Insert Default Users (passwords are BCrypt encoded for 'admin' and 'user')
INSERT INTO users (username, password, role) VALUES
('admin', '$2a$12$CSsdDU/A4c8SNezoUlHZSeH3pyImkvpdi29sP6lBMcD1MgwAg9rAq', 'ADMIN'),
('user', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper1', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper2', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper3', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper4', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper5', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper6', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper7', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper8', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper9', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper10', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper11', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper12', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper13', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper14', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper15', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper16', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper17', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper18', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper19', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
('gatekeeper20', '$2a$12$J05/3HjQl6glDv2Sku37b.8Er6qW7SpDdoOYNtFdj0unByxHf/meS', 'USER');
