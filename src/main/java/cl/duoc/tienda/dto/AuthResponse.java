package cl.duoc.tienda.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de login, registro y renovación. El token de refresco viaja en una cookie HttpOnly, no en el cuerpo.")
public record AuthResponse(
        @Schema(description = "JWT de acceso; se envía en el header Authorization: Bearer <token>") String token,
        @Schema(example = "Bearer") String tipo,
        @Schema(description = "Segundos de validez del token de acceso", example = "900") long expiraEn,
        UsuarioDto usuario) {

    public AuthResponse(String token, long expiraEn, UsuarioDto usuario) {
        this(token, "Bearer", expiraEn, usuario);
    }
}
