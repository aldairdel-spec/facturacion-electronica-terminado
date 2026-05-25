-- ============================================================
-- SCRIPT DE BASE DE DATOS - SISTEMA DE FACTURACIÓN ELECTRÓNICA
-- PostgreSQL
-- ============================================================

CREATE DATABASE facturacion_db;

\c facturacion_db;

-- Tabla: turno
CREATE TABLE turno (
    id_turno     SERIAL PRIMARY KEY,
    hora_inicio  TIME NOT NULL,
    hora_fin     TIME,
    fecha        DATE NOT NULL DEFAULT CURRENT_DATE
);

-- Tabla: empleado (usuario del sistema)
CREATE TABLE empleado (
    id_empleado  SERIAL PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL,
    cargo        VARCHAR(50) NOT NULL,         -- ADMINISTRADOR, CAJERO
    usuario      VARCHAR(50) UNIQUE NOT NULL,
    contrasena   VARCHAR(255) NOT NULL,        -- BCrypt hash
    activo       BOOLEAN DEFAULT TRUE,
    turno_id     INT REFERENCES turno(id_turno)
);

-- Tabla: cliente
CREATE TABLE cliente (
    id_cliente   SERIAL PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL,
    cedula       VARCHAR(20) UNIQUE NOT NULL,
    telefono     VARCHAR(20),
    correo       VARCHAR(100),
    direccion    VARCHAR(200),
    fecha_registro DATE DEFAULT CURRENT_DATE
);

-- Tabla: producto
CREATE TABLE producto (
    id_producto  SERIAL PRIMARY KEY,
    nombre       VARCHAR(150) NOT NULL,
    codigo       VARCHAR(50) UNIQUE NOT NULL,
    precio       NUMERIC(12,2) NOT NULL,
    stock        INT NOT NULL DEFAULT 0,
    tipo         VARCHAR(50),                  -- ej. GRAVADO, EXCLUIDO
    activo       BOOLEAN DEFAULT TRUE
);

-- Tabla: factura
CREATE TABLE factura (
    id_factura      SERIAL PRIMARY KEY,
    numero_factura  VARCHAR(30) UNIQUE NOT NULL,
    fecha           TIMESTAMP NOT NULL DEFAULT NOW(),
    total           NUMERIC(14,2) NOT NULL,
    estado          VARCHAR(20) DEFAULT 'PENDIENTE',  -- PENDIENTE, VALIDADA, ANULADA
    id_cliente      INT REFERENCES cliente(id_cliente),
    id_empleado     INT REFERENCES empleado(id_empleado)
);

-- Tabla: detalle_factura
CREATE TABLE detalle_factura (
    id_detalle      SERIAL PRIMARY KEY,
    id_factura      INT NOT NULL REFERENCES factura(id_factura) ON DELETE CASCADE,
    id_producto     INT NOT NULL REFERENCES producto(id_producto),
    cantidad        INT NOT NULL,
    precio_unitario NUMERIC(12,2) NOT NULL,
    iva             NUMERIC(5,2) NOT NULL DEFAULT 19.00,
    subtotal        NUMERIC(14,2) NOT NULL
);

-- Índices
CREATE INDEX idx_factura_cliente   ON factura(id_cliente);
CREATE INDEX idx_factura_empleado  ON factura(id_empleado);
CREATE INDEX idx_detalle_factura   ON detalle_factura(id_factura);
CREATE INDEX idx_producto_codigo   ON producto(codigo);

-- Usuario administrador por defecto (contraseña: Admin123!)
-- Hash BCrypt generado para 'Admin123!'
INSERT INTO empleado (nombre, cargo, usuario, contrasena)
VALUES ('Administrador Principal', 'ADMINISTRADOR', 'admin',
        '$2b$10$W35K9ROytE//Z0.ZKTIw4uMWNmPLF64JLrh9AXD/57Cgb6FHVB2OO');

-- Datos de ejemplo
INSERT INTO cliente (nombre, cedula, telefono, correo, direccion)
VALUES ('Cliente Consumidor Final', '0000000000', '3000000000', 'consumidor@mail.com', 'N/A');

INSERT INTO producto (nombre, codigo, precio, stock, tipo)
VALUES
    ('Arroz X5Kg',       'ARR001', 18500.00, 100, 'EXCLUIDO'),
    ('Aceite 1L',        'ACE001', 12300.00,  80, 'GRAVADO'),
    ('Leche Entera 1L',  'LEC001',  4200.00, 150, 'EXCLUIDO'),
    ('Pan Tajado',       'PAN001',  6500.00,  60, 'EXCLUIDO'),
    ('Gaseosa 2L',       'GAS001',  8900.00,  90, 'GRAVADO');
