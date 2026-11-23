package cl.duoc.tienda.controlador;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.tienda.dto.CategoriaDto;
import cl.duoc.tienda.dto.CategoriaRequest;
import cl.duoc.tienda.dto.ErrorRespuesta;
import cl.duoc.tienda.servicio.CategoriaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorías", description = "Categorías del catálogo. Lectura pública; cambios solo administrador.")
public class CategoriaControlador {

    private final CategoriaServicio servicio;

    public CategoriaControlador(CategoriaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Público. Incluye la cantidad de productos de cada una.")
    @ApiResponse(responseCode = "200", description = "Lista de categorías")
    public List<CategoriaDto> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una categoría", description = "Público.")
    @ApiResponse(responseCode = "200", description = "Categoría encontrada")
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public CategoriaDto obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear categoría", description = "Rol: administrador. El nombre no se puede repetir.")
    @ApiResponse(responseCode = "201", description = "Categoría creada")
    @ApiResponse(responseCode = "409", description = "Nombre repetido", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public ResponseEntity<CategoriaDto> crear(@Valid @RequestBody CategoriaRequest datos) {
        CategoriaDto creada = servicio.crear(datos);
        return ResponseEntity.created(URI.create("/api/v1/categorias/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar categoría", description = "Rol: administrador.")
    @ApiResponse(responseCode = "200", description = "Categoría actualizada")
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public CategoriaDto actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest datos) {
        return servicio.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar categoría", description = "Rol: administrador. No se puede eliminar si tiene productos.")
    @ApiResponse(responseCode = "204", description = "Categoría eliminada")
    @ApiResponse(responseCode = "409", description = "Tiene productos asociados", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
