package com.facturacion.dao;

import com.facturacion.config.DatabaseConnection;
import com.facturacion.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos para la entidad Producto usando JDBC puro.
 */
public class ProductoDAO implements GenericDAO<Producto, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ProductoDAO.class);

    @Override
    public int guardar(Producto p) {
        String sql = "INSERT INTO producto (nombre, codigo, precio, stock, tipo, activo) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_producto";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCodigo());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getTipo().name());
            ps.setBoolean(6, p.isActivo());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                p.setId(id);
                logger.info("Producto creado con ID={}", id);
                return id;
            }
        } catch (SQLException ex) {
            logger.error("Error al guardar producto: {}", ex.getMessage(), ex);
        }
        return -1;
    }

    @Override
    public boolean actualizar(Producto p) {
        String sql = "UPDATE producto SET nombre=?, codigo=?, precio=?, stock=?, tipo=?, activo=? " +
                     "WHERE id_producto=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCodigo());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getTipo().name());
            ps.setBoolean(6, p.isActivo());
            ps.setInt(7, p.getId());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) logger.info("Producto ID={} actualizado.", p.getId());
            return ok;
        } catch (SQLException ex) {
            logger.error("Error al actualizar producto: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        String sql = "UPDATE producto SET activo=FALSE WHERE id_producto=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) logger.info("Producto ID={} desactivado.", id);
            return ok;
        } catch (SQLException ex) {
            logger.error("Error al eliminar producto: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public Optional<Producto> buscarPorId(Integer id) {
        String sql = "SELECT * FROM producto WHERE id_producto=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException ex) {
            logger.error("Error al buscar producto por ID: {}", ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    public Optional<Producto> buscarPorCodigo(String codigo) {
        String sql = "SELECT * FROM producto WHERE codigo=? AND activo=TRUE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException ex) {
            logger.error("Error al buscar producto por código: {}", ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    public List<Producto> buscarPorNombre(String nombre) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE LOWER(nombre) LIKE ? AND activo=TRUE ORDER BY nombre";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nombre.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapRow(rs));
        } catch (SQLException ex) {
            logger.error("Error al buscar productos por nombre: {}", ex.getMessage(), ex);
        }
        return lista;
    }

    @Override
    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE activo=TRUE ORDER BY nombre";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapRow(rs));
        } catch (SQLException ex) {
            logger.error("Error al listar productos: {}", ex.getMessage(), ex);
        }
        return lista;
    }

    public boolean actualizarStock(int id, int nuevoStock) {
        String sql = "UPDATE producto SET stock=? WHERE id_producto=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.error("Error al actualizar stock del producto ID={}: {}", id, ex.getMessage(), ex);
            return false;
        }
    }

    private Producto mapRow(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id_producto"));
        p.setNombre(rs.getString("nombre"));
        p.setCodigo(rs.getString("codigo"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setStock(rs.getInt("stock"));
        p.setTipo(Producto.TipoIVA.valueOf(rs.getString("tipo")));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }
}
