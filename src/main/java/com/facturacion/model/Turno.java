package com.facturacion.model;

// =====================================================================
// MODELO: Turno
// =====================================================================

import java.time.LocalDate;
import java.time.LocalTime;

public class Turno {
    private int id;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private LocalDate fecha;

    public Turno() {}

    public Turno(int id, LocalTime horaInicio, LocalTime horaFin, LocalDate fecha) {
        this.id = id;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.fecha = fecha;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    @Override
    public String toString() {
        return "Turno " + fecha + " " + horaInicio + " - " + (horaFin != null ? horaFin : "en curso");
    }
}
