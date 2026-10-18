
package com.mhmstore.api.repository;

import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.model.Categoria;
import com.mhmstore.api.model.Producto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductoRepositoryIntegrationTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private org.springframework.data.jpa.repository.JpaRepository<Categoria, Long> categoriaRepository;

    @Autowired
    private org.springframework.data.jpa.repository.JpaRepository<Almacen, Long> almacenRepository;

    @Test
    void debeBuscarProductosConStockBajoOrdenadosPorCantidad() {
        // Arrange: crear los datos necesarios para la prueba
        Categoria categoria = new Categoria(
                "Prueba ropa", "Categoría para pruebas"
        );
        categoria = categoriaRepository.save(categoria);

        Almacen almacen = new Almacen(
                "Almacen prueba", "Arequipa", "Almacén para pruebas"
        );
        almacen = almacenRepository.save(almacen);

        Producto productoStockDos = crearProducto(
                "TEST001", "Modelo A", categoria, almacen, 2
        );
        Producto productoStockCinco = crearProducto(
                "TEST002", "Modelo B", categoria, almacen, 5
        );
        Producto productoStockDiez = crearProducto(
                "TEST003", "Modelo C", categoria, almacen, 10
        );

        productoRepository.saveAll(List.of(
                productoStockDiez,
                productoStockCinco,
                productoStockDos
        ));
        productoRepository.flush();

        // Act: buscar productos con stock menor o igual a 5
        List<Producto> resultado =
                productoRepository.buscarProductosConStockBajo(5);

        // Assert: incluye 2 y 5, excluye 10 y ordena ascendentemente
        assertThat(resultado)
                .extracting(Producto::getCodigo)
                .containsExactly("TEST001", "TEST002");
    }

    private Producto crearProducto(
            String codigo,
            String modelo,
            Categoria categoria,
            Almacen almacen,
            int cantidad) {

        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setModelo(modelo);
        producto.setColor("Negro");
        producto.setCategoria(categoria);
        producto.setPrecio(new BigDecimal("49.90"));
        producto.setCantidad(cantidad);
        producto.setAlmacen(almacen);

        return producto;
    }
}
