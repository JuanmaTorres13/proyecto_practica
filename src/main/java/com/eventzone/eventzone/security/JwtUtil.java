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

/**
 * Utilidad para la generación, validación y extracción de información de tokens JWT.
 * <p>
 * Esta clase centraliza todas las operaciones relacionadas con JWT:
 * <ul>
 *   <li>Generación de tokens con roles y usuario.</li>
 *   <li>Validación de tokens (firma, formato, expiración, etc.).</li>
 *   <li>Extracción del nombre de usuario y roles desde el token.</li>
 * </ul>
 * </p>
 *
 * <p>Los tokens se firman utilizando el algoritmo {@link SignatureAlgorithm#HS512} 
 * y la clave secreta definida en las propiedades de la aplicación (<code>jwt.secret</code>).</p>
 *
 * @author  
 * @version 1.0
 */
@Component
public class JwtUtil {

    /** Logger para trazas y depuración de JWT. */
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /** Clave secreta usada para firmar y verificar los tokens JWT. */
    @Value("${jwt.secret}")
    private String secret;

    /** Tiempo de expiración del token (10 horas expresadas en milisegundos). */
    private final long jwtExpirationMs = 36000000;

    /**
     * Genera un token JWT con el usuario y los roles asociados.
     *
     * @param username nombre del usuario para incluir en el token.
     * @param authorities lista de autoridades o roles del usuario.
     * @return el token JWT generado y firmado.
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

    /**
     * Valida un token JWT verificando su firma, formato y expiración.
     *
     * @param token el token JWT a validar.
     * @return {@code true} si el token es válido, {@code false} en caso contrario.
     */
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

    /**
     * Obtiene el nombre de usuario (subject) contenido en el token.
     *
     * @param token el token JWT desde el cual extraer el usuario.
     * @return el nombre de usuario o {@code null} si no se puede obtener.
     */
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

    /**
     * Extrae los roles asociados al usuario desde el token JWT.
     *
     * @param token el token JWT desde el cual extraer los roles.
     * @return lista de roles presentes en el token, o una lista vacía si no existen.
     */
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

    /**
     * Obtiene un claim específico del token usando un resolver de claims.
     *
     * @param <T> tipo del claim esperado.
     * @param token el token JWT.
     * @param claimsResolver función que define qué claim se debe extraer.
     * @return el claim solicitado.
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Obtiene todos los claims (información) contenidos en el token JWT.
     *
     * @param token el token JWT.
     * @return objeto {@link Claims} con toda la información decodificada.
     * @throws io.jsonwebtoken.JwtException si el token es inválido o no puede parsearse.
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
