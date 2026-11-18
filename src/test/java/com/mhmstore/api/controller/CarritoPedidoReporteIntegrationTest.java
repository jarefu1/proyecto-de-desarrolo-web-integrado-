package com.mhmstore.api.controller;

import com.mhmstore.api.dto.CarritoItemRequest;
import com.mhmstore.api.dto.CarritoResponse;
import com.mhmstore.api.dto.PedidoResponse;
import com.mhmstore.api.dto.ReporteVentasResponse;
import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.model.Categoria;
import com.mhmstore.api.model.EstadoCarrito;
import com.mhmstore.api.model.EstadoPedido;
import com.mhmstore.api.model.Permiso;
import com.mhmstore.api.model.Producto;
import com.mhmstore.api.model.Rol;
import com.mhmstore.api.model.Usuario;
import com.mhmstore.api.repository.AlmacenRepository;
import com.mhmstore.api.repository.CategoriaRepository;
import com.mhmstore.api.repository.ProductoRepository;
import com.mhmstore.api.repository.UsuarioRepository;
import com.mhmstore.api.service.CarritoService;
import com.mhmstore.api.service.PedidoService;
import com.mhmstore.api.service.ReporteVentasService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CarritoPedidoReporteIntegrationTest {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ReporteVentasService reporteVentasService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private Usuario usuario;
    private Producto producto;

    @BeforeEach
    void prepararDatos() {
        usuario = new Usuario();
        usuario.setNombre("Vendedor Test");
        usuario.setEmail("vendedor.test@mhmstore.com");
        usuario.setContrasena("$2a$10$HYCPwIIYweGyUNZkHxKQMurIE74w6D8ReHeEVd3P.KsI89wbABCmS");
        usuario.setRol(Rol.TRABAJADOR);
        usuario.setPermisos(EnumSet.allOf(Permiso.class));
        usuario = usuarioRepository.save(usuario);

        Categoria categoria = new Categoria("Ejecutiva", "Mochilas ejecutivas");
        categoria = categoriaRepository.save(categoria);

        Almacen almacen = new Almacen("Central Test", "Av. Principal 123", "Almacén test");
        almacen = almacenRepository.save(almacen);

        producto = new Producto(
                null,
                "MOCH900",
                "Mochila Ejecutiva Pro",
                "Gris",
                categoria,
                new BigDecimal("100.00"),
                10,
                "Mochila impermeable con porta laptop",
                almacen
        );
        producto = productoRepository.save(producto);
    }

    @Test
    void flujoCompleto_carritoAPedidoYReporteDeVentas_debeDescontarStockYGenerarReporte() {
        // 1. Crear carrito y agregar 3 unidades (3 x 100.00 = 300.00)
        CarritoResponse carrito = carritoService.crear(usuario.getId());
        carrito = carritoService.agregarProducto(
                carrito.id(),
                new CarritoItemRequest(producto.getId(), 3)
        );

        assertEquals(1, carrito.items().size());
        assertEquals(0, new BigDecimal("300.00").compareTo(carrito.total()));

        // 2. Crear pedido transaccional desde el carrito
        PedidoResponse pedido = pedidoService.crearDesdeCarrito(carrito.id());
        assertNotNull(pedido.id());
        assertEquals(EstadoPedido.COMPLETADO, pedido.estado());
        assertEquals(0, new BigDecimal("300.00").compareTo(pedido.total()));

        // 3. Verificar que el stock del producto bajó de 10 a 7 y el carrito pasó a ENVIADO_A_CAJA
        Producto productoActualizado = productoRepository.findById(producto.getId()).orElseThrow();
        assertEquals(7, productoActualizado.getCantidad());

        CarritoResponse carritoActualizado = carritoService.obtenerPorId(carrito.id());
        assertEquals(EstadoCarrito.ENVIADO_A_CAJA, carritoActualizado.estado());

        // 4. Generar reporte de ventas por rango de fechas usando JPQL
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fin = LocalDateTime.now().plusDays(1);
        ReporteVentasResponse reporte = reporteVentasService.generarReporte(inicio, fin);

        assertNotNull(reporte.id());
        assertTrue(reporte.totalProductosVendidos() >= 3);
        assertTrue(reporte.totalIngresos().compareTo(new BigDecimal("300.00")) >= 0);
        assertFalse(reporte.detalles().isEmpty());
    }

    @Test
    void pagoSimuladoDelCliente_conservaElComprobanteAnteReintentos() {
        usuario.setRol(Rol.CLIENTE);
        usuarioRepository.save(usuario);
        CarritoResponse carrito = carritoService.crear(usuario.getId());
        carritoService.agregarProducto(carrito.id(), new CarritoItemRequest(producto.getId(), 2));
        PedidoResponse pendiente = pedidoService.crearDesdeCarrito(carrito.id(), usuario.getEmail());
        assertEquals(EstadoPedido.PENDIENTE, pendiente.estado());
        assertNull(pendiente.fechaPago());

        PedidoResponse pagado = pedidoService.pagarSimulado(pendiente.id(), usuario.getEmail(), "BILLETERA");
        assertEquals(EstadoPedido.COMPLETADO, pagado.estado());
        assertEquals("BILLETERA", pagado.metodoPago());
        assertNotNull(pagado.fechaPago());
        assertEquals(0, new BigDecimal("200.00").compareTo(pagado.total()));
        assertEquals(pagado, pedidoService.pagarSimulado(pendiente.id(), usuario.getEmail(), "BILLETERA"));
        assertEquals(pagado, pedidoService.listarPorUsuario(usuario.getEmail()).get(0));
    }

    @Test
    void crearPedido_sinStockSuficiente_debeLanzarExcepcionYNoDescontar() {
        CarritoResponse carrito = carritoService.crear(usuario.getId());
        carrito = carritoService.agregarProducto(
                carrito.id(),
                new CarritoItemRequest(producto.getId(), 8)
        );

        // Reducimos el stock disponible antes de procesar el pedido
        producto.setCantidad(4);
        productoRepository.save(producto);

        Long carritoId = carrito.id();
        assertThrows(
                ResponseStatusException.class,
                () -> pedidoService.crearDesdeCarrito(carritoId)
        );

        Producto sinCambios = productoRepository.findById(producto.getId()).orElseThrow();
        assertEquals(4, sinCambios.getCantidad());
    }
}
