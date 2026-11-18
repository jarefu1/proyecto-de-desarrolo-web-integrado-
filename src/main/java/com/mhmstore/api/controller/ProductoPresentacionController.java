package com.mhmstore.api.controller;
import com.mhmstore.api.service.ProductoPresentacionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/productos/{productoId}")
public class ProductoPresentacionController {
 private final ProductoPresentacionService servicio;
 public ProductoPresentacionController(ProductoPresentacionService s){servicio=s;}
 @GetMapping("/detalle") public ProductoPresentacionService.Detalle detalle(@PathVariable Long productoId){return servicio.detalle(productoId);}
 @GetMapping("/variantes") public List<com.mhmstore.api.dto.ProductoResponse> variantes(@PathVariable Long productoId){return servicio.variantes(productoId);}
 @GetMapping("/galeria") public List<ProductoPresentacionService.Foto> listar(@PathVariable Long productoId){return servicio.listar(productoId);}
 @PostMapping(value="/galeria",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public ResponseEntity<ProductoPresentacionService.Foto> agregar(@PathVariable Long productoId,@RequestPart("imagen") MultipartFile imagen,@RequestParam(required=false) Long varianteId){return ResponseEntity.status(HttpStatus.CREATED).body(servicio.agregar(productoId,imagen,varianteId));}
 @GetMapping("/galeria/{imagenId}") public ResponseEntity<byte[]> leer(@PathVariable Long productoId,@PathVariable Long imagenId){var img=servicio.leer(productoId,imagenId);return ResponseEntity.ok().contentType(img.tipo()).body(img.contenido());}
 @DeleteMapping("/galeria/{imagenId}") public ResponseEntity<Void> eliminar(@PathVariable Long productoId,@PathVariable Long imagenId){servicio.eliminar(productoId,imagenId);return ResponseEntity.noContent().build();}
}
