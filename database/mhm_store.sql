-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Base de datos: `mhm_store`

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `almacenes`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `almacenes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) DEFAULT NULL,
  `nombre` varchar(80) NOT NULL,
  `ubicacion` varchar(150) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4ilm0f1weyncwuyyikn7rrlf4` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `almacenes` (`id`, `descripcion`, `nombre`, `ubicacion`) VALUES
(1, 'Almacén principal de mochilas', 'Principal', 'Almacén central'),
(2, 'Almacén adicional de mochilas', 'Almacen Secundario', 'Segundo piso')
ON DUPLICATE KEY UPDATE `descripcion` = VALUES(`descripcion`), `ubicacion` = VALUES(`ubicacion`);

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `categorias`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `categorias` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) DEFAULT NULL,
  `nombre` varchar(60) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqcog8b7hps1hioi9onqwjdt6y` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `categorias` (`id`, `descripcion`, `nombre`) VALUES
(1, 'Mochilas de uso urbano', 'Urbana'),
(3, 'Mochilas para actividades deportivas', 'Deportiva')
ON DUPLICATE KEY UPDATE `descripcion` = VALUES(`descripcion`);

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `productos`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `productos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cantidad` int(11) NOT NULL,
  `codigo` varchar(12) NOT NULL,
  `color` varchar(30) NOT NULL,
  `modelo` varchar(80) NOT NULL,
  `precio` decimal(8,2) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `almacen_id` bigint(20) NOT NULL,
  `categoria_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_producto_codigo` (`codigo`),
  KEY `FKiic8p6ahjqklw2fxvxrrhrcbt` (`almacen_id`),
  KEY `FK2fwq10nwymfv7fumctxt9vpgb` (`categoria_id`),
  CONSTRAINT `FK2fwq10nwymfv7fumctxt9vpgb` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`),
  CONSTRAINT `FKiic8p6ahjqklw2fxvxrrhrcbt` FOREIGN KEY (`almacen_id`) REFERENCES `almacenes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `productos` (`id`, `cantidad`, `codigo`, `color`, `modelo`, `precio`, `descripcion`, `almacen_id`, `categoria_id`) VALUES
(1, 10, 'MOCH001', 'Negro', 'Mochila Urbana', 89.90, 'Mochila urbana clásica resistente', 1, 1),
(2, 10, 'MOCH002', 'Negro', 'Mochila Urbana', 89.90, 'Mochila urbana con bolsillo frontal', 1, 1),
(4, 10, 'MOCH008', 'Negro', 'Mochila Urbana', 89.90, 'Mochila urbana ligera', 1, 1),
(5, 10, 'MOCH007', 'Negro', 'Mochila Urbana', 89.90, 'Mochila urbana reforzada', 1, 1),
(6, 10, 'MOCH0010', 'Negro', 'Mochila Urbana', 89.90, 'Mochila urbana porta laptop', 1, 1),
(7, 10, 'MOCH0011', 'Negro', 'Mochila Urbana', 89.90, 'Mochila urbana impermeable', 1, 1),
(8, 10, 'MOCH0012', 'Negro', 'Mochila Urbana', 89.90, 'Mochila urbana edición especial', 1, 1)
ON DUPLICATE KEY UPDATE `cantidad` = VALUES(`cantidad`), `precio` = VALUES(`precio`);

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `usuarios`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `usuarios` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contrasena` varchar(255) NOT NULL,
  `email` varchar(150) NOT NULL,
  `fecha_registro` datetime(6) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `rol` enum('ADMIN','TRABAJADOR') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_usuario_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `usuarios` (`id`, `contrasena`, `email`, `fecha_registro`, `nombre`, `rol`) VALUES
(1, '$2a$10$/1lofnUSw7CkV9KHZh71W.03iWuJwYxRDRpXvNY5D4znyZRwGul1m', 'admin@mhmstore.com', '2026-10-17 09:46:36.000000', 'Administrador', 'ADMIN'),
(2, '$2a$10$HYCPwIIYweGyUNZkHxKQMurIE74w6D8ReHeEVd3P.KsI89wbABCmS', 'trabajador@mhmstore.com', '2026-10-17 09:46:36.000000', 'Trabajador', 'TRABAJADOR')
ON DUPLICATE KEY UPDATE `nombre` = VALUES(`nombre`), `rol` = VALUES(`rol`);

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `usuario_permisos`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `usuario_permisos` (
  `usuario_id` bigint(20) NOT NULL,
  `permiso` varchar(50) NOT NULL,
  KEY `fk_usuario_permisos_usuario` (`usuario_id`),
  CONSTRAINT `fk_usuario_permisos_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `usuario_permisos` (`usuario_id`, `permiso`) VALUES
(1, 'VER_CATALOGOS'),
(1, 'GESTIONAR_PRODUCTOS'),
(1, 'GESTIONAR_INVENTARIO'),
(1, 'GESTIONAR_CATALOGOS'),
(1, 'GESTIONAR_CARRITO'),
(1, 'GESTIONAR_PEDIDOS'),
(1, 'VER_REPORTES'),
(2, 'VER_CATALOGOS'),
(2, 'GESTIONAR_CARRITO'),
(2, 'GESTIONAR_PEDIDOS'),
(2, 'GESTIONAR_INVENTARIO');

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `carritos`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `carritos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_creacion` datetime(6) NOT NULL,
  `estado` enum('ACTIVO','ENVIADO_A_CAJA','CANCELADO') NOT NULL,
  `usuario_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_carrito_usuario` (`usuario_id`),
  CONSTRAINT `fk_carrito_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `items_carrito`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `items_carrito` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cantidad` int(11) NOT NULL,
  `precio_unitario` decimal(8,2) NOT NULL,
  `carrito_id` bigint(20) NOT NULL,
  `producto_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_item_carrito` (`carrito_id`),
  KEY `fk_item_producto` (`producto_id`),
  CONSTRAINT `fk_item_carrito` FOREIGN KEY (`carrito_id`) REFERENCES `carritos` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `pedidos`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `pedidos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_envio` datetime(6) NOT NULL,
  `estado` enum('PENDIENTE','PROCESADO','COMPLETADO') NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `carrito_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pedido_carrito` (`carrito_id`),
  CONSTRAINT `fk_pedido_carrito` FOREIGN KEY (`carrito_id`) REFERENCES `carritos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `reportes_ventas`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `reportes_ventas` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_inicio` datetime(6) NOT NULL,
  `fecha_fin` datetime(6) NOT NULL,
  `total_productos_vendidos` int(11) NOT NULL,
  `total_ingresos` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `detalles_reporte`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `detalles_reporte` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cantidad_vendida` int(11) NOT NULL,
  `ingresos` decimal(12,2) NOT NULL,
  `reporte_id` bigint(20) NOT NULL,
  `producto_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_detalle_reporte` (`reporte_id`),
  KEY `fk_detalle_producto` (`producto_id`),
  CONSTRAINT `fk_detalle_reporte` FOREIGN KEY (`reporte_id`) REFERENCES `reportes_ventas` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_detalle_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
