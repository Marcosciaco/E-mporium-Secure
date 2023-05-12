CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    type BOOLEAN DEFAULT FALSE,
    emergencyEmail VARCHAR(255) DEFAULT NULL,
    emergencyPhone VARCHAR(255) DEFAULT NULL,
    forgotToken VARCHAR(36) DEFAULT NULL,
    registrationToken VARCHAR(36) DEFAULT NULL,
    sessionToken VARCHAR(36) DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) NOT NULL,
    tag VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    image VARCHAR(255) DEFAULT NULL,
    stock INTEGER DEFAULT NULL,
    category VARCHAR(255) NOT NULL
);
