CREATE DATABASE IF NOT EXISTS buspass_test;
USE buspass_test;

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(40) PRIMARY KEY,
    pwd VARCHAR(100) NOT NULL
);

DELETE FROM users;
INSERT INTO users(id, pwd) VALUES
('student01', 'correct123'),
('student02', 'sample456');
