package cl.duoc.tienda.seguridad;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cl.duoc.tienda.modelo.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Genera y verifica los JWT firmados con HMAC-SHA (HS384, clave de 384 bits).
 * <ul>
 *   <li>Token de acceso: corta duración, va en el header Authorization.</li>
 *   <li>Token de refresco: larga duración, va en una cookie HttpOnly y solo sirve para pedir un nuevo token de acceso.</li>
 * </ul>
 * El claim "tipo" impide usar un token de refresco como token de acceso y viceversa.
 */
@Service
public class JwtServicio {

    public static final String TIPO_ACCESO = "acceso";
    public static final String TIPO_REFRESCO = "refresco";
    private static final String EMISOR = "minimarket-dsy";

    private final SecretKey clave;
    private final Duration duracionAcceso;
    private final Duration duracionRefresco;

    public JwtServicio(@Value("${app.jwt.secreto}") String secreto,
                       @Value("${app.jwt.minutos-acceso}") long minutosAcceso,
                       @Value("${app.jwt.dias-refresco}") long diasRefresco) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));
        this.duracionAcceso = Duration.ofMinutes(minutosAcceso);
        this.duracionRefresco = Duration.ofDays(diasRefresco);
    }

    public String generarAcceso(Usuario u) {
        return generar(u, TIPO_ACCESO, duracionAcceso);
    }

    public String generarRefresco(Usuario u) {
        return generar(u, TIPO_REFRESCO, duracionRefresco);
    }

    public Duration getDuracionAcceso() {
        return duracionAcceso;
    }

    public Duration getDuracionRefresco() {
        return duracionRefresco;
    }

    /**
     * Verifica firma, emisor, expiración y tipo. Lanza JwtException si algo no es válido.
     *
     * @return el id del usuario (claim "sub")
     */
    public Long validar(String token, String tipoEsperado) {
        Claims claims = Jwts.parser()
                .verifyWith(clave)
                .requireIssuer(EMISOR)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!tipoEsperado.equals(claims.get("tipo", String.class))) {
            throw new JwtException("El token no es de tipo " + tipoEsperado + ".");
        }
        return Long.valueOf(claims.getSubject());
    }

    private String generar(Usuario u, String tipo, Duration duracion) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .issuer(EMISOR)
                .subject(String.valueOf(u.getId()))
                .claim("correo", u.getCorreo())
                .claim("rol", u.getRol().valor())
                .claim("tipo", tipo)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracion)))
                .signWith(clave)
                .compact();
    }
}
