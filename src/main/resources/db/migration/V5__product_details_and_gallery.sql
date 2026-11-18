ALTER TABLE productos
 ADD COLUMN color_hex VARCHAR(7) NULL,
 ADD COLUMN material VARCHAR(100) NULL,
 ADD COLUMN capacidad VARCHAR(40) NULL,
 ADD COLUMN dimensiones VARCHAR(80) NULL,
 ADD COLUMN peso VARCHAR(40) NULL;
CREATE TABLE producto_imagenes (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 producto_id BIGINT NOT NULL,
 archivo VARCHAR(255) NOT NULL,
 content_type VARCHAR(32) NOT NULL,
 CONSTRAINT fk_producto_imagen FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
 INDEX idx_producto_imagen (producto_id)
);
