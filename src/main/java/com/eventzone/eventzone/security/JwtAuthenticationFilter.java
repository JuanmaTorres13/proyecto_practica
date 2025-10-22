package com.eventzone.eventzone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Ignorar rutas públicas y recursos estáticos
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (!StringUtils.hasText(jwt)) {
                // No hay token en rutas protegidas → 401
                logger.warn("❌ Ruta protegida sin token JWT: {}", path);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT requerido");
                return;
            }

            logger.debug("🔐 Token recibido: {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "...");

            if (jwtUtil.validateToken(jwt)) {
                String email = jwtUtil.getUsernameFromToken(jwt);
                List<String> roles = jwtUtil.getRolesFromToken(jwt);

                logger.info("✅ Token válido para usuario: {}", email);
                logger.debug("🎭 Roles extraídos del token: {}", roles);

                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                var authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        authorities
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("🔓 Usuario autenticado correctamente: {}", email);
            } else {
                logger.warn("❌ Token JWT inválido o expirado para ruta: {}", path);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT inválido o expirado");
                return;
            }

        } catch (Exception ex) {
            logger.error("💥 Error procesando JWT: {}", ex.getMessage(), ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error procesando JWT");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Detecta rutas públicas o recursos estáticos que no requieren JWT.
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/auth/")
                || path.startsWith("/usuarios/login")
                || path.startsWith("/usuarios/registro")
                || path.startsWith("/js/")
                || path.startsWith("/css/")
                || path.startsWith("/images/")
                || path.equals("/favicon.ico");
    }

    /**
     * Extrae el token JWT de la cabecera Authorization
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
