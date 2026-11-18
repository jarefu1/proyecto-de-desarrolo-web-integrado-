package com.mhmstore.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_reporte")
public class DetalleReporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cantidad_vendida", nullable = false)
    private Integer cantidadVendida;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal ingresos;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporte_id", nullable = false)
    private ReporteVentas reporteVentas;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    public DetalleReporte() {
    }

    public DetalleReporte(ReporteVentas reporteVentas, Producto producto,
                          Integer cantidadVendida, BigDecimal ingresos) {
        this.reporteVentas = reporteVentas;
        this.producto = producto;
        this.cantidadVendida = cantidadVendida;
        this.ingresos = ingresos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(Integer cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public BigDecimal getIngresos() {
        return ingresos;
    }

    public void setIngresos(BigDecimal ingresos) {
        this.ingresos = ingresos;
    }

    public ReporteVentas getReporteVentas() {
        return reporteVentas;
    }

    public void setReporteVentas(ReporteVentas reporteVentas) {
        this.reporteVentas = reporteVentas;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }
}
