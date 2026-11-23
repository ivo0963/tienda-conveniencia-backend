package cl.duoc.tienda.excepcion;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import cl.duoc.tienda.dto.ErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;

/** Convierte las excepciones en respuestas JSON con el formato ErrorRespuesta y el código HTTP adecuado. */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> validacion(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> errores.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return respuesta(HttpStatus.BAD_REQUEST, "Hay campos con errores.", req, errores);
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorRespuesta> datosInvalidos(DatosInvalidosException e, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage(), req, e.getErrores());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorRespuesta> formatoInvalido(Exception e, HttpServletRequest req) {
        log.debug("Solicitud con formato inválido en {}: {}", req.getRequestURI(), e.getMessage());
        return respuesta(HttpStatus.BAD_REQUEST, "La solicitud tiene un formato o un valor no válido.", req, null);
    }

    @ExceptionHandler({RecursoNoEncontradoException.class})
    public ResponseEntity<ErrorRespuesta> noEncontrado(RecursoNoEncontradoException e, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage(), req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRespuesta> rutaNoExiste(NoResourceFoundException e, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "La ruta solicitada no existe.", req, null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuesta> reglaNegocio(ReglaNegocioException e, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage(), req, null);
    }

    @ExceptionHandler({NoAutorizadoException.class})
    public ResponseEntity<ErrorRespuesta> noAutorizado(NoAutorizadoException e, HttpServletRequest req) {
        return respuesta(HttpStatus.UNAUTHORIZED, e.getMessage(), req, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorRespuesta> credenciales(BadCredentialsException e, HttpServletRequest req) {
        return respuesta(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.", req, null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorRespuesta> inactivo(DisabledException e, HttpServletRequest req) {
        return respuesta(HttpStatus.FORBIDDEN, "Tu cuenta está inactiva. Contacta al administrador.", req, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRespuesta> sinPermiso(AccessDeniedException e, HttpServletRequest req) {
        return respuesta(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción.", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> inesperado(Exception e, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado en el servidor.", req, null);
    }

    private static ResponseEntity<ErrorRespuesta> respuesta(HttpStatus estado, String mensaje, HttpServletRequest req,
                                                            Map<String, String> errores) {
        ErrorRespuesta cuerpo = new ErrorRespuesta(LocalDateTime.now(), estado.value(), estado.getReasonPhrase(),
                mensaje, req.getRequestURI(), errores);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
