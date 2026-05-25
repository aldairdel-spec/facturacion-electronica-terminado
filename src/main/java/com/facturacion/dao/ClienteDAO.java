package com.facturacion.dao;

import com.facturacion.config.DatabaseConnection;
import com.facturacion.model.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos para la entidad Cliente usando JDBC puro.
 */
public class ClienteDAO implements GenericDAO<Cliente, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ClienteDAO.class);

    @Override
    public int guardar(Cliente c) {
        String sql = "INSERT INTO cliente (nombre, cedula, telefono, correo, direccion, fecha_registro) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_cliente";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getCedula());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getCorreo());
            ps.setString(5, c.getDireccion());
            ps.setDate(6, Date.valueOf(c.getFechaRegistro()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                c.setId(id);
                logger.info("Cliente creado con ID={}", id);
                return id;
            }
        } catch (SQLException ex) {
            logger.error("Error al guardar cliente: {}", ex.getMessage(), ex);
        }
        return -1;
    }

    @Override
    public boolean actualizar(Cliente c) {
        String sql = "UPDATE cliente SET nombre=?, cedula=?, telefono=?, correo=?, direccion=? WHERE id_cliente=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getCedula());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getCorreo());
            ps.setString(5, c.getDireccion());
            ps.setInt(6, c.getId());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) logger.info("Cliente ID={} actualizado.", c.getId());
            return ok;
        } catch (SQLException ex) {
            logger.error("Error al actualizar cliente: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        String sql = "DELETE FROM cliente WHERE id_cliente=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) logger.info("Cliente ID={} eliminado.", id);
            return ok;
        } catch (SQLException ex) {
            logger.error("Error al eliminar cliente: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(Integer id) {
        String sql = "SELECT * FROM cliente WHERE id_cliente=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException ex) {
            logger.error("Error al buscar cliente por ID: {}", ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    public Optional<Cliente> buscarPorCedula(String cedula) {
        String sql = "SELECT * FROM cliente WHERE cedula=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cedula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException ex) {
            logger.error("Error al buscar cliente por cédula: {}", ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    public List<Cliente> buscarPorNombre(String nombre) {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente WHERE LOWER(nombre) LIKE ? ORDER BY nombre";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nombre.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapRow(rs));
        } catch (SQLException ex) {
            logger.error("Error al buscar clientes por nombre: {}", ex.getMessage(), ex);
        }
        return lista;
    }

    @Override
    public List<Cliente> listarTodos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente ORDER BY nombre";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapRow(rs));
        } catch (SQLException ex) {
            logger.error("Error al listar clientes: {}", ex.getMessage(), ex);
        }
        return lista;
    }

    private Cliente mapRow(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id_cliente"));
        c.setNombre(rs.getString("nombre"));
        c.setCedula(rs.getString("cedula"));
        c.setTelefono(rs.getString("telefono"));
        c.setCorreo(rs.getString("correo"));
        c.setDireccion(rs.getString("direccion"));
        Date d = rs.getDate("fecha_registro");
        if (d != null) c.setFechaRegistro(d.toLocalDate());
        return c;
    }
}
