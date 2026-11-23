package cl.duoc.tienda.seguridad;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Respuestas JSON para 401 (sin sesión o token inválido) y 403 (rol sin permiso), con el mismo formato que ErrorRespuesta. */
@Component
public class RespuestasSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        Object errorJwt = request.getAttribute(JwtFiltro.ATRIBUTO_ERROR);
        String mensaje = errorJwt != null ? errorJwt.toString() : "Debes iniciar sesión para acceder a este recurso.";
        escribir(response, request, HttpStatus.UNAUTHORIZED, mensaje);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException {
        escribir(response, request, HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción.");
    }

    private static void escribir(HttpServletResponse response, HttpServletRequest request, HttpStatus estado, String mensaje)
            throws IOException {
        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {"fecha":"%s","estado":%d,"error":"%s","mensaje":"%s","ruta":"%s","errores":null}"""
                .formatted(LocalDateTime.now(), estado.value(), estado.getReasonPhrase(), json(mensaje), json(request.getRequestURI())));
    }

    private static String json(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
