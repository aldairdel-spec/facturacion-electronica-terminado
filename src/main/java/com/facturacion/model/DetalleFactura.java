package com.facturacion.model;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class DetalleFactura {

    private int id;
    private int idFactura;
    private Producto producto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal iva;         // porcentaje, ej. 19.00
    private BigDecimal subtotal;    // precio × cantidad × (1 + iva%)

    public DetalleFactura() {}

    public DetalleFactura(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
        this.iva = producto.getPorcentajeIVA().multiply(new BigDecimal("100"));
        calcularSubtotal();
    }

    /** Calcula el subtotal de la línea incluyendo IVA */
    public void calcularSubtotal() {
        BigDecimal base = precioUnitario.multiply(new BigDecimal(cantidad));
        BigDecimal factorIVA = BigDecimal.ONE.add(iva.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        this.subtotal = base.multiply(factorIVA).setScale(2, RoundingMode.HALF_UP);
    }

    // ---- Getters & Setters ----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdFactura() { return idFactura; }
    public void setIdFactura(int idFactura) { this.idFactura = idFactura; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }

    public BigDecimal getIva() { return iva; }
    public void setIva(BigDecimal iva) {
        this.iva = iva;
        calcularSubtotal();
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
