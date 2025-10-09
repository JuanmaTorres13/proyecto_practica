package com.eventzone.eventzone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        logger.debug("➡️  [JwtFilter] Petición entrante: {}", path);

        try {
            String jwt = getJwtFromRequest(request);

            if (jwt == null) {
                logger.debug("⚠️  No se encontró token JWT en la cabecera Authorization.");
            } else {
                logger.debug("🔐 Token recibido: {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "...");

                if (jwtUtil.validateToken(jwt)) {
                    String email = jwtUtil.getUsernameFromToken(jwt);
                    logger.info("✅ Token válido para usuario: {}", email);

                    var userDetails = usuarioDetailsService.loadUserByUsername(email);
                    logger.debug("🧍 Usuario cargado: {}", userDetails.getUsername());
                    logger.debug("🎭 Roles del usuario: {}", userDetails.getAuthorities());

                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("🔓 Usuario autenticado correctamente en el contexto de seguridad: {}", email);

                } else {
                    logger.warn("❌ Token JWT inválido o expirado.");
                }
            }
        } catch (Exception ex) {
            logger.error("💥 Error procesando JWT: {}", ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
