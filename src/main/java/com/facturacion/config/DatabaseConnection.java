package com.facturacion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public final class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    private static DatabaseConnection instance;

    private static String url;
    private static String user;
    private static String password;
    private static String driver;

    static {
        cargarConfiguracion();
    }

    private DatabaseConnection() {
        // Prevenir instanciación directa
    }


    private static void cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                logger.error("No se encontró el archivo application.properties.");
                return;
            }

            props.load(input);

            url      = props.getProperty("database.url");
            user     = props.getProperty("database.user");
            password = props.getProperty("database.password");
            driver   = props.getProperty("database.driver");

            // Si no viene la URL completa, la construye con host/port/name
            if (url == null || url.trim().isEmpty()) {
                String host = props.getProperty("database.host");
                String port = props.getProperty("database.port");
                String name = props.getProperty("database.name");
                url = "jdbc:postgresql://" + host + ":" + port + "/" + name;
            }

            logger.info("Configuración de base de datos cargada correctamente.");

        } catch (Exception e) {
            logger.error("Error al cargar la configuración de base de datos.", e);
        }
    }

    /**
     * Retorna la única instancia de DatabaseConnection (Singleton).
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Crea y retorna una nueva conexión a la base de datos.
     */
    public Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
            logger.info("Conexión establecida con: {}", url);
        } catch (ClassNotFoundException e) {
            logger.error("Driver JDBC no encontrado: {}", driver);
        } catch (SQLException e) {
            logger.error("Error al conectar con la base de datos: {}", e.getMessage());
        }
        return conn;
    }


    public static void cerrar(AutoCloseable... recursos) {
        for (AutoCloseable recurso : recursos) {
            if (recurso != null) {
                try {
                    recurso.close();
                } catch (Exception e) {
                    logger.error("Error al cerrar recurso JDBC.", e);
                }
            }
        }
    }
}
