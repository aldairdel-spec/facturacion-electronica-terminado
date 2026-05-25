package com.facturacion.ui.views;

import com.facturacion.service.FacturacionService;
import com.facturacion.model.Factura;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "reportes", layout = com.facturacion.ui.MainLayout.class)
@PageTitle("Reportes Financieros")

public class ReportesView extends VerticalLayout { // ✅ AQUÍ ESTÁ EL FIX

    private final FacturacionService service = new FacturacionService();

    public ReportesView() { // 👉 también lo dejo público por buena práctica
        setSizeFull();
        setPadding(true);
        getStyle().set("background", "#f8fafc");

        H2 titulo = new H2("📊 Reportes Financieros");
        titulo.getStyle().set("color", "#1e3a8a").set("margin", "0 0 24px 0");

        LocalDate desde = LocalDate.now().withDayOfMonth(1);
        LocalDate hasta = LocalDate.now();

        Grid<Factura> grid = new Grid<>(Factura.class, false);
        grid.setSizeFull();

        grid.addColumn(Factura::getNumeroFactura).setHeader("# Factura").setWidth("180px");
        grid.addColumn(f -> f.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Fecha").setWidth("110px");
        grid.addColumn(f -> f.getCliente() != null ? f.getCliente().getNombre() : "-")
                .setHeader("Cliente").setFlexGrow(1);
        grid.addColumn(f -> "$" + String.format("%,.2f", f.getSubtotalSinIVA()))
                .setHeader("Base").setWidth("120px");
        grid.addColumn(f -> "$" + String.format("%,.2f", f.getTotalIVA()))
                .setHeader("IVA").setWidth("100px");
        grid.addColumn(f -> "$" + String.format("%,.2f", f.getTotal()))
                .setHeader("Total").setWidth("120px");
        grid.addColumn(f -> f.getEstado().name())
                .setHeader("Estado").setWidth("100px");

        List<Factura> facturas = service.obtenerFacturasPorFechas(desde, hasta);
        grid.setItems(facturas);

        Button btnCalc = new Button("Calcular",
                com.vaadin.flow.component.icon.VaadinIcon.CALC_BOOK.create());

        btnCalc.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        btnCalc.addClickListener(e -> {
            List<Factura> f = service.obtenerFacturasPorFechas(desde, hasta);
            grid.setItems(f);
        });

        HorizontalLayout filtros = new HorizontalLayout(btnCalc);
        filtros.setDefaultVerticalComponentAlignment(
                com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END
        );

        add(titulo, filtros, grid);
    }
}