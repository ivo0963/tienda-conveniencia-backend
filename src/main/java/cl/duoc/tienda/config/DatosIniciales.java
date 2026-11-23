package cl.duoc.tienda.config;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.tienda.modelo.Boleta;
import cl.duoc.tienda.modelo.Categoria;
import cl.duoc.tienda.modelo.DetalleBoleta;
import cl.duoc.tienda.modelo.EstadoBoleta;
import cl.duoc.tienda.modelo.MetodoPago;
import cl.duoc.tienda.modelo.Producto;
import cl.duoc.tienda.modelo.Rol;
import cl.duoc.tienda.modelo.TipoEntrega;
import cl.duoc.tienda.modelo.Usuario;
import cl.duoc.tienda.repositorio.BoletaRepositorio;
import cl.duoc.tienda.repositorio.CategoriaRepositorio;
import cl.duoc.tienda.repositorio.ProductoRepositorio;
import cl.duoc.tienda.repositorio.UsuarioRepositorio;
import cl.duoc.tienda.servicio.BoletaServicio;

/** Carga datos de ejemplo la primera vez que se inicia la aplicación (cuando no hay categorías). */
@Component
@ConditionalOnProperty(name = "app.datos-iniciales", havingValue = "true")
public class DatosIniciales implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final CategoriaRepositorio categorias;
    private final ProductoRepositorio productos;
    private final UsuarioRepositorio usuarios;
    private final BoletaRepositorio boletas;
    private final PasswordEncoder passwordEncoder;

    public DatosIniciales(CategoriaRepositorio categorias, ProductoRepositorio productos, UsuarioRepositorio usuarios,
                          BoletaRepositorio boletas, PasswordEncoder passwordEncoder) {
        this.categorias = categorias;
        this.productos = productos;
        this.usuarios = usuarios;
        this.boletas = boletas;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categorias.count() > 0) {
            return;
        }
        log.info("Base de datos vacía: cargando datos de ejemplo");

        Categoria bebidas = categorias.save(new Categoria("Bebidas y Licores", "Bebidas, jugos, aguas y cervezas para toda ocasión.", "/img/coca cola.jpg"));
        Categoria snacks = categorias.save(new Categoria("Snacks y Golosinas", "Papas fritas, galletas y dulces para picotear.", "/img/papas fritas.jpg"));
        Categoria abarrotes = categorias.save(new Categoria("Abarrotes", "Pan, arroz, fideos y todo lo básico para tu despensa.", "/img/pan de molde.webp"));
        Categoria lacteos = categorias.save(new Categoria("Lácteos y Cecinas", "Leche, yogurt, quesos y cecinas siempre frescos.", "/img/leche.jpg"));

        Producto cola = productos.save(new Producto("BEB-001", "Bebida Cola 2.5 L", "Bebida gaseosa sabor cola en formato familiar retornable.", 2500, null, 50, 5, "/img/coca cola.jpg", bebidas));
        Producto papas = productos.save(new Producto("SNK-001", "Papas Fritas 250 g", "Papas fritas corte americano, crujientes y con sal de mar.", 1890, 1490, 20, 5, "/img/papas fritas.jpg", snacks));
        Producto pan = productos.save(new Producto("ABA-001", "Pan de Molde Familiar", "Pan de molde blanco de 750 g, ideal para el desayuno.", 2100, null, 15, 4, "/img/pan de molde.webp", abarrotes));
        Producto leche = productos.save(new Producto("LAC-001", "Leche Entera 1 L", "Leche entera UHT larga vida, fuente de calcio.", 1050, 890, 40, 10, "/img/leche.jpg", lacteos));
        productos.save(new Producto("SNK-002", "Galletas Chocolate 120 g", "Galletas con chips de chocolate, perfectas para la once.", 990, 790, 4, 5, "/img/Galletas de Chocolate.jpg", snacks));
        Producto jugo = productos.save(new Producto("BEB-002", "Jugo Naranja 1 L", "Jugo de naranja néctar sin colorantes artificiales.", 1450, null, 25, 5, "/img/jugo naranja.jpg", bebidas));
        Producto cervezas = productos.save(new Producto("BEB-003", "Pack 6 Cervezas", "Pack de 6 latas de cerveza lager de 350 cc. Venta solo a mayores de 18 años.", 5990, 4990, 3, 5, "/img/pack 6 cervesas.jpg", bebidas));
        productos.save(new Producto("ABA-002", "Arroz Grado 1 1 kg", "Arroz grano largo grado 1, rinde y no se pega.", 1290, null, 30, 8, "/img/sin-imagen.svg", abarrotes));
        productos.save(new Producto("LAC-002", "Yogurt Frutilla 4 un.", "Pack de 4 yogures batidos sabor frutilla.", 1590, null, 12, 6, "/img/sin-imagen.svg", lacteos));
        productos.save(new Producto("ABA-003", "Fideos Spaghetti 400 g", "Fideos de sémola de trigo, cocción en 8 minutos.", 890, null, 0, 10, "/img/sin-imagen.svg", abarrotes));

        usuarios.save(usuario("111111111", "Admin", "Duoc", "admin@duoc.cl", "admin123", Rol.ADMIN, "Metropolitana", "Santiago", "Av. España 8", ""));
        usuarios.save(usuario("198765430", "María", "Vendedora", "vendedor@profesor.duoc.cl", "vende123", Rol.VENDEDOR, "Valparaíso", "Viña del Mar", "Calle Valparaíso 555", ""));
        Usuario juan = usuarios.save(usuario("123456785", "Juan", "Pérez", "juan.perez@gmail.com", "juan1234", Rol.CLIENTE, "Metropolitana", "Providencia", "Av. Providencia 1234", "Depto 603"));

        guardarBoleta(juan, "Juan", "Pérez", "juan.perez@gmail.com", TipoEntrega.DOMICILIO, MetodoPago.DEBITO,
                LocalDateTime.of(2026, 10, 20, 11, 32), EstadoBoleta.ENTREGADA, null,
                List.of(new DetalleBoleta(cola, 2), new DetalleBoleta(papas, 3)),
                "Av. Providencia 1234", "Depto 603", "Metropolitana", "Providencia", "Dejar con el conserje.");
        guardarBoleta(null, "Pedro", "González", "pedro.gonzalez@gmail.com", TipoEntrega.RETIRO, MetodoPago.CREDITO,
                LocalDateTime.of(2026, 10, 25, 16, 5), EstadoBoleta.PAGADA, null,
                List.of(new DetalleBoleta(cervezas, 2), new DetalleBoleta(leche, 6), new DetalleBoleta(pan, 1)),
                null, null, null, null, null);
        guardarBoleta(juan, "Juan", "Pérez", "juan.perez@gmail.com", TipoEntrega.DOMICILIO, MetodoPago.CREDITO,
                LocalDateTime.of(2026, 10, 27, 7, 15), EstadoBoleta.RECHAZADA, "El medio de pago fue rechazado por el banco emisor.",
                List.of(new DetalleBoleta(jugo, 2)),
                "Av. Providencia 1234", "Depto 603", "Metropolitana", "Providencia", null);
    }

    private Usuario usuario(String run, String nombre, String apellidos, String correo, String password, Rol rol,
                            String region, String comuna, String direccion, String departamento) {
        Usuario u = new Usuario(run, nombre, apellidos, correo, passwordEncoder.encode(password), rol);
        u.setRegion(region);
        u.setComuna(comuna);
        u.setDireccion(direccion);
        u.setDepartamento(departamento);
        return u;
    }

    private void guardarBoleta(Usuario usuario, String nombre, String apellidos, String correo, TipoEntrega entrega,
                               MetodoPago pago, LocalDateTime fecha, EstadoBoleta estado, String motivo,
                               List<DetalleBoleta> detalles, String calle, String depto, String region, String comuna,
                               String indicaciones) {
        Boleta b = new Boleta();
        b.setUsuario(usuario);
        b.setClienteNombre(nombre);
        b.setClienteApellidos(apellidos);
        b.setClienteCorreo(correo);
        b.setEntrega(entrega);
        b.setMetodoPago(pago);
        b.setFecha(fecha);
        b.setEstado(estado);
        b.setMotivoRechazo(motivo);
        b.setCalle(calle);
        b.setDepartamento(depto);
        b.setRegion(region);
        b.setComuna(comuna);
        b.setIndicaciones(indicaciones);
        detalles.forEach(b::agregarDetalle);
        int subtotal = detalles.stream().mapToInt(DetalleBoleta::getSubtotal).sum();
        b.setSubtotal(subtotal);
        b.setEnvio(BoletaServicio.calcularEnvio(subtotal, entrega));
        b.setTotal(subtotal + b.getEnvio());
        Boleta guardada = boletas.save(b);
        guardada.setCodigo(BoletaServicio.generarCodigo(guardada.getId()));
    }
}
