package cl.duoc.tienda.controlador;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.tienda.dto.ErrorRespuesta;
import cl.duoc.tienda.dto.ProductoDto;
import cl.duoc.tienda.dto.ProductoRequest;
import cl.duoc.tienda.dto.ReponerStockRequest;
import cl.duoc.tienda.servicio.ProductoServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Catálogo. Lectura pública; crear, editar y eliminar solo administrador.")
public class ProductoControlador {

    private final ProductoServicio servicio;

    public ProductoControlador(ProductoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = "Público. Se puede filtrar por categoría.")
    @ApiResponse(responseCode = "200", description = "Lista de productos")
    public List<ProductoDto> listar(@Parameter(description = "Id de la categoría (opcional)") @RequestParam(required = false) Long categoriaId) {
        return servicio.listar(categoriaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un producto", description = "Público.")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public ProductoDto obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @GetMapping("/criticos")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Productos con stock crítico", description = "Rol: administrador. Productos con stock igual o menor a su stock crítico.")
    @ApiResponse(responseCode = "200", description = "Lista de productos críticos")
    @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public List<ProductoDto> criticos() {
        return servicio.listarCriticos();
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear producto", description = "Rol: administrador. El código no se puede repetir.")
    @ApiResponse(responseCode = "201", description = "Producto creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    @ApiResponse(responseCode = "409", description = "Código repetido", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public ResponseEntity<ProductoDto> crear(@Valid @RequestBody ProductoRequest datos) {
        ProductoDto creado = servicio.crear(datos);
        return ResponseEntity.created(URI.create("/api/v1/productos/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar producto", description = "Rol: administrador.")
    @ApiResponse(responseCode = "200", description = "Producto actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public ProductoDto actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest datos) {
        return servicio.actualizar(id, datos);
    }

    @PatchMapping("/{id}/stock")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reponer stock", description = "Rol: administrador. Suma unidades al stock actual.")
    @ApiResponse(responseCode = "200", description = "Stock actualizado")
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public ProductoDto reponer(@PathVariable Long id, @Valid @RequestBody ReponerStockRequest datos) {
        return servicio.reponerStock(id, datos.cantidad());
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar producto", description = "Rol: administrador. Las boletas que lo incluyen conservan su nombre y precio.")
    @ApiResponse(responseCode = "204", description = "Producto eliminado")
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
