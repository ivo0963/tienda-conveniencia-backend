package cl.duoc.tienda.dto;

import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Formato común de todas las respuestas de error")
public record ErrorRespuesta(
        LocalDateTime fecha,
        @Schema(example = "400") int estado,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "Hay campos con errores.") String mensaje,
        @Schema(example = "/api/v1/productos") String ruta,
        @Schema(description = "Errores por campo, cuando falla la validación") Map<String, String> errores) {
}
