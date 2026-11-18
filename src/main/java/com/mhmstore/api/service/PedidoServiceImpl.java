package com.mhmstore.api.service;

import com.mhmstore.api.dto.ItemCarritoResponse;
import com.mhmstore.api.dto.PedidoResponse;
import com.mhmstore.api.model.Carrito;
import com.mhmstore.api.model.EstadoCarrito;
import com.mhmstore.api.model.EstadoPedido;
import com.mhmstore.api.model.ItemCarrito;
import com.mhmstore.api.model.Pedido;
import com.mhmstore.api.model.Producto;
import com.mhmstore.api.model.Rol;
import com.mhmstore.api.repository.CarritoRepository;
import com.mhmstore.api.repository.PedidoRepository;
import com.mhmstore.api.repository.ProductoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;

    public PedidoServiceImpl(
            PedidoRepository pedidoRepository,
            CarritoRepository carritoRepository,
            ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public PedidoResponse crearDesdeCarrito(Long carritoId) {
        return crearDesdeCarrito(carritoId, null);
    }

    @Override
    @Transactional
    public PedidoResponse crearDesdeCarrito(Long carritoId, String emailUsuario) {
        if (carritoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del carrito es obligatorio");
        }

        Carrito carrito = carritoRepository.findByIdForUpdate(carritoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el carrito con ID " + carritoId));

        if (emailUsuario != null && !carrito.getUsuario().getEmail().equalsIgnoreCase(emailUsuario)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el carrito con ID " + carritoId);
        }
        if (pedidoRepository.existsByCarritoId(carritoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya se generó un pedido para el carrito " + carritoId);
        }

        if (carrito.getEstado() == EstadoCarrito.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede crear un pedido desde un carrito cancelado");
        }

        if (carrito.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El carrito no contiene productos");
        }

        // Operación transaccional: verificar y descontar stock de todos los productos del carrito
        for (ItemCarrito item : carrito.getItems().stream()
                .sorted(java.util.Comparator.comparing(i -> i.getProducto().getId())).toList()) {
            Producto producto = productoRepository.findByIdForUpdate(item.getProducto().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Producto no encontrado: " + item.getProducto().getId()));

            if (!producto.getActivo()) throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto ya no está publicado: " + producto.getCodigo());
            if (producto.getCantidad() < item.getCantidad()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente para el producto " + producto.getCodigo()
                                + ". Disponible: " + producto.getCantidad()
                                + ", solicitado: " + item.getCantidad()
                );
            }

            producto.setCantidad(producto.getCantidad() - item.getCantidad());
            productoRepository.save(producto);
        }

        carrito.enviarACaja();
        carritoRepository.save(carrito);

        EstadoPedido estadoInicial = carrito.getUsuario().getRol() == Rol.CLIENTE
                ? EstadoPedido.PENDIENTE
                : EstadoPedido.COMPLETADO;
        Pedido pedido = new Pedido(carrito, estadoInicial, carrito.calcularTotal());
        return toResponse(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorUsuario(String emailUsuario) {
        return pedidoRepository.findByCarritoUsuarioEmailIgnoreCaseOrderByFechaEnvioDesc(emailUsuario)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse obtenerPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el pedido con ID " + id));
        return toResponse(pedido);
    }

    @Override
    public PedidoResponse actualizarEstado(Long id, EstadoPedido nuevoEstado) {
        if (nuevoEstado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nuevo estado es obligatorio");
        }
        Pedido pedido = buscarPedidoParaActualizar(id);
        pedido.setEstado(nuevoEstado);
        return toResponse(pedidoRepository.save(pedido));
    }

    @Override
    public PedidoResponse pagarSimulado(Long id, String emailUsuario, String metodo) {
        if (metodo == null || !java.util.Set.of("TARJETA", "TRANSFERENCIA", "BILLETERA").contains(metodo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Método de pago simulado inválido");
        }
        Pedido pedido = buscarPedidoParaActualizar(id);
        if (!pedido.getCarrito().getUsuario().getEmail().equalsIgnoreCase(emailUsuario)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado");
        }
        if (pedido.getFechaPago() != null) {
            if (metodo.equals(pedido.getMetodoPago())) {
                return toResponse(pedido);
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El pedido ya fue pagado con otro método");
        }
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El pedido no está pendiente de pago");
        }
        pedido.setMetodoPago(metodo);
        pedido.setFechaPago(java.time.LocalDateTime.now());
        pedido.setEstado(EstadoPedido.COMPLETADO);
        return toResponse(pedidoRepository.save(pedido));
    }

    private Pedido buscarPedidoParaActualizar(Long id) {
        return pedidoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el pedido con ID " + id));
    }

    private PedidoResponse toResponse(Pedido pedido) {
        Carrito carrito = pedido.getCarrito();
        List<ItemCarritoResponse> items = carrito.getItems().stream()
                .map(i -> new ItemCarritoResponse(
                        i.getId(),
                        i.getProducto().getId(),
                        i.getProducto().getCodigo(),
                        i.getProducto().getModelo(),
                    i.getProducto().getImagenUrl() == null ? null : "/api/v1/productos/" + i.getProducto().getId() + "/imagen",
                        i.getCantidad(),
                        i.getPrecioUnitario(),
                        i.subtotal()
                ))
                .toList();

        return new PedidoResponse(
                pedido.getId(),
                carrito.getId(),
                carrito.getUsuario().getId(),
                carrito.getUsuario().getEmail(),
                pedido.getFechaEnvio(),
                pedido.getEstado(),
                pedido.getTotal(),
                pedido.getMetodoPago(),
                pedido.getFechaPago(),
                items
        );
    }
}
