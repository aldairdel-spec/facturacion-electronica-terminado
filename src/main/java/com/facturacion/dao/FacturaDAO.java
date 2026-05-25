package com.facturacion.dao;

import com.facturacion.config.DatabaseConnection;
import com.facturacion.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos para la entidad Factura. Gestiona transacciones ACID
 * para garantizar consistencia entre factura, detalles e inventario.
 */
public class FacturaDAO implements GenericDAO<Factura, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(FacturaDAO.class);
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ClienteDAO  clienteDAO  = new ClienteDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    @Override
    public int guardar(Factura f) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);   // Inicio de transacción

            // 1. Insertar cabecera
            int idFactura = insertarCabecera(conn, f);
            if (idFactura < 0) throw new SQLException("No se pudo insertar la factura.");
            f.setId(idFactura);

            // 2. Insertar detalles y descontar stock
            for (DetalleFactura det : f.getDetalles()) {
                det.setIdFactura(idFactura);
                insertarDetalle(conn, det);
                // Descuento de stock en BD
                actualizarStock(conn, det.getProducto().getId(),
                        det.getProducto().getStock() - det.getCantidad());
            }

            conn.commit();
            logger.info("Factura #{} guardada con ID={}", f.getNumeroFactura(), idFactura);
            return idFactura;

        } catch (Exception ex) {
            logger.error("Error al guardar factura, haciendo rollback: {}", ex.getMessage(), ex);
            try { if (conn != null) conn.rollback(); } catch (SQLException e) { /* ignore */ }
            return -1;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    private int insertarCabecera(Connection conn, Factura f) throws SQLException {
        String sql = "INSERT INTO factura (numero_factura, fecha, total, estado, id_cliente, id_empleado) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_factura";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getNumeroFactura());
            ps.setTimestamp(2, Timestamp.valueOf(f.getFecha()));
            ps.setBigDecimal(3, f.getTotal());
            ps.setString(4, f.getEstado().name());
            ps.setInt(5, f.getCliente().getId());
            ps.setInt(6, f.getEmpleado().getId());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    private void insertarDetalle(Connection conn, DetalleFactura det) throws SQLException {
        String sql = "INSERT INTO detalle_factura (id_factura, id_producto, cantidad, precio_unitario, iva, subtotal) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, det.getIdFactura());
            ps.setInt(2, det.getProducto().getId());
            ps.setInt(3, det.getCantidad());
            ps.setBigDecimal(4, det.getPrecioUnitario());
            ps.setBigDecimal(5, det.getIva());
            ps.setBigDecimal(6, det.getSubtotal());
            ps.executeUpdate();
        }
    }

    private void actualizarStock(Connection conn, int idProducto, int nuevoStock) throws SQLException {
        String sql = "UPDATE producto SET stock=? WHERE id_producto=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean actualizar(Factura f) {
        String sql = "UPDATE factura SET estado=? WHERE id_factura=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getEstado().name());
            ps.setInt(2, f.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.error("Error al actualizar factura: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        String sql = "UPDATE factura SET estado='ANULADA' WHERE id_factura=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) logger.info("Factura ID={} anulada.", id);
            return ok;
        } catch (SQLException ex) {
            logger.error("Error al anular factura: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public Optional<Factura> buscarPorId(Integer id) {
        String sql = "SELECT * FROM factura WHERE id_factura=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Factura f = mapCabecera(rs);
                f.setDetalles(cargarDetalles(f.getId()));
                return Optional.of(f);
            }
        } catch (SQLException ex) {
            logger.error("Error al buscar factura por ID: {}", ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    @Override
    public List<Factura> listarTodos() {
        List<Factura> lista = new ArrayList<>();
        String sql = "SELECT * FROM factura ORDER BY fecha DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Factura f = mapCabecera(rs);
                f.setDetalles(cargarDetalles(f.getId()));
                lista.add(f);
            }
        } catch (SQLException ex) {
            logger.error("Error al listar facturas: {}", ex.getMessage(), ex);
        }
        return lista;
    }

    public List<Factura> listarPorRangoFechas(LocalDate desde, LocalDate hasta) {
        List<Factura> lista = new ArrayList<>();
        String sql = "SELECT * FROM factura WHERE DATE(fecha) BETWEEN ? AND ? ORDER BY fecha DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Factura f = mapCabecera(rs);
                f.setDetalles(cargarDetalles(f.getId()));
                lista.add(f);
            }
        } catch (SQLException ex) {
            logger.error("Error al listar facturas por fechas: {}", ex.getMessage(), ex);
        }
        return lista;
    }

    private List<DetalleFactura> cargarDetalles(int idFactura) {
        List<DetalleFactura> detalles = new ArrayList<>();
        String sql = "SELECT df.*, p.nombre AS prod_nombre, p.codigo, p.tipo " +
                     "FROM detalle_factura df " +
                     "JOIN producto p ON df.id_producto = p.id_producto " +
                     "WHERE df.id_factura=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFactura);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DetalleFactura det = new DetalleFactura();
                det.setId(rs.getInt("id_detalle"));
                det.setIdFactura(idFactura);
                det.setCantidad(rs.getInt("cantidad"));
                det.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                det.setIva(rs.getBigDecimal("iva"));
                det.setSubtotal(rs.getBigDecimal("subtotal"));
                Producto p = new Producto();
                p.setId(rs.getInt("id_producto"));
                p.setNombre(rs.getString("prod_nombre"));
                p.setCodigo(rs.getString("codigo"));
                p.setTipo(Producto.TipoIVA.valueOf(rs.getString("tipo")));
                p.setPrecio(rs.getBigDecimal("precio_unitario"));
                det.setProducto(p);
                detalles.add(det);
            }
        } catch (SQLException ex) {
            logger.error("Error al cargar detalles de factura ID={}: {}", idFactura, ex.getMessage(), ex);
        }
        return detalles;
    }

    private Factura mapCabecera(ResultSet rs) throws SQLException {
        Factura f = new Factura();
        f.setId(rs.getInt("id_factura"));
        f.setNumeroFactura(rs.getString("numero_factura"));
        Timestamp ts = rs.getTimestamp("fecha");
        if (ts != null) f.setFecha(ts.toLocalDateTime());
        f.setTotal(rs.getBigDecimal("total"));
        f.setEstado(Factura.Estado.valueOf(rs.getString("estado")));
        // Cargar cliente y empleado por ID (lazy simple)
        int idCliente = rs.getInt("id_cliente");
        clienteDAO.buscarPorId(idCliente).ifPresent(f::setCliente);
        int idEmpleado = rs.getInt("id_empleado");
        empleadoDAO.buscarPorId(idEmpleado).ifPresent(f::setEmpleado);
        return f;
    }
}
