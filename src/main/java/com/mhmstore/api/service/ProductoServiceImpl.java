
package com.mhmstore.api.service;

import com.mhmstore.api.dto.ProductoPatchRequest;
import com.mhmstore.api.dto.ProductoRequest;
import com.mhmstore.api.dto.ProductoResponse;
import com.mhmstore.api.exception.CodigoDuplicadoException;
import com.mhmstore.api.exception.ProductoNotFoundException;
import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.model.Categoria;
import com.mhmstore.api.model.Producto;
import com.mhmstore.api.repository.AlmacenRepository;
import com.mhmstore.api.repository.CategoriaRepository;
import com.mhmstore.api.repository.ProductoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private static final int STOCK_BAJO_UMBRAL = 5;

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AlmacenRepository almacenRepository;
    private final Path directorioImagenes;

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            AlmacenRepository almacenRepository,
            @Value("${app.storage.product-images:uploads/productos}") String directorioImagenes) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.almacenRepository = almacenRepository;
        this.directorioImagenes = Path.of(directorioImagenes == null ? "uploads/productos" : directorioImagenes).toAbsolutePath().normalize();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(
            Long almacenId,
            Boolean disponible,
            Boolean stockBajo,
            String q) {

        return productoRepository.buscarFiltrados(almacenId, null, disponible, stockBajo,
                true, q == null ? null : q.trim(), Pageable.unpaged())
                .map(this::toResponse).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listarPaginado(Long almacenId, Long categoriaId, Boolean disponible,
            Boolean stockBajo, String q, Pageable pageable) {
        return productoRepository.buscarFiltrados(almacenId, categoriaId, disponible, stockBajo,
                true, q == null ? null : q.trim(), pageable).map(this::toResponse);
    }

    
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarProductosConStockBajo(
            Integer limite) {

        return productoRepository
                .buscarProductosConStockBajo(limite)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        Producto producto = buscar(id);
        if (!producto.getActivo() && !puedeGestionar()) throw new ProductoNotFoundException(id);
        return toResponse(producto);
    }

    @Transactional(readOnly = true)
    public Page<ProductoResponse> inventario(Long categoria, Boolean disponible, Boolean bajo, String q, Pageable page) {
        return productoRepository.buscarFiltrados(null, categoria, disponible, bajo, false, q, page).map(this::toResponse);
    }

    @Override
    public ProductoResponse crear(ProductoRequest request) {

        validarCodigoDisponible(request.getCodigo(), null);

        Categoria categoria = buscarCategoria(request.getCategoriaId());
        Almacen almacen = buscarAlmacen(request.getAlmacenId());

        Producto producto = new Producto(
                null,
                request.getCodigo().trim().toUpperCase(),
                request.getModelo().trim(),
                request.getColor().trim(),
                categoria,
                request.getPrecio(),
                request.getCantidad(),
                almacen
        );
        producto.setDescripcion(request.getDescripcion());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setActivo(request.getActivo());

        return toResponse(productoRepository.save(producto));
    }

    @Override
    public ProductoResponse reemplazar(Long id, ProductoRequest request) {

        Producto producto = buscar(id);

        validarCodigoDisponible(request.getCodigo(), id);

        Categoria categoria = buscarCategoria(request.getCategoriaId());
        Almacen almacen = buscarAlmacen(request.getAlmacenId());

        producto.setCodigo(request.getCodigo().trim().toUpperCase());
        producto.setModelo(request.getModelo().trim());
        producto.setColor(request.getColor().trim());
        producto.setDescripcion(request.getDescripcion());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setActivo(request.getActivo());
        producto.setCategoria(categoria);
        producto.setPrecio(request.getPrecio());
        producto.setCantidad(request.getCantidad());
        producto.setAlmacen(almacen);

        return toResponse(productoRepository.save(producto));
    }

    @Override
    public ProductoResponse actualizarParcial(
            Long id,
            ProductoPatchRequest request) {

        Producto producto = buscar(id);

        if (request.getCodigo() != null) {
            validarCodigoDisponible(request.getCodigo(), id);
            producto.setCodigo(request.getCodigo().trim().toUpperCase());
        }

        if (request.getModelo() != null) {
            producto.setModelo(request.getModelo().trim());
        }

        if (request.getColor() != null) {
            producto.setColor(request.getColor().trim());
        }

        if (request.getCategoriaId() != null) {
            producto.setCategoria(
                    buscarCategoria(request.getCategoriaId())
            );
        }

        if (request.getPrecio() != null) {
            producto.setPrecio(request.getPrecio());
        }

        if (request.getCantidad() != null) {
            producto.setCantidad(request.getCantidad());
        }

        if (request.getAlmacenId() != null) {
            producto.setAlmacen(
                    buscarAlmacen(request.getAlmacenId())
            );
        }

        return toResponse(productoRepository.save(producto));
    }

    @Override
    public void eliminar(Long id) {
        Producto producto = buscar(id);
        String imagenAnterior = producto.getImagenUrl();
        productoRepository.delete(producto);
        eliminarImagenTrasCommit(imagenAnterior);
    }

    @Override
    public ProductoResponse guardarImagen(Long id, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty() || archivo.getSize() > 5L * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen debe pesar hasta 5 MB.");
        }
        try {
            byte[] bytes = archivo.getBytes();
            String tipo = detectarTipo(bytes);
            if (tipo == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usa una imagen JPG, PNG o WebP válida.");
            Producto producto = buscar(id);
            String extension = tipo.equals("image/png") ? ".png" : tipo.equals("image/webp") ? ".webp" : ".jpg";
            String nombre = UUID.randomUUID() + extension;
            Files.createDirectories(directorioImagenes);
            Files.write(directorioImagenes.resolve(nombre), bytes);
            String imagenAnterior = producto.getImagenUrl();
            producto.setImagenUrl(nombre);
            producto.setImagenContentType(tipo);
            ProductoResponse response = toResponse(productoRepository.save(producto));
            eliminarImagenTrasCommit(imagenAnterior);
            return response;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ImagenProducto obtenerImagen(Long id) {
        Producto producto = buscar(id);
        if (producto.getImagenUrl() == null || producto.getImagenContentType() == null) {
            throw new ProductoNotFoundException(id);
        }
        try {
            Path archivo = directorioImagenes.resolve(Path.of(producto.getImagenUrl()).getFileName()).normalize();
            if (!archivo.startsWith(directorioImagenes)) throw new IOException("Ruta de imagen inválida");
            return new ImagenProducto(Files.readAllBytes(archivo), MediaType.parseMediaType(producto.getImagenContentType()));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La imagen no está disponible.");
        }
    }

    private String detectarTipo(byte[] bytes) {
        if (bytes.length >= 8 && bytes[0] == (byte)0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47) return "image/png";
        if (bytes.length >= 3 && bytes[0] == (byte)0xff && bytes[1] == (byte)0xd8 && bytes[2] == (byte)0xff) return "image/jpeg";
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') return "image/webp";
        return null;
    }

    private void eliminarImagenTrasCommit(String nombre) {
        if (nombre == null) return;
        Runnable eliminar = () -> { try { Files.deleteIfExists(directorioImagenes.resolve(Path.of(nombre).getFileName())); } catch (IOException ignored) { } };
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
                @Override public void afterCommit() { eliminar.run(); }
            });
        } else { eliminar.run(); }
    }

    private boolean puedeGestionar() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> java.util.Set.of("ROLE_ADMIN", "PERMISO_GESTIONAR_PRODUCTOS", "GESTIONAR_PRODUCTOS", "PERMISO_GESTIONAR_INVENTARIO", "GESTIONAR_INVENTARIO", "PERMISO_GESTIONAR_CATALOGOS", "GESTIONAR_CATALOGOS").contains(a.getAuthority()));
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe la categoría con ID " + id
                ));
    }

    private Almacen buscarAlmacen(Long id) {
        return almacenRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe el almacén con ID " + id
                ));
    }

    private void validarCodigoDisponible(String codigo, Long idActual) {
        productoRepository.findByCodigoIgnoreCase(codigo.trim())
                .ifPresent(existing -> {
                    if (idActual == null
                            || !existing.getId().equals(idActual)) {
                        throw new CodigoDuplicadoException(codigo);
                    }
                });
    }

    public ProductoResponse toResponse(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getModelo(),
                producto.getColor(),
                producto.getDescripcion(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                producto.getPrecio(),
                producto.getCantidad(),
                producto.getAlmacen().getId(),
                producto.getAlmacen().getNombre(),
                producto.getImagenUrl() == null ? null : "/api/v1/productos/" + producto.getId() + "/imagen",
                producto.getStockMinimo(), producto.getActivo(), producto.getFechaCreacion(), producto.getFechaActualizacion()
        );
    }
}
