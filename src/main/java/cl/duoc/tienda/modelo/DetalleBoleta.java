package cl.duoc.tienda.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Línea de una boleta. Guarda el nombre y el precio cobrado en el momento de la compra,
 * así la boleta no cambia si después se edita o elimina el producto.
 */
@Entity
@Table(name = "detalle_boleta")
public class DetalleBoleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "boleta_id", nullable = false)
    private Boleta boleta;

    /** Puede quedar en null si el producto se elimina del catálogo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(name = "nombre_producto", nullable = false, length = 100)
    private String nombreProducto;

    @Column(length = 255)
    private String imagen;

    @Column(name = "precio_unitario", nullable = false)
    private Integer precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Integer subtotal;

    protected DetalleBoleta() {
    }

    public DetalleBoleta(Producto producto, int cantidad) {
        this.producto = producto;
        this.nombreProducto = producto.getNombre();
        this.imagen = producto.getImagen();
        this.precioUnitario = producto.precioFinal();
        this.cantidad = cantidad;
        this.subtotal = precioUnitario * cantidad;
    }

    public Long getId() { return id; }
    public Boleta getBoleta() { return boleta; }
    void setBoleta(Boleta boleta) { this.boleta = boleta; }
    public Producto getProducto() { return producto; }
    public String getNombreProducto() { return nombreProducto; }
    public String getImagen() { return imagen; }
    public Integer getPrecioUnitario() { return precioUnitario; }
    public Integer getCantidad() { return cantidad; }
    public Integer getSubtotal() { return subtotal; }
}
