package com.facturacion.dao;

import com.facturacion.config.DatabaseConnection;
import com.facturacion.model.Empleado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos para la entidad Empleado usando JDBC puro.
 */
public class EmpleadoDAO implements GenericDAO<Empleado, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(EmpleadoDAO.class);

    @Override
    public int guardar(Empleado e) {
        String sql = "INSERT INTO empleado (nombre, cargo, usuario, contrasena, activo) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id_empleado";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getCargo().name());
            ps.setString(3, e.getUsuario());
            ps.setString(4, e.getContrasena());
            ps.setBoolean(5, e.isActivo());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                e.setId(id);
                logger.info("Empleado creado con ID={}", id);
                return id;
            }
        } catch (SQLException ex) {
            logger.error("Error al guardar empleado: {}", ex.getMessage(), ex);
        }
        return -1;
    }

    @Override
    public boolean actualizar(Empleado e) {
        String sql = "UPDATE empleado SET nombre=?, cargo=?, usuario=?, activo=? WHERE id_empleado=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getCargo().name());
            ps.setString(3, e.getUsuario());
            ps.setBoolean(4, e.isActivo());
            ps.setInt(5, e.getId());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) logger.info("Empleado ID={} actualizado.", e.getId());
            return ok;
        } catch (SQLException ex) {
            logger.error("Error al actualizar empleado: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        // Eliminación lógica
        String sql = "UPDATE empleado SET activo=FALSE WHERE id_empleado=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) logger.info("Empleado ID={} desactivado.", id);
            return ok;
        } catch (SQLException ex) {
            logger.error("Error al eliminar empleado: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public Optional<Empleado> buscarPorId(Integer id) {
        String sql = "SELECT * FROM empleado WHERE id_empleado=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException ex) {
            logger.error("Error al buscar empleado por ID: {}", ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    public Optional<Empleado> buscarPorUsuario(String usuario) {
        String sql = "SELECT * FROM empleado WHERE usuario=? AND activo=TRUE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException ex) {
            logger.error("Error al buscar empleado por usuario: {}", ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    @Override
    public List<Empleado> listarTodos() {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT * FROM empleado ORDER BY nombre";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapRow(rs));
        } catch (SQLException ex) {
            logger.error("Error al listar empleados: {}", ex.getMessage(), ex);
        }
        return lista;
    }

    public boolean actualizarContrasena(int id, String hashNuevo) {
        String sql = "UPDATE empleado SET contrasena=? WHERE id_empleado=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashNuevo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.error("Error al actualizar contraseña: {}", ex.getMessage(), ex);
            return false;
        }
    }

    private Empleado mapRow(ResultSet rs) throws SQLException {
        Empleado e = new Empleado();
        e.setId(rs.getInt("id_empleado"));
        e.setNombre(rs.getString("nombre"));
        e.setCargo(Empleado.Cargo.valueOf(rs.getString("cargo")));
        e.setUsuario(rs.getString("usuario"));
        e.setContrasena(rs.getString("contrasena"));
        e.setActivo(rs.getBoolean("activo"));
        return e;
    }
}
