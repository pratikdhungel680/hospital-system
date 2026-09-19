-- Hospital Management System — Database Schema
-- Import this file in phpMyAdmin (or `mysql -u root -p < hospital_db.sql`) before running the app.

CREATE DATABASE IF NOT EXISTS hospital_db;
USE hospital_db;

CREATE TABLE IF NOT EXISTS patients (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    disease VARCHAR(100) NOT NULL,
    gender VARCHAR(10) DEFAULT '',
    contact VARCHAR(20) DEFAULT ''
);

CREATE TABLE IF NOT EXISTS appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor VARCHAR(100) NOT NULL,
    appointment_date DATE NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STAFF'
);

-- Default admin login: username "admin", password "admin123"
-- (Change this password after first login, or just delete this row and use
--  the Register button on the login screen to create your own account.)
INSERT INTO users (username, password_hash, salt, role)
VALUES ('admin', 'vN9mq4pNw2E9DvUKNnnklRm/ef0fWk/pwWeN0c4dllM=', 'MTIzNDU2Nzg5MGFiY2RlZg==', 'ADMIN')
ON DUPLICATE KEY UPDATE username = username;
