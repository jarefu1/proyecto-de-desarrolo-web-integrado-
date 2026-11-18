package com.mhmstore.api.dto;

import jakarta.validation.constraints.Pattern;

public record PagoSimuladoRequest(
        @Pattern(regexp = "TARJETA|TRANSFERENCIA|BILLETERA", message = "Método de pago simulado inválido")
        String metodo
) {}
