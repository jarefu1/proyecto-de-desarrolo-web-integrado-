package com.mhmstore.api.service;

import com.mhmstore.api.dto.DetalleReporteResponse;
import com.mhmstore.api.dto.ReporteVentasResponse;
import com.mhmstore.api.model.DetalleReporte;
import com.mhmstore.api.model.EstadoPedido;
import com.mhmstore.api.model.ItemCarrito;
import com.mhmstore.api.model.Pedido;
import com.mhmstore.api.model.Producto;
import com.mhmstore.api.model.ReporteVentas;
import com.mhmstore.api.repository.PedidoRepository;
import com.mhmstore.api.repository.ReporteVentasRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ReporteVentasServiceImpl implements ReporteVentasService {

    private final ReporteVentasRepository reporteVentasRepository;
    private final PedidoRepository pedidoRepository;

    public ReporteVentasServiceImpl(
            ReporteVentasRepository reporteVentasRepository,
            PedidoRepository pedidoRepository) {
        this.reporteVentasRepository = reporteVentasRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public ReporteVentasResponse generarReporte(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las fechas de inicio y fin son obligatorias");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha fin no puede ser anterior a la fecha inicio");
        }

        List<Pedido> pedidos = pedidoRepository.buscarPedidosPorRangoFechas(fechaInicio, fechaFin);

        Map<Long, AcumuladorProducto> acumuladoPorProducto = new LinkedHashMap<>();
        int totalProductosVendidos = 0;
        BigDecimal totalIngresos = BigDecimal.ZERO;

        for (Pedido pedido : pedidos) {
            if (pedido.getEstado() == EstadoPedido.PENDIENTE) {
                continue;
            }
            for (ItemCarrito item : pedido.getCarrito().getItems()) {
                Producto producto = item.getProducto();
                BigDecimal subtotalItem = item.subtotal();
                totalProductosVendidos += item.getCantidad();
                totalIngresos = totalIngresos.add(subtotalItem);

                acumuladoPorProducto.compute(producto.getId(), (id, actual) -> {
                    if (actual == null) {
                        return new AcumuladorProducto(producto, item.getCantidad(), subtotalItem);
                    }
                    actual.cantidad += item.getCantidad();
                    actual.ingresos = actual.ingresos.add(subtotalItem);
                    return actual;
                });
            }
        }

        ReporteVentas reporte = new ReporteVentas(
                fechaInicio,
                fechaFin,
                totalProductosVendidos,
                totalIngresos
        );

        for (AcumuladorProducto acc : acumuladoPorProducto.values()) {
            DetalleReporte detalle = new DetalleReporte(
                    reporte,
                    acc.producto,
                    acc.cantidad,
                    acc.ingresos
            );
            reporte.agregarDetalle(detalle);
        }

        return toResponse(reporteVentasRepository.save(reporte));
    }

    @Override
    public ReporteVentasResponse generarMensual(Integer mes, Integer anio) {
        if (mes == null || mes < 1 || mes > 12 || anio == null || anio < 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mes o año inválido para el reporte mensual");
        }
        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime fin = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return generarReporte(inicio, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteVentasResponse> listar() {
        return reporteVentasRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteVentasResponse obtenerPorId(Long id) {
        ReporteVentas reporte = reporteVentasRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el reporte de ventas con ID " + id));
        return toResponse(reporte);
    }

    private ReporteVentasResponse toResponse(ReporteVentas reporte) {
        List<DetalleReporteResponse> detalles = reporte.getDetalles().stream()
                .map(d -> new DetalleReporteResponse(
                        d.getId(),
                        d.getProducto().getId(),
                        d.getProducto().getCodigo(),
                        d.getProducto().getModelo(),
                        d.getCantidadVendida(),
                        d.getIngresos()
                ))
                .toList();

        return new ReporteVentasResponse(
                reporte.getId(),
                reporte.getFechaInicio(),
                reporte.getFechaFin(),
                reporte.getTotalProductosVendidos(),
                reporte.getTotalIngresos(),
                detalles
        );
    }

    private static class AcumuladorProducto {
        final Producto producto;
        int cantidad;
        BigDecimal ingresos;

        AcumuladorProducto(Producto producto, int cantidad, BigDecimal ingresos) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.ingresos = ingresos;
        }
    }
}
