CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS user_db;
CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS order_db;

CREATE USER IF NOT EXISTS 'service_auth'@'%' IDENTIFIED BY 'securePassword123';
CREATE USER IF NOT EXISTS 'service_user'@'%' IDENTIFIED BY 'securePassword123';
CREATE USER IF NOT EXISTS 'service_product'@'%' IDENTIFIED BY 'securePassword123';
CREATE USER IF NOT EXISTS 'service_order'@'%' IDENTIFIED BY 'securePassword123';

GRANT ALL PRIVILEGES ON auth_db.* TO 'service_auth'@'%';
GRANT ALL PRIVILEGES ON user_db.* TO 'service_user'@'%';
GRANT ALL PRIVILEGES ON product_db.* TO 'service_product'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'service_order'@'%';

FLUSH PRIVILEGES;
