package cl.duoc.tienda.seguridad;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import cl.duoc.tienda.modelo.Rol;
import cl.duoc.tienda.modelo.Usuario;

/** Usuario autenticado tal como lo ve Spring Security. La autoridad es ROLE_ADMIN, ROLE_VENDEDOR o ROLE_CLIENTE. */
public record UsuarioPrincipal(Long id, String correo, String password, Rol rol, boolean activo) implements UserDetails {

    public static UsuarioPrincipal desde(Usuario u) {
        return new UsuarioPrincipal(u.getId(), u.getCorreo(), u.getPassword(), u.getRol(), u.estaActivo());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
