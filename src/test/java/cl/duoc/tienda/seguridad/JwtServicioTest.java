package cl.duoc.tienda.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import cl.duoc.tienda.modelo.Rol;
import cl.duoc.tienda.modelo.Usuario;
import io.jsonwebtoken.JwtException;

class JwtServicioTest {

    private static final String SECRETO = "OzKdndZfJwLwAlOTkj6figFWs+ppCG62kMqneozRXRExQqbuXhfBGcDkyd7Dugks";
    private static final String OTRO_SECRETO = "c2VjcmV0by1kaXN0aW50by1wYXJhLWxhcy1wcnVlYmFzLWRlLWp3dC0xMjM0NTY3OA==";

    private final JwtServicio jwt = new JwtServicio(SECRETO, 15, 7);

    private static Usuario usuario() {
        Usuario u = new Usuario("123456785", "Juan", "Pérez", "juan.perez@gmail.com", "hash", Rol.CLIENTE);
        ReflectionTestUtils.setField(u, "id", 3L);
        return u;
    }

    @Test
    void generaTokenDeAccesoYDevuelveElIdDelUsuario() {
        String token = jwt.generarAcceso(usuario());
        assertThat(jwt.validar(token, JwtServicio.TIPO_ACCESO)).isEqualTo(3L);
    }

    @Test
    void noAceptaUnTokenDeRefrescoComoTokenDeAcceso() {
        String refresco = jwt.generarRefresco(usuario());
        assertThatThrownBy(() -> jwt.validar(refresco, JwtServicio.TIPO_ACCESO)).isInstanceOf(JwtException.class);
        assertThat(jwt.validar(refresco, JwtServicio.TIPO_REFRESCO)).isEqualTo(3L);
    }

    @Test
    void rechazaTokenFirmadoConOtraClave() {
        String ajeno = new JwtServicio(OTRO_SECRETO, 15, 7).generarAcceso(usuario());
        assertThatThrownBy(() -> jwt.validar(ajeno, JwtServicio.TIPO_ACCESO)).isInstanceOf(JwtException.class);
    }

    @Test
    void rechazaTokenModificado() {
        String token = jwt.generarAcceso(usuario());
        String alterado = token.substring(0, token.length() - 3) + (token.endsWith("a") ? "bbb" : "aaa");
        assertThatThrownBy(() -> jwt.validar(alterado, JwtServicio.TIPO_ACCESO)).isInstanceOf(JwtException.class);
    }

    @Test
    void rechazaTokenExpirado() {
        String expirado = new JwtServicio(SECRETO, 0, 7).generarAcceso(usuario());
        assertThatThrownBy(() -> jwt.validar(expirado, JwtServicio.TIPO_ACCESO)).isInstanceOf(JwtException.class);
    }
}
