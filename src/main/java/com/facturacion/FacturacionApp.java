package com.facturacion;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableVaadin("com.facturacion.ui")
public class FacturacionApp {

    private static final Logger logger = LoggerFactory.getLogger(FacturacionApp.class);

    public static void main(String[] args) {
        logger.info("Iniciando Sistema de Facturación Electrónica...");
        SpringApplication.run(FacturacionApp.class, args);
        logger.info("Sistema iniciado correctamente en http://localhost:8080");
    }
}
