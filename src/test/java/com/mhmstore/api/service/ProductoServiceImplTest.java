/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mhmstore.api.service;

/**
 *
 * @author Usuario
 */

import com.mhmstore.api.dto.ProductoRequest;
import com.mhmstore.api.dto.ProductoResponse;
import com.mhmstore.api.exception.CodigoDuplicadoException;
import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.model.Categoria;
import com.mhmstore.api.model.Producto;
import com.mhmstore.api.repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoServiceImplTest {

    @Mock
    private ProductoRepository repository;

    private ProductoServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ProductoServiceImpl(repository);
    }

    @Test
    void crear_debeNormalizarCodigoYGuardarProducto() {

        // ARRANGE
        ProductoRequest request = crearRequest();
        request.setCodigo("  sku001  ");

        when(repository.findByCodigoIgnoreCase("sku001"))
                .thenReturn(Optional.empty());

        when(repository.save(any(Producto.class)))
                .thenAnswer(invocation -> {
                    Producto producto = invocation.getArgument(0);
                    producto.setId(1L);
                    return producto;
                });

        // ACT
        ProductoResponse resultado = service.crear(request);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("SKU001", resultado.codigo());
        assertEquals("Mochila Urbana", resultado.modelo());

        verify(repository).save(any(Producto.class));
    }

    private ProductoRequest crearRequest() {

        ProductoRequest request = new ProductoRequest();

        request.setCodigo("SKU001");
        request.setModelo("Mochila Urbana");
        request.setColor("Negro");
        request.setCategoria(Categoria.URBANA);
        request.setPrecio(new BigDecimal("89.90"));
        request.setCantidad(10);
        request.setAlmacen(Almacen.PRINCIPAL);

        return request;
    }
    
    @Test
void crear_debeRechazarCodigoDuplicado() {

    // ARRANGE
    ProductoRequest request = crearRequest();

    Producto productoExistente = new Producto(
            1L,
            "SKU001",
            "Mochila Urbana",
            "Negro",
            Categoria.URBANA,
            new BigDecimal("89.90"),
            10,
            Almacen.PRINCIPAL
    );

    when(repository.findByCodigoIgnoreCase("SKU001"))
            .thenReturn(Optional.of(productoExistente));

    // ACT + ASSERT
    assertThrows(
            CodigoDuplicadoException.class,
            () -> service.crear(request)
    );

    verify(repository, never()).save(any(Producto.class));
}
}