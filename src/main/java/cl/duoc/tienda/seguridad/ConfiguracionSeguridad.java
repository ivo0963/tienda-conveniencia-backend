package cl.duoc.tienda.seguridad;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import cl.duoc.tienda.repositorio.UsuarioRepositorio;

/**
 * Seguridad de la API: sin sesiones de servidor (stateless), autenticación con JWT
 * y permisos por rol definidos aquí para cada ruta.
 *
 * <pre>
 * Público ........ login, registro, refresh, logout, catálogo (GET productos y categorías), comprar, Swagger
 * Autenticado .... /auth/me, /perfil, /boletas/mias, GET /boletas/{id} (un cliente solo ve las suyas)
 * Vendedor ....... GET /boletas (listado y detalle de órdenes)
 * Administrador .. todo lo anterior + CRUD de productos, categorías y usuarios, estado de boletas, reportes
 * </pre>
 */
@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    private static final String API = "/api/v1";

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http, JwtServicio jwt, UsuarioRepositorio usuarios,
                                               RespuestasSeguridad respuestas) throws Exception {
        http
                // La API no usa cookies de sesión para autenticar (el JWT va en el header), por eso CSRF no aplica.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(respuestas).accessDeniedHandler(respuestas))
                .authorizeHttpRequests(rutas -> rutas
                        // Documentación
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        // Autenticación
                        .requestMatchers(HttpMethod.POST, API + "/auth/login", API + "/auth/registro",
                                API + "/auth/refresh", API + "/auth/logout").permitAll()
                        // Catálogo: lectura pública, escritura solo administrador
                        .requestMatchers(HttpMethod.GET, API + "/productos/criticos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, API + "/productos/**", API + "/categorias/**").permitAll()
                        .requestMatchers(API + "/productos/**", API + "/categorias/**").hasRole("ADMIN")
                        // Boletas: cualquiera puede comprar (también como invitado)
                        .requestMatchers(HttpMethod.POST, API + "/boletas").permitAll()
                        .requestMatchers(HttpMethod.GET, API + "/boletas/mias").authenticated()
                        .requestMatchers(HttpMethod.GET, API + "/boletas").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.GET, API + "/boletas/*").authenticated()
                        .requestMatchers(API + "/boletas/**").hasRole("ADMIN")
                        // Administración
                        .requestMatchers(API + "/usuarios/**", API + "/reportes/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtFiltro(jwt, usuarios), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    /** Solo el frontend autorizado puede llamar a la API desde el navegador; se permiten credenciales para la cookie de refresco. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origenes}") List<String> origenes) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(origenes);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", cors);
        return fuente;
    }
}
