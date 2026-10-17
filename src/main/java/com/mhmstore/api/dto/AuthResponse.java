package com.mhmstore.api.dto;

public record AuthResponse(String token, String tipo, String email, String rol) {}
