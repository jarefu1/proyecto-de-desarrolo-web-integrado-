package com.mhmstore.api.service;

import com.mhmstore.api.dto.PedidoResponse;
import com.mhmstore.api.model.EstadoPedido;
import java.util.List;

public interface PedidoService {
    PedidoResponse crearDesdeCarrito(Long carritoId);
    PedidoResponse crearDesdeCarrito(Long carritoId, String emailUsuario);
    List<PedidoResponse> listar();
    List<PedidoResponse> listarPorUsuario(String emailUsuario);
    PedidoResponse obtenerPorId(Long id);
    PedidoResponse actualizarEstado(Long id, EstadoPedido nuevoEstado);
    PedidoResponse pagarSimulado(Long id, String emailUsuario, String metodo);
}
