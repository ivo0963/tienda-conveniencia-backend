package cl.duoc.tienda.controlador;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.tienda.dto.BoletaDto;
import cl.duoc.tienda.dto.CambioEstadoRequest;
import cl.duoc.tienda.dto.CompraRequest;
import cl.duoc.tienda.dto.ErrorRespuesta;
import cl.duoc.tienda.seguridad.UsuarioPrincipal;
import cl.duoc.tienda.servicio.BoletaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/boletas")
@Tag(name = "Boletas", description = "Compras (checkout) y órdenes de compra")
public class BoletaControlador {

    private final BoletaServicio servicio;

    public BoletaControlador(BoletaServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @Operation(summary = "Comprar (checkout)",
            description = "Público: se puede comprar como invitado. Si se envía un token, la boleta queda asociada al usuario. "
                    + "Siempre crea la boleta: con estado 'pagada' o 'rechazada' (falta de stock o pago rechazado).")
    @ApiResponse(responseCode = "201", description = "Boleta creada (revisar el campo estado)")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    @ApiResponse(responseCode = "409", description = "Un producto ya no existe", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public ResponseEntity<BoletaDto> comprar(@Valid @RequestBody CompraRequest datos,
                                             @AuthenticationPrincipal UsuarioPrincipal principal) {
        BoletaDto boleta = servicio.procesarCompra(datos, principal != null ? principal.id() : null);
        return ResponseEntity.created(URI.create("/api/v1/boletas/" + boleta.id())).body(boleta);
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar órdenes", description = "Roles: administrador y vendedor. Ordenadas de la más reciente a la más antigua.")
    @ApiResponse(responseCode = "200", description = "Lista de boletas")
    @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public List<BoletaDto> listar() {
        return servicio.listar();
    }

    @GetMapping("/mias")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mis compras", description = "Cualquier usuario con sesión: sus propias boletas.")
    @ApiResponse(responseCode = "200", description = "Boletas del usuario conectado")
    public List<BoletaDto> mias(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return servicio.listarDeUsuario(principal.id());
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Detalle de una boleta", description = "Administrador y vendedor ven cualquiera; un cliente solo las suyas.")
    @ApiResponse(responseCode = "200", description = "Boleta encontrada")
    @ApiResponse(responseCode = "403", description = "La boleta es de otro usuario", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public BoletaDto obtener(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return servicio.obtener(id, principal.id(), principal.rol());
    }

    @PatchMapping("/{id}/estado")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cambiar el estado de una boleta", description = "Rol: administrador. Estados: pagada, enviada, entregada.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "409", description = "La boleta fue rechazada y no puede cambiar", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public BoletaDto cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest datos) {
        return servicio.cambiarEstado(id, datos.estado());
    }
}
