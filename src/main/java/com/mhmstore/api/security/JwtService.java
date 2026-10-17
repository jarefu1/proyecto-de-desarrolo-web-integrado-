package com.mhmstore.api.security;

import com.mhmstore.api.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${security.jwt.secret:}") String secret,
            @Value("${security.jwt.expiration-ms:3600000}") long expirationMs) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Falta configurar JWT_SECRET_BASE64. Consulte README.md.");
        }
        byte[] decoded;
        try {
            decoded = Decoders.BASE64.decode(secret);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("JWT_SECRET_BASE64 debe ser una clave Base64 valida.", ex);
        }
        if (decoded.length < 32) {
            throw new IllegalStateException("La clave JWT debe tener al menos 32 bytes (256 bits).");
        }
        if (expirationMs <= 0) throw new IllegalStateException("La expiracion JWT debe ser positiva.");
        this.key = Keys.hmacShaKeyFor(decoded);
        this.expirationMs = expirationMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);
        return Jwts.builder()
                .claims(Map.of("rol", usuario.getRol().name()))
                .subject(usuario.getEmail())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(key)
                .compact();
    }

    public String obtenerEmail(String token) {
        return obtenerClaims(token).getSubject();
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }
}
