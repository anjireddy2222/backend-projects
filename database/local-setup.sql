-- Run as a MySQL administrator when using an existing local MySQL installation.
-- These credentials are for a local classroom database only.
CREATE DATABASE IF NOT EXISTS amazon_catalog CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS amazon_customer CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS helpdesk CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'student'@'localhost' IDENTIFIED BY 'student-local-password';
GRANT ALL PRIVILEGES ON amazon_catalog.* TO 'student'@'localhost';
GRANT ALL PRIVILEGES ON amazon_customer.* TO 'student'@'localhost';
GRANT ALL PRIVILEGES ON helpdesk.* TO 'student'@'localhost';
