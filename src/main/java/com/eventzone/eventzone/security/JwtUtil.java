package com.eventzone.eventzone.security;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    // 10 horas
    private final long jwtExpirationMs = 36000000;

    /**
     * Genera un token JWT con el username y roles del usuario.
     */
    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        logger.debug("🪙 Generando token JWT para usuario: {}", username);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact()
                .trim();

        logger.info("✅ Token JWT generado correctamente para usuario: {}", username);
        return token;
    }

    public boolean validateToken(String token) {
        try {
            logger.debug("🧩 Validando token JWT...");
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            logger.debug("✅ Token JWT válido.");
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("⚠️  Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.warn("⚠️  Token JWT no soportado: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warn("⚠️  Token JWT malformado: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.warn("⚠️  Firma del token JWT inválida: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️  Token JWT vacío o inválido: {}", e.getMessage());
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        try {
            String username = getClaimFromToken(token, Claims::getSubject);
            logger.debug("👤 Usuario extraído del token: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("💥 Error al obtener el usuario del token: {}", e.getMessage());
            return null;
        }
    }

    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            List<String> roles = claims.get("roles", List.class);
            logger.debug("🎭 Roles extraídos del token: {}", roles);
            return roles != null ? roles : Collections.emptyList();
        } catch (Exception e) {
            logger.error("💥 Error al obtener roles del token: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
