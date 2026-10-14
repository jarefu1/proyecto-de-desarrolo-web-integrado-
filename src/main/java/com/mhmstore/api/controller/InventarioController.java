package com.mhmstore.api.controller;

import com.mhmstore.api.dto.ProductoResponse;
import com.mhmstore.api.service.InventarioService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @PatchMapping("/{productoId}/salida")
    public ResponseEntity<ProductoResponse> registrarSalida(
            @PathVariable Long productoId,
            @RequestParam(required = false) Integer cantidad,
            @RequestBody(required = false) Map<String, Integer> body) {

        Integer cantidadFinal = cantidad != null ? cantidad
                : (body != null ? body.get("cantidad") : null);

        return ResponseEntity.ok(
                inventarioService.registrarSalida(productoId, cantidadFinal)
        );
    }

    @PatchMapping("/{productoId}/entrada")
    public ResponseEntity<ProductoResponse> registrarEntrada(
            @PathVariable Long productoId,
            @RequestParam(required = false) Integer cantidad,
            @RequestBody(required = false) Map<String, Integer> body) {

        Integer cantidadFinal = cantidad != null ? cantidad
                : (body != null ? body.get("cantidad") : null);

        return ResponseEntity.ok(
                inventarioService.registrarEntrada(productoId, cantidadFinal)
        );
    }
}