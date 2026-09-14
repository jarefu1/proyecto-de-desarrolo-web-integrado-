package com.mhmstore.api.repository;

import org.springframework.stereotype.Repository;
import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.model.Categoria;
import com.mhmstore.api.model.Producto;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryProductoRepository implements ProductoRepository {
    private final ConcurrentHashMap<Long, Producto> data = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public InMemoryProductoRepository() {
        save(new Producto(null, "MCH-001", "Mochila Escolar Prime", "Azul",
                Categoria.ESCOLAR, new BigDecimal("89.90"), 25, Almacen.PRINCIPAL));
        save(new Producto(null, "MCH-002", "Mochila Urbana Negra", "Negro",
                Categoria.URBANA, new BigDecimal("129.90"), 8, Almacen.PRINCIPAL));
        save(new Producto(null, "MCH-003", "Mochila de Montana 40L", "Verde",
                Categoria.MONTANA, new BigDecimal("249.90"), 3, Almacen.SECUNDARIO));
                save(new Producto(null, "MCH-004", "Mochila Urbana Negra", "Negro",
                Categoria.URBANA, new BigDecimal("129.90"), 8, Almacen.PRINCIPAL));
                save(new Producto(null, "MCH-005", "Mochila Urbana Negra", "Negro",
                Categoria.URBANA, new BigDecimal("129.90"), 8, Almacen.PRINCIPAL));
                save(new Producto(null, "MCH-006", "Mochila Urbana Negra", "Negro",
                Categoria.URBANA, new BigDecimal("129.90"), 8, Almacen.PRINCIPAL));



    }

    @Override public List<Producto> findAll() {
        return data.values().stream().sorted(Comparator.comparing(Producto::getId)).toList();
    }
    @Override public Optional<Producto> findById(Long id) { return Optional.ofNullable(data.get(id)); }
    @Override public Optional<Producto> findByCodigoIgnoreCase(String codigo) {
        return data.values().stream().filter(p -> p.getCodigo().equalsIgnoreCase(codigo)).findFirst();
    }
    @Override public Producto save(Producto producto) {
        if (producto.getId() == null) producto.setId(sequence.incrementAndGet());
        data.put(producto.getId(), producto);
        return producto;
    }
    @Override public void deleteById(Long id) { data.remove(id); }
}
