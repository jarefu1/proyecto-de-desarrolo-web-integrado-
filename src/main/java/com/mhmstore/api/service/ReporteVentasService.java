package com.mhmstore.api.service;

import com.mhmstore.api.dto.ReporteVentasResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface ReporteVentasService {
    ReporteVentasResponse generarReporte(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    ReporteVentasResponse generarMensual(Integer mes, Integer anio);
    List<ReporteVentasResponse> listar();
    ReporteVentasResponse obtenerPorId(Long id);
}
