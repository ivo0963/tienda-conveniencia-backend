package cl.duoc.tienda.seguridad;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.tienda.repositorio.UsuarioRepositorio;

/** Spring Security lo usa en el login para buscar al usuario por su correo y comparar la contraseña BCrypt. */
@Service
public class UsuarioDetallesServicio implements UserDetailsService {

    private final UsuarioRepositorio usuarios;

    public UsuarioDetallesServicio(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) {
        return usuarios.findByCorreoIgnoreCase(correo.trim())
                .map(UsuarioPrincipal::desde)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
