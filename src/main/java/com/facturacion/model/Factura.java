package com.facturacion.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Factura {

    public enum Estado { PENDIENTE, VALIDADA, ANULADA }

    private int id;
    private String numeroFactura;
    private LocalDateTime fecha;
    private BigDecimal total;
    private Estado estado;
    private Cliente cliente;
    private Empleado empleado;
    private List<DetalleFactura> detalles;

    public Factura() {
        this.fecha = LocalDateTime.now();
        this.estado = Estado.PENDIENTE;
        this.detalles = new ArrayList<>();
        this.total = BigDecimal.ZERO;
    }

    /** Agrega un detalle y recalcula el total */
    public void agregarDetalle(DetalleFactura detalle) {
        detalles.add(detalle);
        calcularTotal();
    }

    /** Elimina un detalle y recalcula el total */
    public void eliminarDetalle(DetalleFactura detalle) {
        detalles.remove(detalle);
        calcularTotal();
    }

    /** Suma los subtotales de todos los detalles */
    public void calcularTotal() {
        this.total = detalles.stream()
                .map(DetalleFactura::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Total sin IVA */
    public BigDecimal getSubtotalSinIVA() {
        return detalles.stream()
                .map(d -> d.getPrecioUnitario().multiply(new BigDecimal(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Total IVA */
    public BigDecimal getTotalIVA() {
        return total.subtract(getSubtotalSinIVA());
    }

    // ---- Getters & Setters ----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public List<DetalleFactura> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleFactura> detalles) {
        this.detalles = detalles;
        calcularTotal();
    }

    @Override
    public String toString() {
        return "Factura #" + numeroFactura + " | " + fecha + " | Total: $" + total;
    }
}
