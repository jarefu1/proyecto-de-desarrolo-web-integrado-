package com.mhmstore.api.service;

import com.mhmstore.api.dto.CarritoItemRequest;
import com.mhmstore.api.dto.CarritoResponse;
import java.util.List;

public interface CarritoService {
    CarritoResponse crear(Long usuarioId);
    CarritoResponse obtenerPorId(Long carritoId);
    CarritoResponse obtenerActivoPorUsuario(Long usuarioId);
    CarritoResponse obtenerActivoPorEmail(String email);
    boolean usuarioPerteneceA(Long usuarioId, String email);
    List<CarritoResponse> listarPorUsuario(Long usuarioId);
    List<CarritoResponse> listarTodos();
    CarritoResponse agregarProducto(Long carritoId, CarritoItemRequest request);
    CarritoResponse quitarProducto(Long carritoId, Long productoId);
    CarritoResponse vaciar(Long carritoId);
    CarritoResponse enviarACaja(Long carritoId);
}
