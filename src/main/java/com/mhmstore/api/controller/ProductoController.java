
package com.mhmstore.api.controller;

import com.mhmstore.api.dto.ProductoPatchRequest;
import com.mhmstore.api.dto.ProductoRequest;
import com.mhmstore.api.dto.ProductoResponse;
import com.mhmstore.api.service.ProductoService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listar(
            @RequestParam(name = "almacen", required = false) Long almacenId,
            @RequestParam(required = false) Boolean disponible,
            @RequestParam(required = false) Boolean stockBajo,
            @RequestParam(required = false) String q) {

        return ResponseEntity.ok(
                service.listar(almacenId, disponible, stockBajo, q)
        );
    }

    @GetMapping("/pagina")
    public ResponseEntity<Page<ProductoResponse>> listarPaginado(
            @RequestParam(name = "almacen", required = false) Long almacenId,
            @RequestParam(name = "categoria", required = false) Long categoriaId,
            @RequestParam(required = false) Boolean disponible,
            @RequestParam(required = false) Boolean stockBajo,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Paginación inválida (size máximo: 100)");
        }
        return ResponseEntity.ok(service.listarPaginado(almacenId, categoriaId, disponible, stockBajo, q,
                PageRequest.of(page, size, Sort.by("id").ascending())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtener(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoResponse> guardarImagen(@PathVariable Long id, @RequestPart("imagen") MultipartFile imagen) {
        return ResponseEntity.ok(service.guardarImagen(id, imagen));
    }

    @GetMapping(value = "/{id}/imagen")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable Long id) {
        var imagen = service.obtenerImagen(id);
        return ResponseEntity.ok().contentType(imagen.tipo()).body(imagen.contenido());
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(
            @Valid @RequestBody ProductoRequest request,
            UriComponentsBuilder uriBuilder) {

        ProductoResponse creado = service.crear(request);

        URI location = uriBuilder
                .path("/api/v1/productos/{id}")
                .buildAndExpand(creado.id())
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> reemplazar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {

        return ResponseEntity.ok(
                service.reemplazar(id, request)
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarParcial(
            @PathVariable Long id,
            @Valid @RequestBody ProductoPatchRequest request) {

        return ResponseEntity.ok(
                service.actualizarParcial(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        service.eliminar(id);

        return ResponseEntity.noContent().build();
    }
    
    
@GetMapping("/stock-bajo")
public ResponseEntity<List<ProductoResponse>> buscarProductosConStockBajo(
        @RequestParam(defaultValue = "5") Integer limite) {

    return ResponseEntity.ok(
            service.buscarProductosConStockBajo(limite)
    );
}

}
