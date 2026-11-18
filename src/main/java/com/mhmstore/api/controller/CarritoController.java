package com.mhmstore.api.controller;

import com.mhmstore.api.dto.CarritoItemRequest;
import com.mhmstore.api.dto.CarritoResponse;
import com.mhmstore.api.service.CarritoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/carritos")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public ResponseEntity<List<CarritoResponse>> listarTodos() {
        return ResponseEntity.ok(carritoService.listarTodos());
    }

    @GetMapping("/mi-carrito")
    public ResponseEntity<CarritoResponse> obtenerMiCarrito(Authentication authentication) {
        return ResponseEntity.ok(carritoService.obtenerActivoPorEmail(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarritoResponse> obtenerPorId(@PathVariable Long id, Authentication authentication) {
        CarritoResponse carrito = carritoService.obtenerPorId(id);
        validarPropietario(carrito, authentication);
        return ResponseEntity.ok(carrito);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<CarritoResponse> obtenerPorUsuario(@PathVariable Long usuarioId, Authentication authentication) {
        if (!esAdmin(authentication) && !carritoService.usuarioPerteneceA(usuarioId, authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        return ResponseEntity.ok(carritoService.obtenerActivoPorUsuario(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/historial")
    public ResponseEntity<List<CarritoResponse>> listarPorUsuario(@PathVariable Long usuarioId, Authentication authentication) {
        if (!esAdmin(authentication) && !carritoService.usuarioPerteneceA(usuarioId, authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        return ResponseEntity.ok(carritoService.listarPorUsuario(usuarioId));
    }

    @PostMapping
    public ResponseEntity<CarritoResponse> crear(
            @RequestParam(required = false) Long usuarioId,
            @RequestBody(required = false) Map<String, Long> body) {
        Long idFinal = usuarioId != null ? usuarioId : (body != null ? body.get("usuarioId") : null);
        if (idFinal == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuarioId es obligatorio");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(carritoService.crear(idFinal));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<CarritoResponse> agregarProducto(
            @PathVariable Long id,
            @Valid @RequestBody CarritoItemRequest request, Authentication authentication) {
        validarPropietario(carritoService.obtenerPorId(id), authentication);
        return ResponseEntity.ok(carritoService.agregarProducto(id, request));
    }

    @DeleteMapping("/{id}/items/{productoId}")
    public ResponseEntity<CarritoResponse> quitarProducto(
            @PathVariable Long id,
            @PathVariable Long productoId, Authentication authentication) {
        validarPropietario(carritoService.obtenerPorId(id), authentication);
        return ResponseEntity.ok(carritoService.quitarProducto(id, productoId));
    }

    @DeleteMapping("/{id}/items")
    public ResponseEntity<CarritoResponse> vaciar(@PathVariable Long id, Authentication authentication) {
        validarPropietario(carritoService.obtenerPorId(id), authentication);
        return ResponseEntity.ok(carritoService.vaciar(id));
    }

    @PatchMapping("/{id}/enviar-a-caja")
    public ResponseEntity<CarritoResponse> enviarACaja(@PathVariable Long id, Authentication authentication) {
        validarPropietario(carritoService.obtenerPorId(id), authentication);
        return ResponseEntity.ok(carritoService.enviarACaja(id));
    }

    private void validarPropietario(CarritoResponse carrito, Authentication authentication) {
        if (!esAdmin(authentication) && !carrito.usuarioEmail().equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no encontrado");
        }
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
