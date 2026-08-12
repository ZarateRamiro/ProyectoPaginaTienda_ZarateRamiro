CREATE DATABASE IF NOT EXISTS tienda
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE tienda;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS item_carrito;
DROP TABLE IF EXISTS carrito;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS usuario;

SET FOREIGN_KEY_CHECKS = 1;

-- ==========================
-- TABLA USUARIO
-- ==========================

CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    rol ENUM('ADMIN','USUARIO') NOT NULL
);

-- ==========================
-- TABLA PRODUCTO
-- ==========================

CREATE TABLE producto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    imagen VARCHAR(500),
    stock INT NOT NULL DEFAULT 0,

    tipo ENUM(
        'REMERA',
        'CAMPERA',
        'BUZO',
        'PANTALON',
        'JEAN',
        'SHORT',
        'ZAPATILLAS',
        'ZAPATOS',
        'BOTAS',
        'SANDALIAS',
        'GORRA',
        'ACCESORIOS'
    ) NOT NULL
);

-- ==========================
-- TABLA CARRITO
-- ==========================

CREATE TABLE carrito (
    id INT AUTO_INCREMENT PRIMARY KEY,

    usuario_id INT NOT NULL,

    fecha_creacion DATE NOT NULL,

    estado VARCHAR(20) NOT NULL,

    CONSTRAINT fk_carrito_usuario
        FOREIGN KEY(usuario_id)
        REFERENCES usuario(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ==========================
-- TABLA ITEM_CARRITO
-- ==========================

CREATE TABLE item_carrito (

    id INT AUTO_INCREMENT PRIMARY KEY,

    carrito_id INT NOT NULL,

    producto_id INT NOT NULL,

    cantidad INT NOT NULL,

    precio_unitario DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_item_carrito
        FOREIGN KEY(carrito_id)
        REFERENCES carrito(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_item_producto
        FOREIGN KEY(producto_id)
        REFERENCES producto(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);