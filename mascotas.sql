CREATE TABLE articulo (id_articulo BIGINT NOT NULL, edad INT, especie VARCHAR(255), foto LONGBLOB NOT NULL, nombre VARCHAR(255) NOT NULL, precio DOUBLE NOT NULL, raza VARCHAR(255), PRIMARY KEY (id_articulo));

CREATE TABLE articulo (id_articulo BIGINT NOT NULL, edad INT, especie VARCHAR(255), foto LONGBLOB NOT NULL, nombre VARCHAR(255) NOT NULL, precio DOUBLE NOT NULL, raza VARCHAR(255), PRIMARY KEY (id_articulo));

CREATE TABLE venta (id_venta BIGINT NOT NULL, `ARTICULOSCOMPRADOS` LONGBLOB NOT NULL, PRIMARY KEY (id_venta));

CREATE TABLE venta_articulo (`Venta_id_venta` BIGINT NOT NULL, articulos_id_articulo BIGINT NOT NULL, PRIMARY KEY (`Venta_id_venta`, articulos_id_articulo));
