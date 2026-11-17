package com.mhmstore.api.security;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter)
            throws Exception {
        http.cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, error) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .accessDeniedHandler((request, response, error) ->
                    response.sendError(HttpServletResponse.SC_FORBIDDEN)))
            .authorizeHttpRequests(auth -> auth
                // Peticiones preflight OPTIONS requeridas por navegadores para Angular
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Catálogo público para explorar; el registro público solo crea cuentas CLIENTE.
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/registro", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/productos/stock-bajo").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_GESTIONAR_INVENTARIO", "GESTIONAR_INVENTARIO")
                .requestMatchers(HttpMethod.GET, "/api/v1/productos/**", "/api/v1/categorias/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/carritos", "/api/v1/carritos/usuario/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/carritos").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/*/estado").hasRole("ADMIN")
                // Administracion de usuarios y permisos: exclusivo para ADMIN
                .requestMatchers("/api/v1/usuarios", "/api/v1/usuarios/**").hasRole("ADMIN")
                // Inventario: ADMIN o trabajador con permiso GESTIONAR_INVENTARIO
                .requestMatchers("/api/v1/inventario/**").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_GESTIONAR_INVENTARIO", "GESTIONAR_INVENTARIO")
                // Reportes de ventas: ADMIN o trabajador con permiso VER_REPORTES
                .requestMatchers("/api/v1/reportes/**").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_VER_REPORTES", "VER_REPORTES")
                // El rol TRABAJADOR por sí solo no concede permisos funcionales.
                .requestMatchers("/api/v1/carritos/**").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_GESTIONAR_CARRITO", "GESTIONAR_CARRITO")
                .requestMatchers("/api/v1/pedidos/**").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_GESTIONAR_PEDIDOS", "GESTIONAR_PEDIDOS")
                // Lectura de productos, categorias y almacenes
                .requestMatchers(HttpMethod.GET, "/api/v1/productos/**").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_VER_CATALOGOS", "VER_CATALOGOS")
                .requestMatchers(HttpMethod.GET, "/api/v1/categorias/**", "/api/v1/almacenes/**").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_VER_CATALOGOS", "VER_CATALOGOS")
                // Escritura en productos: ADMIN o trabajador con permiso GESTIONAR_PRODUCTOS
                .requestMatchers(HttpMethod.POST, "/api/v1/productos/**").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_GESTIONAR_PRODUCTOS", "GESTIONAR_PRODUCTOS")
                .requestMatchers(HttpMethod.PUT, "/api/v1/productos/**").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_GESTIONAR_PRODUCTOS", "GESTIONAR_PRODUCTOS")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/productos/**").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_GESTIONAR_PRODUCTOS", "GESTIONAR_PRODUCTOS")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/productos/**").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_GESTIONAR_PRODUCTOS", "GESTIONAR_PRODUCTOS")
                // Escritura en categorias y almacenes: ADMIN o trabajador con permiso GESTIONAR_CATALOGOS
                .requestMatchers(HttpMethod.POST, "/api/v1/categorias/**", "/api/v1/almacenes/**").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_GESTIONAR_CATALOGOS", "GESTIONAR_CATALOGOS")
                .requestMatchers(HttpMethod.PUT, "/api/v1/categorias/**", "/api/v1/almacenes/**").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_GESTIONAR_CATALOGOS", "GESTIONAR_CATALOGOS")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/categorias/**", "/api/v1/almacenes/**").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_GESTIONAR_CATALOGOS", "GESTIONAR_CATALOGOS")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/categorias/**", "/api/v1/almacenes/**").hasAnyAuthority(
                    "ROLE_ADMIN", "PERMISO_GESTIONAR_CATALOGOS", "GESTIONAR_CATALOGOS")
                // Reglas generales de respaldo
                .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAnyRole("ADMIN", "TRABAJADOR", "CLIENTE")
                .requestMatchers(HttpMethod.POST, "/api/v1/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
