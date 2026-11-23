-- Esquema de la base tienda_conveniencia (MySQL 8.4), generado por Hibernate a partir de las entidades.
-- Exportado con: mysqldump --no-data tienda_conveniencia

CREATE TABLE `boleta` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `calle` varchar(150) DEFAULT NULL,
  `cliente_apellidos` varchar(100) NOT NULL,
  `cliente_correo` varchar(100) NOT NULL,
  `cliente_nombre` varchar(50) NOT NULL,
  `codigo` varchar(20) DEFAULT NULL,
  `comuna` varchar(50) DEFAULT NULL,
  `departamento` varchar(20) DEFAULT NULL,
  `entrega` enum('DOMICILIO','RETIRO') NOT NULL,
  `envio` int NOT NULL,
  `estado` enum('ENTREGADA','ENVIADA','PAGADA','RECHAZADA') NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `indicaciones` varchar(300) DEFAULT NULL,
  `metodo_pago` enum('CREDITO','DEBITO','TRANSFERENCIA') NOT NULL,
  `motivo_rechazo` varchar(300) DEFAULT NULL,
  `region` varchar(50) DEFAULT NULL,
  `subtotal` int NOT NULL,
  `total` int NOT NULL,
  `usuario_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKme6nl2y7pg6v5jbcm7a4llova` (`codigo`),
  KEY `FK62vn7tni2jyc0y0xv0ewyb6sj` (`usuario_id`),
  CONSTRAINT `FK62vn7tni2jyc0y0xv0ewyb6sj` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `categoria` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(200) DEFAULT NULL,
  `imagen` varchar(255) DEFAULT NULL,
  `nombre` varchar(60) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK35t4wyxqrevf09uwx9e9p6o75` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `detalle_boleta` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` int NOT NULL,
  `imagen` varchar(255) DEFAULT NULL,
  `nombre_producto` varchar(100) NOT NULL,
  `precio_unitario` int NOT NULL,
  `subtotal` int NOT NULL,
  `boleta_id` bigint NOT NULL,
  `producto_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6ao8lmippvqnncrgwnfjjch18` (`boleta_id`),
  KEY `FKht1rfy8yukwexahhcscksj5ri` (`producto_id`),
  CONSTRAINT `FK6ao8lmippvqnncrgwnfjjch18` FOREIGN KEY (`boleta_id`) REFERENCES `boleta` (`id`),
  CONSTRAINT `FKht1rfy8yukwexahhcscksj5ri` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `producto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(20) NOT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `imagen` varchar(255) DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `precio` int NOT NULL,
  `precio_oferta` int DEFAULT NULL,
  `stock` int NOT NULL,
  `stock_critico` int NOT NULL,
  `categoria_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkxdt4u9c4w6vveo7ylph4pd09` (`codigo`),
  KEY `FKodqr7965ok9rwquj1utiamt0m` (`categoria_id`),
  CONSTRAINT `FKodqr7965ok9rwquj1utiamt0m` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `usuario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `apellidos` varchar(100) NOT NULL,
  `comuna` varchar(50) DEFAULT NULL,
  `correo` varchar(100) NOT NULL,
  `departamento` varchar(20) DEFAULT NULL,
  `direccion` varchar(300) DEFAULT NULL,
  `estado` enum('ACTIVO','INACTIVO') NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `region` varchar(50) DEFAULT NULL,
  `rol` enum('ADMIN','CLIENTE','VENDEDOR') NOT NULL,
  `run` varchar(9) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK2mlfr087gb1ce55f2j87o74t` (`correo`),
  UNIQUE KEY `UKboxe2nbk8bf7qn3s3ig45xb69` (`run`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
