package com.facturacion.model;

import java.math.BigDecimal;


public class Producto {

    public enum TipoIVA { GRAVADO, EXCLUIDO, EXENTO }

    private int id;
    private String nombre;
    private String codigo;
    private BigDecimal precio;
    private int stock;
    private TipoIVA tipo;
    private boolean activo;

    public Producto() {
        this.activo = true;
        this.tipo = TipoIVA.GRAVADO;
        this.precio = BigDecimal.ZERO;
        this.stock = 0;
    }

    public Producto(int id, String nombre, String codigo, BigDecimal precio,
                    int stock, TipoIVA tipo, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.precio = precio;
        this.stock = stock;
        this.tipo = tipo;
        this.activo = activo;
    }

    /** Retorna el porcentaje de IVA aplicable */
    public BigDecimal getPorcentajeIVA() {
        return TipoIVA.GRAVADO.equals(this.tipo)
                ? new BigDecimal("0.19")
                : BigDecimal.ZERO;
    }

    /** Reduce el stock en la cantidad indicada */
    public void reducirStock(int cantidad) {
        if (cantidad > this.stock) {
            throw new IllegalArgumentException("Stock insuficiente para el producto: " + nombre);
        }
        this.stock -= cantidad;
    }

    /** Aumenta el stock */
    public void aumentarStock(int cantidad) {
        this.stock += cantidad;
    }

    // ---- Getters & Setters ----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public TipoIVA getTipo() { return tipo; }
    public void setTipo(TipoIVA tipo) { this.tipo = tipo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "[" + codigo + "] " + nombre + " - $" + precio;
    }
}
