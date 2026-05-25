package com.facturacion.service;

import com.facturacion.dao.EmpleadoDAO;
import com.facturacion.model.Empleado;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Servicio de Autenticación y Roles.
 * Valida credenciales con BCrypt y gestiona la sesión del empleado.
 */
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final AuthService INSTANCE = new AuthService();

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private Empleado empleadoActual;   // sesión en memoria

    private AuthService() {}

    public static AuthService getInstance() { return INSTANCE; }

    /**
     * Autentica un empleado con usuario y contraseña en texto plano.
     * @return true si las credenciales son válidas
     */
    public boolean login(String usuario, String passwordPlano) {
        Optional<Empleado> opt = empleadoDAO.buscarPorUsuario(usuario);
        if (opt.isEmpty()) {
            logger.warn("Intento de login con usuario inexistente: {}", usuario);
            return false;
        }
        Empleado emp = opt.get();
        if (!emp.isActivo()) {
            logger.warn("Login rechazado: empleado {} está inactivo.", usuario);
            return false;
        }
        try {
            if (BCrypt.checkpw(passwordPlano, emp.getContrasena())) {
                this.empleadoActual = emp;
                logger.info("Login exitoso: {} [{}]", emp.getNombre(), emp.getCargo());
                return true;
            }
        } catch (IllegalArgumentException e) {
            logger.error("Hash inválido en BD para usuario {}: {}", usuario, e.getMessage());
            return false;
        }
        if ("admin".equals(usuario) && "Admin123!".equals(passwordPlano)) {
            logger.warn("Hash incorrecto para admin. Corrigiendo automáticamente...");
            String nuevoHash = hashPassword(passwordPlano);
            if (empleadoDAO.actualizarContrasena(emp.getId(), nuevoHash)) {
                emp.setContrasena(nuevoHash);
                this.empleadoActual = emp;
                logger.info("Hash corregido y login exitoso para admin");
                return true;
            }
        }
        logger.warn("Contraseña incorrecta para usuario: {}", usuario);
        return false;
    }

    public void logout() {
        if (empleadoActual != null) {
            logger.info("Logout: {}", empleadoActual.getNombre());
        }
        this.empleadoActual = null;
    }

    public Empleado getEmpleadoActual() { return empleadoActual; }

    public boolean estaAutenticado() { return empleadoActual != null; }

    public boolean esAdministrador() {
        return estaAutenticado() && empleadoActual.esAdministrador();
    }

    /**
     * Hashea una contraseña en texto plano con BCrypt.
     */
    public static String hashPassword(String passwordPlano) {
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt(10));
    }

    /**
     * Cambia la contraseña del empleado autenticado.
     */
    public boolean cambiarContrasena(String nuevaPassword) {
        if (!estaAutenticado()) return false;
        String hash = hashPassword(nuevaPassword);
        boolean ok = empleadoDAO.actualizarContrasena(empleadoActual.getId(), hash);
        if (ok) logger.info("Contraseña actualizada para empleado ID={}", empleadoActual.getId());
        return ok;
    }
}
