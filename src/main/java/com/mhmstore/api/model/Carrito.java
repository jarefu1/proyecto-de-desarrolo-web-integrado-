package com.mhmstore.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "carritos")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoCarrito estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrito> items = new ArrayList<>();

    public Carrito() {
    }

    public Carrito(Usuario usuario) {
        this.usuario = usuario;
        this.estado = EstadoCarrito.ACTIVO;
        this.fechaCreacion = LocalDateTime.now();
    }

    @PrePersist
    public void antesDeGuardar() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoCarrito.ACTIVO;
        }
    }

    public void agregarProducto(Producto producto, Integer cantidad) {
        Optional<ItemCarrito> existente = items.stream()
                .filter(i -> i.getProducto().getId().equals(producto.getId()))
                .findFirst();

        if (existente.isPresent()) {
            ItemCarrito item = existente.get();
            item.setCantidad(item.getCantidad() + cantidad);
            item.setPrecioUnitario(producto.getPrecio());
        } else {
            ItemCarrito nuevo = new ItemCarrito(this, producto, cantidad, producto.getPrecio());
            items.add(nuevo);
        }
    }

    public void quitarProducto(Long productoId) {
        items.removeIf(i -> i.getProducto().getId().equals(productoId));
    }

    public void vaciar() {
        items.clear();
    }

    public BigDecimal calcularTotal() {
        return items.stream()
                .map(ItemCarrito::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void enviarACaja() {
        this.estado = EstadoCarrito.ENVIADO_A_CAJA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public EstadoCarrito getEstado() {
        return estado;
    }

    public void setEstado(EstadoCarrito estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<ItemCarrito> getItems() {
        return items;
    }

    public void setItems(List<ItemCarrito> items) {
        this.items = items;
    }
}
