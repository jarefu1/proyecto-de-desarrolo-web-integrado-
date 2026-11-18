package com.mhmstore.api.controller;

import com.mhmstore.api.dto.ProductoResponse;
import com.mhmstore.api.repository.ProductoRepository;
import com.mhmstore.api.repository.CategoriaRepository;
import com.mhmstore.api.service.ProductoServiceImpl;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario")
public class InventarioConsultaController {
    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final ProductoServiceImpl servicio;
    public InventarioConsultaController(ProductoRepository p, CategoriaRepository c, ProductoServiceImpl s) { productos=p; categorias=c; servicio=s; }
    @GetMapping("/productos")
    public Page<ProductoResponse> listar(@RequestParam(required=false) Long categoria,
            @RequestParam(required=false) Boolean disponible, @RequestParam(required=false) Boolean stockBajo,
            @RequestParam(required=false) String q, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        if(page<0 || size<1 || size>100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paginación inválida");
        return servicio.inventario(categoria, disponible, stockBajo, q, PageRequest.of(page,size));
    }
    @GetMapping("/resumen")
    public Resumen resumen() { return new Resumen(productos.count(), productos.contarDisponibles(), productos.contarBajos(), productos.countByCantidad(0), categorias.count()); }
    @GetMapping("/categorias")
    public List<CategoriaResumen> categorias() { return categorias.findAll().stream().map(c -> new CategoriaResumen(c.getId(), c.getNombre(), c.getDescripcion(), productos.countByCategoriaId(c.getId()))).toList(); }
    public record Resumen(long total, long disponibles, long bajos, long agotados, long categorias) {}
    public record CategoriaResumen(Long id, String nombre, String descripcion, long productos) {}
}
