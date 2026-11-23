package cl.duoc.tienda.seguridad;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import cl.duoc.tienda.modelo.Usuario;
import cl.duoc.tienda.repositorio.UsuarioRepositorio;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lee el header "Authorization: Bearer <token>" de cada petición. Si el token de acceso es válido
 * y el usuario sigue activo, deja al usuario autenticado para el resto de la petición.
 * El rol se lee desde la base de datos, así un cambio de rol se aplica de inmediato.
 */
public class JwtFiltro extends OncePerRequestFilter {

    static final String ATRIBUTO_ERROR = "errorJwt";

    private final JwtServicio jwt;
    private final UsuarioRepositorio usuarios;

    public JwtFiltro(JwtServicio jwt, UsuarioRepositorio usuarios) {
        this.jwt = jwt;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Long id = jwt.validar(header.substring(7), JwtServicio.TIPO_ACCESO);
                usuarios.findById(id)
                        .filter(Usuario::estaActivo)
                        .map(UsuarioPrincipal::desde)
                        .ifPresentOrElse(principal -> {
                            var autenticacion = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                            autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(autenticacion);
                        }, () -> request.setAttribute(ATRIBUTO_ERROR, "La cuenta no existe o está inactiva."));
            } catch (JwtException | IllegalArgumentException e) {
                request.setAttribute(ATRIBUTO_ERROR, "Token inválido o expirado.");
            }
        }
        chain.doFilter(request, response);
    }
}
