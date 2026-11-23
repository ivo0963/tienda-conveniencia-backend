package cl.duoc.tienda.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.tienda.modelo.Rol;
import cl.duoc.tienda.modelo.Usuario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);

    boolean existsByRun(String run);

    boolean existsByRunAndIdNot(String run, Long id);

    long countByRol(Rol rol);

    List<Usuario> findAllByOrderByIdAsc();
}
