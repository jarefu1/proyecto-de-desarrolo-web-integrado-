package com.mhmstore.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductoPatchRequest {

    @Size(max = 12, message = "El código no puede superar los 12 caracteres")
    private String codigo;

    @Size(max = 80, message = "El modelo no puede superar los 80 caracteres")
    private String modelo;

    @Size(max = 30, message = "El color no puede superar los 30 caracteres")
    private String color;

    @Positive(message = "El ID de la categoría debe ser mayor que cero")
    private Long categoriaId;

    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
    @Digits(integer = 6, fraction = 2,
            message = "El precio admite hasta 6 enteros y 2 decimales")
    private BigDecimal precio;

    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidad;

    @Positive(message = "El ID del almacén debe ser mayor que cero")
    private Long almacenId;

    public ProductoPatchRequest() {
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Long getAlmacenId() { return almacenId; }
    public void setAlmacenId(Long almacenId) { this.almacenId = almacenId; }
}