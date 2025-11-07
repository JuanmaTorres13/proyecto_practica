package com.eventzone.eventzone.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración principal de seguridad para la aplicación.
 * <p>
 * Define la política de autenticación basada en JWT, desactiva el uso de sesiones,
 * configura los filtros de autenticación, y gestiona los permisos de acceso a las rutas.
 * </p>
 *
 * <p>
 * Esta clase utiliza {@link UsuarioDetailsService} para la carga de usuarios desde la base de datos
 * y {@link JwtAuthenticationFilter} para la validación de tokens JWT.
 * </p>
 *
 * @author 
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructor de la configuración de seguridad.
     *
     * @param usuarioDetailsService servicio encargado de cargar los usuarios para la autenticación
     * @param jwtAuthenticationFilter filtro que intercepta las peticiones y valida el token JWT
     */
    public SecurityConfig(UsuarioDetailsService usuarioDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.usuarioDetailsService = usuarioDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configura la cadena de filtros de seguridad de Spring Security.
     * <ul>
     *     <li>Desactiva CSRF por el uso de JWT.</li>
     *     <li>Establece la política de sesión como stateless.</li>
     *     <li>Define los permisos de acceso a las diferentes rutas.</li>
     *     <li>Agrega el filtro de autenticación JWT antes del filtro de usuario y contraseña.</li>
     * </ul>
     *
     * @param http objeto {@link HttpSecurity} utilizado para configurar la seguridad HTTP
     * @return una instancia de {@link SecurityFilterChain} con la configuración aplicada
     * @throws Exception si ocurre un error al construir la configuración de seguridad
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/usuarios/registro",
                                "/usuarios/login",
                                "/usuarios/verificar",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/usuarios/profile").authenticated()
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/user/**").hasAuthority("USER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .build();
    }

    /**
     * Define el codificador de contraseñas utilizado por Spring Security.
     *
     * @return una instancia de {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Proveedor de autenticación que utiliza {@link UsuarioDetailsService}
     * para cargar los usuarios y el {@link PasswordEncoder} para validar contraseñas.
     *
     * @return un {@link DaoAuthenticationProvider} configurado
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Expone el {@link AuthenticationManager} como bean para que pueda inyectarse
     * en otros componentes (por ejemplo, controladores de autenticación).
     *
     * @param config configuración de autenticación provista por Spring
     * @return el {@link AuthenticationManager} configurado
     * @throws Exception si ocurre un error al obtener el administrador de autenticación
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
