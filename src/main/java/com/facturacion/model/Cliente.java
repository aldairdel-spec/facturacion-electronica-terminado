package com.facturacion.model;

import java.time.LocalDate;

/**
 * Entidad Cliente del supermercado.
 */
public class Cliente {

    private int id;
    private String nombre;
    private String cedula;
    private String telefono;
    private String correo;
    private String direccion;
    private LocalDate fechaRegistro;

    public Cliente() {
        this.fechaRegistro = LocalDate.now();
    }

    public Cliente(int id, String nombre, String cedula, String telefono,
                   String correo, String direccion, LocalDate fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.cedula = cedula;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.fechaRegistro = fechaRegistro;
    }

    // ---- Getters & Setters ----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    @Override
    public String toString() {
        return nombre + " - CC: " + cedula;
    }
}
