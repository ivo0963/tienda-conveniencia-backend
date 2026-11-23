package cl.duoc.tienda.servicio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.tienda.dto.CategoriaDto;
import cl.duoc.tienda.dto.CategoriaRequest;
import cl.duoc.tienda.excepcion.RecursoNoEncontradoException;
import cl.duoc.tienda.excepcion.ReglaNegocioException;
import cl.duoc.tienda.modelo.Categoria;
import cl.duoc.tienda.repositorio.CategoriaRepositorio;
import cl.duoc.tienda.repositorio.ProductoRepositorio;

@Service
public class CategoriaServicio {

    private static final String IMAGEN_POR_DEFECTO = "/img/sin-imagen.svg";

    private final CategoriaRepositorio categorias;
    private final ProductoRepositorio productos;

    public CategoriaServicio(CategoriaRepositorio categorias, ProductoRepositorio productos) {
        this.categorias = categorias;
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDto> listar() {
        return categorias.findAll().stream()
                .map(c -> CategoriaDto.desde(c, productos.countByCategoriaId(c.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDto obtener(Long id) {
        Categoria c = buscar(id);
        return CategoriaDto.desde(c, productos.countByCategoriaId(id));
    }

    @Transactional
    public CategoriaDto crear(CategoriaRequest datos) {
        String nombre = datos.nombre().trim();
        if (categorias.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe una categoría con ese nombre.");
        }
        Categoria c = categorias.save(new Categoria(nombre, limpiar(datos.descripcion()), imagen(datos.imagen())));
        return CategoriaDto.desde(c, 0);
    }

    @Transactional
    public CategoriaDto actualizar(Long id, CategoriaRequest datos) {
        Categoria c = buscar(id);
        String nombre = datos.nombre().trim();
        if (categorias.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe una categoría con ese nombre.");
        }
        c.setNombre(nombre);
        c.setDescripcion(limpiar(datos.descripcion()));
        c.setImagen(imagen(datos.imagen()));
        return CategoriaDto.desde(c, productos.countByCategoriaId(id));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria c = buscar(id);
        if (productos.countByCategoriaId(id) > 0) {
            throw new ReglaNegocioException("No se puede eliminar \"" + c.getNombre() + "\" porque tiene productos asociados.");
        }
        categorias.delete(c);
    }

    Categoria buscar(Long id) {
        return categorias.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id));
    }

    private static String limpiar(String texto) {
        return texto == null ? null : texto.trim();
    }

    private static String imagen(String ruta) {
        return ruta == null || ruta.isBlank() ? IMAGEN_POR_DEFECTO : ruta.trim();
    }
}
