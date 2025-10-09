package com.eventzone.eventzone.security;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    // 10 horas
    private final long jwtExpirationMs = 36000000;

    public String generateToken(String username) {
        logger.debug("🪙 Generando token JWT para usuario: {}", username);

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();

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

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
}
