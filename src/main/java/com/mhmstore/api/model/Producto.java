package com.mhmstore.api.model;

import java.math.BigDecimal;

public class Producto {
    private Long id;
    private String codigo;
    private String modelo;
    private String color;
    private Categoria categoria;
    private BigDecimal precio;
    private Integer cantidad;
    private Almacen almacen;

    public Producto() {}

    public Producto(Long id, String codigo, String modelo, String color, Categoria categoria,
                    BigDecimal precio, Integer cantidad, Almacen almacen) {
        this.id = id; this.codigo = codigo; this.modelo = modelo; this.color = color;
        this.categoria = categoria; this.precio = precio; this.cantidad = cantidad; this.almacen = almacen;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Almacen getAlmacen() { return almacen; }
    public void setAlmacen(Almacen almacen) { this.almacen = almacen; }
}
