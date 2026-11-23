package cl.duoc.tienda.controlador;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.tienda.dto.AuthResponse;
import cl.duoc.tienda.dto.ErrorRespuesta;
import cl.duoc.tienda.dto.LoginRequest;
import cl.duoc.tienda.dto.RegistroRequest;
import cl.duoc.tienda.dto.UsuarioDto;
import cl.duoc.tienda.excepcion.NoAutorizadoException;
import cl.duoc.tienda.modelo.Usuario;
import cl.duoc.tienda.repositorio.UsuarioRepositorio;
import cl.duoc.tienda.seguridad.JwtServicio;
import cl.duoc.tienda.seguridad.UsuarioPrincipal;
import cl.duoc.tienda.servicio.UsuarioServicio;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión, registro y renovación de tokens JWT")
public class AuthControlador {

    /** Nombre de la cookie HttpOnly que guarda el token de refresco. */
    public static final String COOKIE_REFRESCO = "refresh_token";

    private final AuthenticationManager authenticationManager;
    private final JwtServicio jwt;
    private final UsuarioServicio usuarioServicio;
    private final UsuarioRepositorio usuarios;
    private final boolean cookieSegura;

    public AuthControlador(AuthenticationManager authenticationManager, JwtServicio jwt, UsuarioServicio usuarioServicio,
                           UsuarioRepositorio usuarios, @Value("${app.jwt.cookie-segura}") boolean cookieSegura) {
        this.authenticationManager = authenticationManager;
        this.jwt = jwt;
        this.usuarioServicio = usuarioServicio;
        this.usuarios = usuarios;
        this.cookieSegura = cookieSegura;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Valida correo y contraseña. Devuelve el token de acceso en el cuerpo y deja el token de refresco en una cookie HttpOnly.")
    @ApiResponse(responseCode = "200", description = "Sesión iniciada")
    @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    @ApiResponse(responseCode = "403", description = "Cuenta inactiva", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest datos) {
        var autenticacion = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(datos.correo().trim(), datos.password()));
        UsuarioPrincipal principal = (UsuarioPrincipal) autenticacion.getPrincipal();
        Usuario usuario = usuarios.findById(principal.id()).orElseThrow();
        return iniciarSesion(usuario, HttpStatus.OK);
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar un cliente", description = "Crea una cuenta con rol cliente e inicia la sesión.")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    @ApiResponse(responseCode = "409", description = "El correo o el RUN ya están registrados", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest datos) {
        Usuario usuario = usuarioServicio.registrarCliente(datos);
        return iniciarSesion(usuario, HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar el token de acceso",
            description = "Usa la cookie HttpOnly con el token de refresco para entregar un nuevo token de acceso. "
                    + "También renueva la cookie (rotación). El frontend lo llama al recargar la página y cuando el token de acceso expira.")
    @ApiResponse(responseCode = "200", description = "Token renovado")
    @ApiResponse(responseCode = "401", description = "No hay sesión o el token de refresco expiró", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public ResponseEntity<AuthResponse> refrescar(
            @CookieValue(name = COOKIE_REFRESCO, required = false) String tokenRefresco) {
        if (tokenRefresco == null || tokenRefresco.isBlank()) {
            throw new NoAutorizadoException("No hay una sesión activa.");
        }
        Long id;
        try {
            id = jwt.validar(tokenRefresco, JwtServicio.TIPO_REFRESCO);
        } catch (JwtException | IllegalArgumentException e) {
            throw new NoAutorizadoException("La sesión expiró. Inicia sesión nuevamente.");
        }
        Usuario usuario = usuarios.findById(id)
                .filter(Usuario::estaActivo)
                .orElseThrow(() -> new NoAutorizadoException("La cuenta no existe o está inactiva."));
        return iniciarSesion(usuario, HttpStatus.OK);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Borra la cookie con el token de refresco.")
    @ApiResponse(responseCode = "204", description = "Sesión cerrada")
    public ResponseEntity<Void> logout() {
        ResponseCookie borrar = cookie("", Duration.ZERO);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, borrar.toString()).build();
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Usuario conectado", description = "Devuelve los datos del usuario dueño del token de acceso.")
    @ApiResponse(responseCode = "200", description = "Usuario autenticado")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public UsuarioDto yo(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return usuarioServicio.obtener(principal.id());
    }

    private ResponseEntity<AuthResponse> iniciarSesion(Usuario usuario, HttpStatus estado) {
        ResponseCookie refresco = cookie(jwt.generarRefresco(usuario), jwt.getDuracionRefresco());
        AuthResponse cuerpo = new AuthResponse(jwt.generarAcceso(usuario), jwt.getDuracionAcceso().toSeconds(), UsuarioDto.desde(usuario));
        return ResponseEntity.status(estado).header(HttpHeaders.SET_COOKIE, refresco.toString()).body(cuerpo);
    }

    /** HttpOnly: JavaScript no puede leerla. SameSite=Strict: no se envía desde otros sitios. Path: solo viaja a /auth. */
    private ResponseCookie cookie(String valor, Duration duracion) {
        return ResponseCookie.from(COOKIE_REFRESCO, valor)
                .httpOnly(true)
                .secure(cookieSegura)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(duracion)
                .build();
    }
}
