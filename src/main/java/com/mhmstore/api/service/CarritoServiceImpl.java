package com.mhmstore.api.service;

import com.mhmstore.api.dto.CarritoItemRequest;
import com.mhmstore.api.dto.CarritoResponse;
import com.mhmstore.api.dto.ItemCarritoResponse;
import com.mhmstore.api.exception.ProductoNotFoundException;
import com.mhmstore.api.model.Carrito;
import com.mhmstore.api.model.EstadoCarrito;
import com.mhmstore.api.model.ItemCarrito;
import com.mhmstore.api.model.Producto;
import com.mhmstore.api.model.Usuario;
import com.mhmstore.api.repository.CarritoRepository;
import com.mhmstore.api.repository.ProductoRepository;
import com.mhmstore.api.repository.UsuarioRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public CarritoServiceImpl(
            CarritoRepository carritoRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository) {
        this.carritoRepository = carritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public CarritoResponse crear(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        Carrito carrito = new Carrito(usuario);
        return toResponse(carritoRepository.save(carrito));
    }

    @Override
    @Transactional(readOnly = true)
    public CarritoResponse obtenerPorId(Long carritoId) {
        return toResponse(buscarCarrito(carritoId));
    }

    @Override
    public CarritoResponse obtenerActivoPorUsuario(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        Carrito carrito = carritoRepository
                .findFirstByUsuarioIdAndEstadoOrderByFechaCreacionDesc(usuarioId, EstadoCarrito.ACTIVO)
                .orElseGet(() -> carritoRepository.save(new Carrito(usuario)));
        return toResponse(carrito);
    }

    @Override
    public CarritoResponse obtenerActivoPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return obtenerActivoPorUsuario(usuario.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean usuarioPerteneceA(Long usuarioId, String email) {
        return usuarioRepository.findById(usuarioId)
                .map(usuario -> usuario.getEmail().equalsIgnoreCase(email)).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarritoResponse> listarPorUsuario(Long usuarioId) {
        buscarUsuario(usuarioId);
        return carritoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarritoResponse> listarTodos() {
        return carritoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CarritoResponse agregarProducto(Long carritoId, CarritoItemRequest request) {
        Carrito carrito = buscarCarrito(carritoId);
        validarCarritoModificable(carrito);

        Producto producto = productoRepository.findById(request.productoId())
                .orElseThrow(() -> new ProductoNotFoundException(request.productoId()));

        int cantidadExistente = carrito.getItems().stream()
                .filter(i -> i.getProducto().getId().equals(producto.getId()))
                .mapToInt(ItemCarrito::getCantidad)
                .sum();

        if (producto.getCantidad() < (cantidadExistente + request.cantidad())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stock insuficiente para el producto " + producto.getCodigo()
                            + ". Disponible: " + producto.getCantidad()
            );
        }

        carrito.agregarProducto(producto, request.cantidad());
        return toResponse(carritoRepository.save(carrito));
    }

    @Override
    public CarritoResponse quitarProducto(Long carritoId, Long productoId) {
        Carrito carrito = buscarCarrito(carritoId);
        validarCarritoModificable(carrito);
        carrito.quitarProducto(productoId);
        return toResponse(carritoRepository.save(carrito));
    }

    @Override
    public CarritoResponse vaciar(Long carritoId) {
        Carrito carrito = buscarCarrito(carritoId);
        validarCarritoModificable(carrito);
        carrito.vaciar();
        return toResponse(carritoRepository.save(carrito));
    }

    @Override
    public CarritoResponse enviarACaja(Long carritoId) {
        Carrito carrito = buscarCarrito(carritoId);
        if (carrito.getEstado() == EstadoCarrito.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede enviar a caja un carrito cancelado");
        }
        if (carrito.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede enviar a caja un carrito vacío");
        }
        carrito.enviarACaja();
        return toResponse(carritoRepository.save(carrito));
    }

    private void validarCarritoModificable(Carrito carrito) {
        if (carrito.getEstado() != EstadoCarrito.ACTIVO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El carrito no se puede modificar porque su estado es " + carrito.getEstado()
            );
        }
    }

    private Carrito buscarCarrito(Long id) {
        return carritoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el carrito con ID " + id));
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el usuario con ID " + id));
    }

    private CarritoResponse toResponse(Carrito carrito) {
        List<ItemCarritoResponse> items = carrito.getItems().stream()
                .map(i -> new ItemCarritoResponse(
                        i.getId(),
                        i.getProducto().getId(),
                        i.getProducto().getCodigo(),
                        i.getProducto().getModelo(),
                        i.getCantidad(),
                        i.getPrecioUnitario(),
                        i.subtotal()
                ))
                .toList();

        return new CarritoResponse(
                carrito.getId(),
                carrito.getUsuario().getId(),
                carrito.getUsuario().getEmail(),
                carrito.getFechaCreacion(),
                carrito.getEstado(),
                items,
                carrito.calcularTotal()
        );
    }
}
