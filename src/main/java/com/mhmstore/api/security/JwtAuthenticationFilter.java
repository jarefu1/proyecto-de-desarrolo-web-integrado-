package com.mhmstore.api.security;

import com.mhmstore.api.model.Permiso;
import com.mhmstore.api.model.Rol;
import com.mhmstore.api.model.Usuario;
import com.mhmstore.api.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            try {
                String email = jwtService.obtenerEmail(token);
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
                    if (usuario != null) {
                        List<GrantedAuthority> authorities = new ArrayList<>();
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));

                        Set<Permiso> permisosEfectivos = usuario.getRol() == Rol.ADMIN
                                ? EnumSet.allOf(Permiso.class)
                                : (usuario.getPermisos() != null ? usuario.getPermisos() : Set.of());

                        for (Permiso permiso : permisosEfectivos) {
                            authorities.add(new SimpleGrantedAuthority("PERMISO_" + permiso.name()));
                            authorities.add(new SimpleGrantedAuthority(permiso.name()));
                        }

                        var authentication = new UsernamePasswordAuthenticationToken(
                                usuario.getEmail(), null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
