package com.facturacion.ui.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


@Route(value = "historial", layout = com.facturacion.ui.MainLayout.class)
@PageTitle("Historial")

public class HistorialView extends VerticalLayout {

    public HistorialView() {
        setSizeFull();
        setPadding(true);

        H2 titulo = new H2("📄 Historial de Facturas");

        add(titulo);
    }
}

