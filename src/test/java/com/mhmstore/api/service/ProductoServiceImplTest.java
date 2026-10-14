package com.mhmstore.api.service;

import com.mhmstore.api.dto.ProductoRequest;
import com.mhmstore.api.dto.ProductoResponse;
import com.mhmstore.api.exception.CodigoDuplicadoException;
import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.model.Categoria;
import com.mhmstore.api.model.Producto;
import com.mhmstore.api.repository.AlmacenRepository;
import com.mhmstore.api.repository.CategoriaRepository;
import com.mhmstore.api.repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository repository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private AlmacenRepository almacenRepository;

    @InjectMocks
    private ProductoServiceImpl service;

    @Test
    void crear_debeNormalizarCodigoYGuardarProducto() {
        // ARRANGE
        ProductoRequest request = crearRequest();
        request.setCodigo("  SKU001  ");

        Categoria categoria = crearCategoria();
        Almacen almacen = crearAlmacen();

        when(repository.findByCodigoIgnoreCase("SKU001"))
                .thenReturn(Optional.empty());

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(almacenRepository.findById(1L))
                .thenReturn(Optional.of(almacen));

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
        assertEquals(1L, resultado.categoriaId());
        assertEquals(1L, resultado.almacenId());

        verify(repository).save(any(Producto.class));
    }

    @Test
    void crear_debeRechazarCodigoDuplicado() {
        // ARRANGE
        ProductoRequest request = crearRequest();

        Categoria categoria = crearCategoria();
        Almacen almacen = crearAlmacen();

        Producto productoExistente = new Producto(
                1L,
                "SKU001",
                "Mochila Urbana",
                "Negro",
                categoria,
                new BigDecimal("89.90"),
                10,
                almacen
        );

        when(repository.findByCodigoIgnoreCase("SKU001"))
                .thenReturn(Optional.of(productoExistente));

        // ACT + ASSERT
        assertThrows(
                CodigoDuplicadoException.class,
                () -> service.crear(request)
        );

        verify(repository, never()).save(any(Producto.class));
        verifyNoInteractions(categoriaRepository, almacenRepository);
    }

    private ProductoRequest crearRequest() {
        ProductoRequest request = new ProductoRequest();

        request.setCodigo("SKU001");
        request.setModelo("Mochila Urbana");
        request.setColor("Negro");
        request.setCategoriaId(1L);
        request.setPrecio(new BigDecimal("89.90"));
        request.setCantidad(10);
        request.setAlmacenId(1L);

        return request;
    }

    private Categoria crearCategoria() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Urbana");
        categoria.setDescripcion("Mochilas para uso urbano");
        return categoria;
    }

    private Almacen crearAlmacen() {
        Almacen almacen = new Almacen();
        almacen.setId(1L);
        almacen.setNombre("Principal");
        almacen.setUbicacion("Almacén principal");
        return almacen;
    }
}