
package com.mhmstore.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductoRequest {

    @NotBlank(message = "El codigo (SKU) es obligatorio")
    @Size(max = 12, message = "El codigo admite maximo 12 caracteres")
    private String codigo;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 80, message = "El modelo admite maximo 80 caracteres")
    private String modelo;

    @NotBlank(message = "El color es obligatorio")
    @Size(max = 30, message = "El color admite maximo 30 caracteres")
    private String color;

    @NotNull(message = "La categoria es obligatoria")
    @Positive(message = "El ID de categoria debe ser positivo")
    private Long categoriaId;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 6, fraction = 2,
            message = "El precio admite maximo 6 enteros y 2 decimales")
    private BigDecimal precio;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidad;

    @NotNull(message = "El almacen es obligatorio")
    @Positive(message = "El ID de almacen debe ser positivo")
    private Long almacenId;

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

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
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

    public Long getAlmacenId() {
        return almacenId;
    }

    public void setAlmacenId(Long almacenId) {
        this.almacenId = almacenId;
    }
}
