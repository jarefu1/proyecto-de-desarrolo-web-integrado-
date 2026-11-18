package com.mhmstore.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reportes_ventas")
public class ReporteVentas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(name = "total_productos_vendidos", nullable = false)
    private Integer totalProductosVendidos;

    @Column(name = "total_ingresos", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalIngresos;

    @OneToMany(mappedBy = "reporteVentas", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleReporte> detalles = new ArrayList<>();

    public ReporteVentas() {
    }

    public ReporteVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin,
                         Integer totalProductosVendidos, BigDecimal totalIngresos) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.totalProductosVendidos = totalProductosVendidos;
        this.totalIngresos = totalIngresos;
    }

    public void agregarDetalle(DetalleReporte detalle) {
        detalle.setReporteVentas(this);
        this.detalles.add(detalle);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getTotalProductosVendidos() {
        return totalProductosVendidos;
    }

    public void setTotalProductosVendidos(Integer totalProductosVendidos) {
        this.totalProductosVendidos = totalProductosVendidos;
    }

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public List<DetalleReporte> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleReporte> detalles) {
        this.detalles = detalles;
    }
}
