package cl.duoc.tienda.controlador;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.tienda.dto.BoletaDto;
import cl.duoc.tienda.dto.ErrorRespuesta;
import cl.duoc.tienda.dto.UsuarioDto;
import cl.duoc.tienda.dto.UsuarioRequest;
import cl.duoc.tienda.seguridad.UsuarioPrincipal;
import cl.duoc.tienda.servicio.UsuarioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/usuarios")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Gestión de usuarios y roles. Rol: administrador.")
public class UsuarioControlador {

    private final UsuarioServicio servicio;

    public UsuarioControlador(UsuarioServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public List<UsuarioDto> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un usuario")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public UsuarioDto obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @GetMapping("/{id}/boletas")
    @Operation(summary = "Historial de compras de un usuario", description = "Incluye las compras hechas como invitado con su mismo correo.")
    @ApiResponse(responseCode = "200", description = "Boletas del usuario")
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public List<BoletaDto> historial(@PathVariable Long id) {
        return servicio.historial(id);
    }

    @PostMapping
    @Operation(summary = "Crear usuario", description = "La contraseña es obligatoria. Correo y RUN no se pueden repetir.")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    @ApiResponse(responseCode = "409", description = "Correo o RUN repetido", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public ResponseEntity<UsuarioDto> crear(@Valid @RequestBody UsuarioRequest datos) {
        UsuarioDto creado = servicio.crear(datos);
        return ResponseEntity.created(URI.create("/api/v1/usuarios/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Si la contraseña viene vacía se mantiene la actual. Un administrador no puede cambiar su propio rol ni desactivarse.")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado")
    @ApiResponse(responseCode = "409", description = "Correo o RUN repetido", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public UsuarioDto actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest datos,
                                 @AuthenticationPrincipal UsuarioPrincipal principal) {
        return servicio.actualizar(id, datos, principal.id());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Un administrador no puede eliminar su propia cuenta. Sus boletas se conservan.")
    @ApiResponse(responseCode = "204", description = "Usuario eliminado")
    @ApiResponse(responseCode = "409", description = "Intento de eliminar la propia cuenta", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        servicio.eliminar(id, principal.id());
        return ResponseEntity.noContent().build();
    }
}
