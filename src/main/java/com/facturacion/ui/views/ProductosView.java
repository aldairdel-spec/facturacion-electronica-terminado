package com.facturacion.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;


@Route(value = "productos", layout = com.facturacion.ui.MainLayout.class)
@PageTitle("Productos")

public class ProductosView extends VerticalLayout {

    public ProductosView() {

        setSizeFull();

        H2 titulo = new H2("📦 Productos");

        Button btnNuevo = new Button("Nuevo", VaadinIcon.PLUS.create());

        HorizontalLayout header = new HorizontalLayout(titulo, btnNuevo);

        // ✅ AQUÍ ESTÁ EL FIX
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();

        Grid<String> grid = new Grid<>();
        grid.setItems("Producto 1", "Producto 2");

        add(header, grid);
    }
}
