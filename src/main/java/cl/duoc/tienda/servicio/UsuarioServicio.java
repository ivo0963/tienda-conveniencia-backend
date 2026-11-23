package cl.duoc.tienda.servicio;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.tienda.dto.BoletaDto;
import cl.duoc.tienda.dto.PerfilRequest;
import cl.duoc.tienda.dto.RegistroRequest;
import cl.duoc.tienda.dto.UsuarioDto;
import cl.duoc.tienda.dto.UsuarioRequest;
import cl.duoc.tienda.excepcion.DatosInvalidosException;
import cl.duoc.tienda.excepcion.RecursoNoEncontradoException;
import cl.duoc.tienda.excepcion.ReglaNegocioException;
import cl.duoc.tienda.modelo.EstadoUsuario;
import cl.duoc.tienda.modelo.Rol;
import cl.duoc.tienda.modelo.Usuario;
import cl.duoc.tienda.repositorio.BoletaRepositorio;
import cl.duoc.tienda.repositorio.UsuarioRepositorio;
import cl.duoc.tienda.validacion.Validaciones;

@Service
public class UsuarioServicio {

    private final UsuarioRepositorio usuarios;
    private final BoletaRepositorio boletas;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServicio(UsuarioRepositorio usuarios, BoletaRepositorio boletas, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.boletas = boletas;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listar() {
        return usuarios.findAllByOrderByIdAsc().stream().map(UsuarioDto::desde).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDto obtener(Long id) {
        return UsuarioDto.desde(buscar(id));
    }

    @Transactional
    public UsuarioDto crear(UsuarioRequest datos) {
        if (datos.password() == null || datos.password().isBlank()) {
            throw new DatosInvalidosException("password", "La contraseña es obligatoria.");
        }
        String run = Validaciones.normalizarRun(datos.run());
        validarUnicos(datos.correo(), run, null);
        Usuario u = new Usuario(run, datos.nombre().trim(), datos.apellidos().trim(), datos.correo().trim(),
                passwordEncoder.encode(datos.password()), datos.rol());
        u.setEstado(datos.estado() == null ? EstadoUsuario.ACTIVO : datos.estado());
        copiarDireccion(u, datos.region(), datos.comuna(), datos.direccion(), datos.departamento());
        return UsuarioDto.desde(usuarios.save(u));
    }

    /** Registro público: siempre crea un cliente activo. */
    @Transactional
    public Usuario registrarCliente(RegistroRequest datos) {
        String run = Validaciones.normalizarRun(datos.run());
        validarUnicos(datos.correo(), run, null);
        Usuario u = new Usuario(run, datos.nombre().trim(), datos.apellidos().trim(), datos.correo().trim(),
                passwordEncoder.encode(datos.password()), Rol.CLIENTE);
        copiarDireccion(u, datos.region(), datos.comuna(), datos.direccion(), datos.departamento());
        return usuarios.save(u);
    }

    @Transactional
    public UsuarioDto actualizar(Long id, UsuarioRequest datos, Long idSolicitante) {
        Usuario u = buscar(id);
        String run = Validaciones.normalizarRun(datos.run());
        validarUnicos(datos.correo(), run, id);
        if (id.equals(idSolicitante) && (datos.rol() != u.getRol() || datos.estado() == EstadoUsuario.INACTIVO)) {
            throw new ReglaNegocioException("No puedes cambiar tu propio rol ni desactivar tu propia cuenta.");
        }
        u.setRun(run);
        u.setNombre(datos.nombre().trim());
        u.setApellidos(datos.apellidos().trim());
        u.setCorreo(datos.correo().trim());
        u.setRol(datos.rol());
        if (datos.estado() != null) {
            u.setEstado(datos.estado());
        }
        if (datos.password() != null && !datos.password().isBlank()) {
            u.setPassword(passwordEncoder.encode(datos.password()));
        }
        copiarDireccion(u, datos.region(), datos.comuna(), datos.direccion(), datos.departamento());
        return UsuarioDto.desde(u);
    }

    @Transactional
    public void eliminar(Long id, Long idSolicitante) {
        if (id.equals(idSolicitante)) {
            throw new ReglaNegocioException("No puedes eliminar tu propia cuenta.");
        }
        Usuario u = buscar(id);
        // Las boletas del usuario se conservan como compras de invitado (con sus datos copiados).
        boletas.buscarHistorial(id, u.getCorreo()).stream()
                .filter(b -> b.getUsuario() != null && b.getUsuario().getId().equals(id))
                .forEach(b -> b.setUsuario(null));
        usuarios.delete(u);
    }

    @Transactional(readOnly = true)
    public List<BoletaDto> historial(Long id) {
        Usuario u = buscar(id);
        return boletas.buscarHistorial(id, u.getCorreo()).stream().map(BoletaDto::desde).toList();
    }

    @Transactional
    public UsuarioDto actualizarPerfil(Long id, PerfilRequest datos) {
        Usuario u = buscar(id);
        validarUnicos(datos.correo(), u.getRun(), id);
        u.setNombre(datos.nombre().trim());
        u.setApellidos(datos.apellidos().trim());
        u.setCorreo(datos.correo().trim());
        if (datos.password() != null && !datos.password().isBlank()) {
            u.setPassword(passwordEncoder.encode(datos.password()));
        }
        copiarDireccion(u, datos.region(), datos.comuna(), datos.direccion(), datos.departamento());
        return UsuarioDto.desde(u);
    }

    @Transactional(readOnly = true)
    public Usuario buscarActivo(Long id) {
        Usuario u = buscar(id);
        if (!u.estaActivo()) {
            throw new ReglaNegocioException("La cuenta está inactiva.");
        }
        return u;
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    private void validarUnicos(String correo, String run, Long idActual) {
        boolean correoRepetido = idActual == null
                ? usuarios.existsByCorreoIgnoreCase(correo.trim())
                : usuarios.existsByCorreoIgnoreCaseAndIdNot(correo.trim(), idActual);
        if (correoRepetido) {
            throw new ReglaNegocioException("Ya existe una cuenta con ese correo.");
        }
        boolean runRepetido = idActual == null ? usuarios.existsByRun(run) : usuarios.existsByRunAndIdNot(run, idActual);
        if (runRepetido) {
            throw new ReglaNegocioException("Ya existe una cuenta con ese RUN.");
        }
    }

    private static void copiarDireccion(Usuario u, String region, String comuna, String direccion, String departamento) {
        u.setRegion(region);
        u.setComuna(comuna);
        u.setDireccion(direccion);
        u.setDepartamento(departamento);
    }
}
