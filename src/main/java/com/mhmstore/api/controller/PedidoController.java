package com.mhmstore.api.controller;

import com.mhmstore.api.dto.PedidoCreateRequest;
import com.mhmstore.api.dto.PedidoResponse;
import com.mhmstore.api.dto.PagoSimuladoRequest;
import com.mhmstore.api.model.EstadoPedido;
import com.mhmstore.api.service.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(esAdmin(authentication) ? pedidoService.listar()
                : pedidoService.listarPorUsuario(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPorId(@PathVariable Long id, Authentication authentication) {
        PedidoResponse pedido = pedidoService.obtenerPorId(id);
        if (!esAdmin(authentication) && !pedido.usuarioEmail().equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado");
        }
        return ResponseEntity.ok(pedido);
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crearDesdeCarrito(
            @RequestParam(required = false) Long carritoId,
            @RequestBody(required = false) PedidoCreateRequest body, Authentication authentication) {
        Long idFinal = carritoId != null ? carritoId : (body != null ? body.carritoId() : null);
        if (idFinal == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El carritoId es obligatorio");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearDesdeCarrito(idFinal, authentication.getName()));
    }

    @PostMapping("/{id}/pago-simulado")
    public ResponseEntity<PedidoResponse> pagarSimulado(
            @PathVariable Long id,
            @Valid @RequestBody PagoSimuladoRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(pedidoService.pagarSimulado(id, authentication.getName(), request.metodo()));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponse> actualizarEstado(
            @PathVariable Long id,
            @RequestParam(required = false) EstadoPedido estado,
            @RequestBody(required = false) Map<String, String> body) {
        EstadoPedido estadoFinal = estado;
        if (estadoFinal == null && body != null && body.get("estado") != null) {
            try {
                estadoFinal = EstadoPedido.valueOf(body.get("estado").trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de pedido inválido");
            }
        }
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, estadoFinal));
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
