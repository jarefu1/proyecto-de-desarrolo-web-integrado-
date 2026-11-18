CREATE TABLE IF NOT EXISTS almacenes (
  id BIGINT NOT NULL AUTO_INCREMENT,
  descripcion VARCHAR(255),
  nombre VARCHAR(80) NOT NULL,
  ubicacion VARCHAR(150) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_almacen_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categorias (
  id BIGINT NOT NULL AUTO_INCREMENT,
  descripcion VARCHAR(255),
  nombre VARCHAR(60) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_categoria_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS usuarios (
  id BIGINT NOT NULL AUTO_INCREMENT,
  contrasena VARCHAR(255) NOT NULL,
  email VARCHAR(150) NOT NULL,
  fecha_registro DATETIME(6) NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  rol ENUM('ADMIN','TRABAJADOR') NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_usuario_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS usuario_permisos (
  usuario_id BIGINT NOT NULL,
  permiso VARCHAR(50) NOT NULL,
  KEY ix_usuario_permisos_usuario (usuario_id),
  CONSTRAINT fk_usuario_permisos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS productos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  cantidad INT NOT NULL,
  codigo VARCHAR(12) NOT NULL,
  color VARCHAR(30) NOT NULL,
  modelo VARCHAR(80) NOT NULL,
  precio DECIMAL(8,2) NOT NULL,
  descripcion VARCHAR(255),
  almacen_id BIGINT NOT NULL,
  categoria_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_producto_codigo (codigo),
  KEY ix_producto_almacen (almacen_id),
  KEY ix_producto_categoria (categoria_id),
  CONSTRAINT fk_producto_almacen FOREIGN KEY (almacen_id) REFERENCES almacenes (id),
  CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS carritos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  fecha_creacion DATETIME(6) NOT NULL,
  estado ENUM('ACTIVO','ENVIADO_A_CAJA','CANCELADO') NOT NULL,
  usuario_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  KEY ix_carrito_usuario (usuario_id),
  CONSTRAINT fk_carrito_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS items_carrito (
  id BIGINT NOT NULL AUTO_INCREMENT,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(8,2) NOT NULL,
  carrito_id BIGINT NOT NULL,
  producto_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  KEY ix_item_carrito (carrito_id),
  KEY ix_item_producto (producto_id),
  CONSTRAINT fk_item_carrito FOREIGN KEY (carrito_id) REFERENCES carritos (id) ON DELETE CASCADE,
  CONSTRAINT fk_item_producto FOREIGN KEY (producto_id) REFERENCES productos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pedidos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  fecha_envio DATETIME(6) NOT NULL,
  estado ENUM('PENDIENTE','PROCESADO','COMPLETADO') NOT NULL,
  total DECIMAL(10,2) NOT NULL,
  carrito_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_pedido_carrito (carrito_id),
  CONSTRAINT fk_pedido_carrito FOREIGN KEY (carrito_id) REFERENCES carritos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reportes_ventas (
  id BIGINT NOT NULL AUTO_INCREMENT,
  fecha_inicio DATETIME(6) NOT NULL,
  fecha_fin DATETIME(6) NOT NULL,
  total_productos_vendidos INT NOT NULL,
  total_ingresos DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS detalles_reporte (
  id BIGINT NOT NULL AUTO_INCREMENT,
  cantidad_vendida INT NOT NULL,
  ingresos DECIMAL(12,2) NOT NULL,
  reporte_id BIGINT NOT NULL,
  producto_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  KEY ix_detalle_reporte (reporte_id),
  KEY ix_detalle_producto (producto_id),
  CONSTRAINT fk_detalle_reporte FOREIGN KEY (reporte_id) REFERENCES reportes_ventas (id) ON DELETE CASCADE,
  CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES productos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
