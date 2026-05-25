package com.facturacion.service;

import com.facturacion.dao.ClienteDAO;
import com.facturacion.dao.FacturaDAO;
import com.facturacion.dao.ProductoDAO;
import com.facturacion.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio de Facturación Electrónica.
 * Orquesta la creación de facturas, validación de stock y generación de número de factura.
 */
public class FacturacionService {

    private static final Logger logger = LoggerFactory.getLogger(FacturacionService.class);

    private final FacturaDAO  facturaDAO  = new FacturaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ClienteDAO  clienteDAO  = new ClienteDAO();

    private static final AtomicInteger secuencia = new AtomicInteger(1);

    /**
     * Genera un número de factura único con formato: FE-YYYYMMDD-NNNN
     */
    public String generarNumeroFactura() {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq   = String.format("%04d", secuencia.getAndIncrement());
        return "FE-" + fecha + "-" + seq;
    }

    /**
     * Crea y persiste una factura completa.
     * Valida disponibilidad de stock antes de proceder.
     *
     * @param factura Factura con detalles completamente configurados
     * @return ID de la factura creada, o -1 si falla
     * @throws IllegalArgumentException si hay stock insuficiente
     */
    public int crearFactura(Factura factura) {
        // Validar stock para cada producto (sin descontar aún)
        for (DetalleFactura det : factura.getDetalles()) {
            Producto prod = det.getProducto();
            Optional<Producto> opt = productoDAO.buscarPorId(prod.getId());
            if (opt.isEmpty()) {
                throw new IllegalArgumentException("Producto no encontrado: " + prod.getCodigo());
            }
            Producto actual = opt.get();
            if (actual.getStock() < det.getCantidad()) {
                throw new IllegalArgumentException(
                    "Stock insuficiente para '" + actual.getNombre() +
                    "'. Disponible: " + actual.getStock() +
                    ", Solicitado: " + det.getCantidad());
            }
        }

        if (factura.getNumeroFactura() == null || factura.getNumeroFactura().isEmpty()) {
            factura.setNumeroFactura(generarNumeroFactura());
        }

        factura.calcularTotal();
        int id = facturaDAO.guardar(factura);
        if (id > 0) {
            logger.info("Factura #{} creada exitosamente. Total: ${}", factura.getNumeroFactura(), factura.getTotal());
            // Descontar stock tras guardar factura para mantener consistencia
            for (DetalleFactura det : factura.getDetalles()) {
                Producto actual = productoDAO.buscarPorId(det.getProducto().getId()).get();
                int nuevoStock = actual.getStock() - det.getCantidad();
                if (!productoDAO.actualizarStock(actual.getId(), nuevoStock)) {
                    // Intento de rollback: eliminar factura creada si falla el descuento de stock
                    try {
                        facturaDAO.eliminar(id);
                    } catch (Exception ignore) {}
                    logger.error("Error al descontar stock para producto ID={}. Rolback factura ID={}", actual.getId(), id);
                    throw new IllegalStateException("Error al actualizar stock tras generar la factura.");
                }
                det.getProducto().setStock(nuevoStock);
            }
        }
        return id;
    }

    /** Obtiene el historial de facturas filtrado por rango de fechas */
    public List<Factura> obtenerFacturasPorFechas(LocalDate desde, LocalDate hasta) {
        return facturaDAO.listarPorRangoFechas(desde, hasta);
    }

    /** Anula una factura */
    public boolean anularFactura(int idFactura) {
        Optional<Factura> opt = facturaDAO.buscarPorId(idFactura);
        if (opt.isEmpty()) {
            logger.warn("Intento de anular factura inexistente ID={}", idFactura);
            return false;
        }
        Factura f = opt.get();
        f.setEstado(Factura.Estado.ANULADA);
        boolean ok = facturaDAO.actualizar(f);
        if (ok) logger.info("Factura #{} anulada.", f.getNumeroFactura());
        return ok;
    }

    /** Lista todas las facturas */
    public List<Factura> listarFacturas() {
        return facturaDAO.listarTodos();
    }

    /** Busca o crea cliente consumidor final */
    public Optional<Cliente> buscarClientePorCedula(String cedula) {
        return clienteDAO.buscarPorCedula(cedula);
    }

    public List<Cliente> listarClientes() {
        return clienteDAO.listarTodos();
    }

    public List<Producto> listarProductos() {
        return productoDAO.listarTodos();
    }

    public Optional<Producto> buscarProductoPorCodigo(String codigo) {
        return productoDAO.buscarPorCodigo(codigo);
    }
}
