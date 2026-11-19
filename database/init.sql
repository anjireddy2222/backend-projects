-- Docker creates the student account using MYSQL_USER and MYSQL_PASSWORD first.
CREATE DATABASE IF NOT EXISTS amazon_catalog CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS amazon_customer CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS helpdesk CHARACTER SET utf8mb4;
GRANT ALL PRIVILEGES ON amazon_catalog.* TO 'student'@'%';
GRANT ALL PRIVILEGES ON amazon_customer.* TO 'student'@'%';
GRANT ALL PRIVILEGES ON helpdesk.* TO 'student'@'%';
