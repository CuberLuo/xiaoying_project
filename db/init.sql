-- MySQL 容器首次启动时的初始化（与 docker-compose 的 MYSQL_* 环境变量互补，确保库与账号存在）
CREATE DATABASE IF NOT EXISTS material_studio CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER IF NOT EXISTS 'material'@'%' IDENTIFIED BY 'material123';
GRANT ALL PRIVILEGES ON material_studio.* TO 'material'@'%';
FLUSH PRIVILEGES;
