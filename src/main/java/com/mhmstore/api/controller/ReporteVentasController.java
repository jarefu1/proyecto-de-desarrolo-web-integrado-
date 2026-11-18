package com.mhmstore.api.controller;

import com.mhmstore.api.dto.ReporteVentasResponse;
import com.mhmstore.api.service.ReporteVentasService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteVentasController {

    private final ReporteVentasService reporteVentasService;

    public ReporteVentasController(ReporteVentasService reporteVentasService) {
        this.reporteVentasService = reporteVentasService;
    }

    @GetMapping
    public ResponseEntity<List<ReporteVentasResponse>> listar() {
        return ResponseEntity.ok(reporteVentasService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReporteVentasResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reporteVentasService.obtenerPorId(id));
    }

    @PostMapping("/generar")
    public ResponseEntity<ReporteVentasResponse> generarPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reporteVentasService.generarReporte(fechaInicio, fechaFin));
    }

    @PostMapping("/mensual")
    public ResponseEntity<ReporteVentasResponse> generarMensual(
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reporteVentasService.generarMensual(mes, anio));
    }
}
