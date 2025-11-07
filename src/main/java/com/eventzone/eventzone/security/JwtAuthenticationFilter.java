package com.eventzone.eventzone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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

/**
 * Filtro de autenticación JWT que intercepta todas las solicitudes HTTP
 * y valida la presencia y validez del token JWT.
 * <p>
 * Si el token es válido, se establece la autenticación del usuario en el
 * contexto de seguridad de Spring Security. En caso contrario, devuelve
 * un error 401 (Unauthorized).
 * </p>
 *
 * <p>Extiende {@link OncePerRequestFilter}, lo que garantiza que el filtro
 * se ejecute una sola vez por cada solicitud.</p>
 *
 * @author 
 * @version 1.0
 * @since 2025
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    /**
     * Constructor que inyecta la utilidad JWT.
     *
     * @param jwtUtil clase auxiliar para generar y validar tokens JWT.
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Método principal del filtro. Se ejecuta en cada solicitud HTTP
     * para comprobar si el token JWT está presente y es válido.
     *
     * @param request     la solicitud HTTP entrante.
     * @param response    la respuesta HTTP que se enviará.
     * @param filterChain la cadena de filtros para continuar la ejecución.
     * @throws ServletException si ocurre un error en el procesamiento del filtro.
     * @throws IOException      si ocurre un error de entrada/salida.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Ignora rutas públicas y recursos estáticos
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
     * Verifica si la ruta solicitada corresponde a un recurso público o estático
     * que no requiere autenticación.
     *
     * @param path la ruta de la solicitud HTTP.
     * @return {@code true} si la ruta es pública, {@code false} si requiere autenticación.
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/auth/")
                || path.startsWith("/usuarios/login")
                || path.startsWith("/usuarios/registro")
                || path.startsWith("/usuarios/verificar")
                || path.startsWith("/js/")
                || path.startsWith("/css/")
                || path.startsWith("/images/")
                || path.equals("/favicon.ico");
    }

    /**
     * Extrae el token JWT desde la cookie "jwt_token" o, si no está presente,
     * desde el encabezado HTTP "Authorization" (formato Bearer).
     *
     * @param request la solicitud HTTP entrante.
     * @return el token JWT si está presente, o {@code null} si no se encuentra.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // Intentar obtener el token desde la cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // Si no existe cookie, buscar en el header Authorization (opcional)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
