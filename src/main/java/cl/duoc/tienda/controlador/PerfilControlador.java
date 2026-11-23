package cl.duoc.tienda.controlador;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.tienda.dto.ErrorRespuesta;
import cl.duoc.tienda.dto.PerfilRequest;
import cl.duoc.tienda.dto.UsuarioDto;
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
@RequestMapping("/api/v1/perfil")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Perfil", description = "Datos de la cuenta del usuario conectado. Cualquier usuario con sesión.")
public class PerfilControlador {

    private final UsuarioServicio servicio;

    public PerfilControlador(UsuarioServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Ver mi perfil")
    @ApiResponse(responseCode = "200", description = "Datos del usuario conectado")
    public UsuarioDto obtener(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return servicio.obtener(principal.id());
    }

    @PutMapping
    @Operation(summary = "Actualizar mi perfil", description = "No permite cambiar el rol ni el estado de la propia cuenta.")
    @ApiResponse(responseCode = "200", description = "Perfil actualizado")
    @ApiResponse(responseCode = "409", description = "Correo usado por otra cuenta", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public UsuarioDto actualizar(@AuthenticationPrincipal UsuarioPrincipal principal, @Valid @RequestBody PerfilRequest datos) {
        return servicio.actualizarPerfil(principal.id(), datos);
    }
}
