package com.mhmstore.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(
    name = "productos",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_producto_codigo",
            columnNames = "codigo"
        )
    }
)
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 12)
    @Column(nullable = false, unique = true, length = 12)
    private String codigo;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String modelo;

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String color;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 6, fraction = 2)
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precio;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer cantidad;

    @Size(max = 255)
    @Column(length = 255)
    private String descripcion;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @Column(name = "imagen_content_type", length = 32)
    private String imagenContentType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "almacen_id", nullable = false)
    private Almacen almacen;

    @Min(0)
    @Column(nullable = false)
    private Integer stockMinimo = 5;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private java.time.LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private java.time.LocalDateTime fechaActualizacion;

    @PrePersist
    void alCrear() { fechaCreacion = java.time.LocalDateTime.now(); fechaActualizacion = fechaCreacion; }
    @PreUpdate
    void alActualizar() { fechaActualizacion = java.time.LocalDateTime.now(); }
    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer value) { stockMinimo = value; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean value) { activo = value; }
    public java.time.LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public java.time.LocalDateTime getFechaActualizacion() { return fechaActualizacion; }

    public Producto() {
    }

    public Producto(Long id, String codigo, String modelo,
                    String color, Categoria categoria,
                    BigDecimal precio, Integer cantidad,
                    Almacen almacen) {
        this.id = id;
        this.codigo = codigo;
        this.modelo = modelo;
        this.color = color;
        this.categoria = categoria;
        this.precio = precio;
        this.cantidad = cantidad;
        this.almacen = almacen;
    }

    public Producto(Long id, String codigo, String modelo,
                    String color, Categoria categoria,
                    BigDecimal precio, Integer cantidad,
                    String descripcion, Almacen almacen) {
        this(id, codigo, modelo, color, categoria, precio, cantidad, almacen);
        this.descripcion = descripcion;
    }

    public void actualizarStock(Integer variacion) {
        if (variacion == null) {
            return;
        }
        int nuevoStock = (this.cantidad == null ? 0 : this.cantidad) + variacion;
        if (nuevoStock < 0) {
            throw new IllegalStateException("Stock insuficiente para el producto " + this.codigo);
        }
        this.cantidad = nuevoStock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public String getImagenContentType() { return imagenContentType; }
    public void setImagenContentType(String imagenContentType) { this.imagenContentType = imagenContentType; }

    public Almacen getAlmacen() {
        return almacen;
    }

    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
    }
}
