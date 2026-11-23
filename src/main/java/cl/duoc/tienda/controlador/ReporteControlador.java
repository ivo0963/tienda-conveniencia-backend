package cl.duoc.tienda.controlador;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.tienda.dto.ResumenDto;
import cl.duoc.tienda.servicio.ReporteServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/reportes")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reportes", description = "Indicadores de ventas e inventario. Rol: administrador.")
public class ReporteControlador {

    private final ReporteServicio servicio;

    public ReporteControlador(ReporteServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/resumen")
    @Operation(summary = "Resumen de ventas e inventario", description = "Datos del dashboard y de las vistas de reportes.")
    @ApiResponse(responseCode = "200", description = "Resumen calculado")
    public ResumenDto resumen() {
        return servicio.resumen();
    }
}
