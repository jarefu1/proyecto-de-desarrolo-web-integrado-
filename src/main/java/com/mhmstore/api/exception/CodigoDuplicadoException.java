package com.mhmstore.api.exception;

public class CodigoDuplicadoException extends RuntimeException {
    public CodigoDuplicadoException(String codigo) { super("Ya existe un producto con codigo " + codigo); }
}
