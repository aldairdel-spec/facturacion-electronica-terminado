package com.facturacion.model;


public class Empleado {

    public enum Cargo { ADMINISTRADOR, CAJERO }

    private int id;
    private String nombre;
    private Cargo cargo;
    private String usuario;
    private String contrasena;   // almacena el hash BCrypt
    private boolean activo;
    private Turno turno;

    public Empleado() {
        this.activo = true;
    }

    public Empleado(int id, String nombre, Cargo cargo, String usuario, String contrasena, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.cargo = cargo;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.activo = activo;
    }

    // ---- Getters & Setters ----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Cargo getCargo() { return cargo; }
    public void setCargo(Cargo cargo) { this.cargo = cargo; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }

    public boolean esAdministrador() {
        return Cargo.ADMINISTRADOR.equals(this.cargo);
    }

    @Override
    public String toString() {
        return nombre + " [" + cargo + "]";
    }
}
