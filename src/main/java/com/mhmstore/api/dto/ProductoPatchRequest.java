package com.mhmstore.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.model.Categoria;

import java.math.BigDecimal;

public class ProductoPatchRequest {
    @Size(min = 1, max = 12, message = "El codigo admite entre 1 y 12 caracteres")
    private String codigo;
    @Size(min = 1, max = 80, message = "El modelo admite entre 1 y 80 caracteres")
    private String modelo;
    @Size(min = 1, max = 30, message = "El color admite entre 1 y 30 caracteres")
    private String color;
    private Categoria categoria;
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 6, fraction = 2, message = "El precio admite maximo 6 enteros y 2 decimales")
    private BigDecimal precio;
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidad;
    private Almacen almacen;

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
