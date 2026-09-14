package com.mhmstore.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


import com.mhmstore.api.dto.ProductoPatchRequest;
import com.mhmstore.api.dto.ProductoRequest;
import com.mhmstore.api.dto.ProductoResponse;

import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.service.ProductoService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {
    private final ProductoService service;

    public ProductoController(ProductoService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listar(
            @RequestParam(required = false) Almacen almacen,
            @RequestParam(required = false) Boolean disponible,
            @RequestParam(required = false) Boolean stockBajo,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(service.listar(almacen, disponible, stockBajo, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request,
                                                  UriComponentsBuilder uriBuilder) {
        ProductoResponse creado = service.crear(request);
        URI location = uriBuilder.path("/api/v1/productos/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> reemplazar(@PathVariable Long id,
                                                       @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(service.reemplazar(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarParcial(@PathVariable Long id,
                                                              @Valid @RequestBody ProductoPatchRequest request) {
        return ResponseEntity.ok(service.actualizarParcial(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
