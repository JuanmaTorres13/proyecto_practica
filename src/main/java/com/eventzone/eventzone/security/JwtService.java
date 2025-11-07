package com.eventzone.eventzone.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.function.Function;

/**
 * Servicio encargado de la generación, validación y análisis de tokens JWT (JSON Web Tokens).
 * 
 * <p>Esta clase se utiliza para gestionar los tokens de autenticación en el sistema. 
 * Permite crear nuevos tokens a partir del email del usuario (username), 
 * extraer información contenida en ellos y validar su vigencia.</p>
 * 
 * <p>El token se firma utilizando el algoritmo {@link SignatureAlgorithm#HS256} 
 * y una clave secreta definida en el archivo de configuración (<code>application.properties</code>).</p>
 * 
 * @author  
 * @version 1.0
 */
@Service
public class JwtService {

    /**
     * Clave secreta usada para firmar y validar los tokens JWT.
     * Se carga desde las propiedades de la aplicación (<code>jwt.secret</code>).
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Tiempo de expiración del token en milisegundos (24 horas).
     */
    private final long expirationMs = 1000 * 60 * 60 * 24;

    /**
     * Método ejecutado tras la inicialización del bean.
     * Imprime la clave secreta para confirmar su correcta carga.
     * <p><b>Nota:</b> En un entorno de producción no se recomienda mostrar la clave secreta en logs.</p>
     */
    @PostConstruct
    public void init() {
        System.out.println("JWT Secret cargada: " + secretKey);
    }

    /**
     * Extrae el nombre de usuario (email) del token JWT.
     *
     * @param token el token JWT del que se desea extraer el usuario.
     * @return el nombre de usuario contenido en el token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token JWT.
     *
     * @param token el token JWT del cual se obtendrá la fecha de expiración.
     * @return la fecha de expiración del token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un valor (claim) específico del token JWT utilizando una función de resolución.
     *
     * @param <T> el tipo de valor que se desea obtener.
     * @param token el token JWT.
     * @param claimsResolver una función que define qué información se desea extraer.
     * @return el valor del claim solicitado.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Obtiene todos los claims contenidos en el token JWT.
     *
     * @param token el token JWT a analizar.
     * @return un objeto {@link Claims} que contiene todos los datos del token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Verifica si el token JWT ha expirado.
     *
     * @param token el token JWT a verificar.
     * @return {@code true} si el token ya ha expirado, {@code false} en caso contrario.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Genera un nuevo token JWT para el usuario especificado.
     *
     * @param username el email o identificador único del usuario.
     * @return un token JWT firmado y con fecha de expiración.
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * Valida un token JWT verificando que el usuario coincida y que no haya expirado.
     *
     * @param token el token JWT a validar.
     * @param username el nombre de usuario esperado.
     * @return {@code true} si el token es válido y pertenece al usuario, {@code false} en caso contrario.
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }
}
