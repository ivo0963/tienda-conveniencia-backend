package cl.duoc.tienda.servicio;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.tienda.dto.ProductoDto;
import cl.duoc.tienda.dto.ProductoRequest;
import cl.duoc.tienda.excepcion.DatosInvalidosException;
import cl.duoc.tienda.excepcion.RecursoNoEncontradoException;
import cl.duoc.tienda.excepcion.ReglaNegocioException;
import cl.duoc.tienda.modelo.Producto;
import cl.duoc.tienda.repositorio.DetalleBoletaRepositorio;
import cl.duoc.tienda.repositorio.ProductoRepositorio;

@Service
public class ProductoServicio {

    private static final String IMAGEN_POR_DEFECTO = "/img/sin-imagen.svg";

    private final ProductoRepositorio productos;
    private final DetalleBoletaRepositorio detalles;
    private final CategoriaServicio categorias;

    public ProductoServicio(ProductoRepositorio productos, DetalleBoletaRepositorio detalles, CategoriaServicio categorias) {
        this.productos = productos;
        this.detalles = detalles;
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public List<ProductoDto> listar(Long categoriaId) {
        List<Producto> lista = categoriaId == null
                ? productos.findAllByOrderByIdAsc()
                : productos.findByCategoriaIdOrderByIdAsc(categoriaId);
        return lista.stream().map(ProductoDto::desde).toList();
    }

    @Transactional(readOnly = true)
    public ProductoDto obtener(Long id) {
        return ProductoDto.desde(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<ProductoDto> listarCriticos() {
        return productos.buscarCriticos().stream().map(ProductoDto::desde).toList();
    }

    @Transactional
    public ProductoDto crear(ProductoRequest datos) {
        String codigo = datos.codigo().trim().toUpperCase(Locale.ROOT);
        validarOferta(datos);
        if (productos.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException("Ya existe un producto con el código " + codigo + ".");
        }
        Producto p = new Producto(codigo, datos.nombre().trim(), limpiar(datos.descripcion()), datos.precio(),
                datos.precioOferta(), datos.stock(), datos.stockCritico(), imagen(datos.imagen()),
                categorias.buscar(datos.categoriaId()));
        return ProductoDto.desde(productos.save(p));
    }

    @Transactional
    public ProductoDto actualizar(Long id, ProductoRequest datos) {
        Producto p = buscar(id);
        String codigo = datos.codigo().trim().toUpperCase(Locale.ROOT);
        validarOferta(datos);
        if (productos.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new ReglaNegocioException("Ya existe un producto con el código " + codigo + ".");
        }
        p.setCodigo(codigo);
        p.setNombre(datos.nombre().trim());
        p.setDescripcion(limpiar(datos.descripcion()));
        p.setPrecio(datos.precio());
        p.setPrecioOferta(datos.precioOferta());
        p.setStock(datos.stock());
        p.setStockCritico(datos.stockCritico());
        p.setImagen(imagen(datos.imagen()));
        p.setCategoria(categorias.buscar(datos.categoriaId()));
        return ProductoDto.desde(p);
    }

    @Transactional
    public ProductoDto reponerStock(Long id, int cantidad) {
        Producto p = buscar(id);
        p.setStock(p.getStock() + cantidad);
        return ProductoDto.desde(p);
    }

    /** Las boletas conservan el nombre y el precio cobrado, así que solo se desvinculan del producto. */
    @Transactional
    public void eliminar(Long id) {
        Producto p = buscar(id);
        detalles.desvincularProducto(id);
        productos.delete(p);
    }

    private Producto buscar(Long id) {
        return productos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
    }

    private static void validarOferta(ProductoRequest datos) {
        if (datos.precioOferta() != null && datos.precioOferta() >= datos.precio()) {
            throw new DatosInvalidosException("precioOferta", "La oferta debe ser menor al precio normal.");
        }
    }

    private static String limpiar(String texto) {
        return texto == null ? null : texto.trim();
    }

    private static String imagen(String ruta) {
        return ruta == null || ruta.isBlank() ? IMAGEN_POR_DEFECTO : ruta.trim();
    }
}
