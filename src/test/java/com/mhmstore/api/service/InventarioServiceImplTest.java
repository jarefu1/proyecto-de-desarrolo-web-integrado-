package com.mhmstore.api.service;

import com.mhmstore.api.dto.ProductoResponse;
import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.model.Categoria;
import com.mhmstore.api.model.Producto;
import com.mhmstore.api.repository.ProductoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    @Test
    void registrarSalida_conStockSuficiente_debeDescontarStockYGuardar() {
        // ARRANGE: Producto con 10 unidades en stock
        Producto producto = crearProductoConStock(10);

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT: Descontamos 4 unidades
        ProductoResponse resultado = inventarioService.registrarSalida(1L, 4);

        // ASSERT: El stock debe bajar a 6 y guardarse en BD
        assertNotNull(resultado);
        assertEquals(6, resultado.cantidad());
        assertEquals(6, producto.getCantidad());
        verify(productoRepository).save(producto);
    }

    @Test
    void registrarSalida_conStockInsuficiente_debeFallarYNoModificarStock() {
        // ARRANGE: Producto con solo 5 unidades disponibles
        Producto producto = crearProductoConStock(5);

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        // ACT + ASSERT: Intentar sacar 8 unidades lanza excepción
        assertThrows(
                ResponseStatusException.class,
                () -> inventarioService.registrarSalida(1L, 8)
        );

        // Comprobamos que el stock sigue intacto en 5 y nunca se guardó cambio parcial
        assertEquals(5, producto.getCantidad());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void registrarSalida_conCantidadNoPositiva_debeRechazarOperacion() {
        // ACT + ASSERT: Cantidad 0 o negativa no debe tocar la base de datos
        assertThrows(
                ResponseStatusException.class,
                () -> inventarioService.registrarSalida(1L, 0)
        );

        verifyNoInteractions(productoRepository);
    }

    private Producto crearProductoConStock(Integer stockInicial) {
        Categoria categoria = new Categoria("Urbana", "Mochilas para uso urbano");
        categoria.setId(1L);

        Almacen almacen = new Almacen("Principal", "Almacén principal", "Central");
        almacen.setId(1L);

        return new Producto(
                1L,
                "SKU001",
                "Mochila Urbana",
                "Negro",
                categoria,
                new BigDecimal("89.90"),
                stockInicial,
                almacen
        );
    }
}