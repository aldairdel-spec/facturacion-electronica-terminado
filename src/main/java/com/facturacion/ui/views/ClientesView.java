package com.facturacion.ui.views;

import com.facturacion.dao.ClienteDAO;
import com.facturacion.model.Cliente;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;

@Route(value = "clientes", layout = com.facturacion.ui.MainLayout.class)
@PageTitle("Clientes")
@PermitAll
public class ClientesView extends VerticalLayout {
    private final ClienteDAO dao = new ClienteDAO();
    private Grid<Cliente> grid;

    ClientesView() {
        setSizeFull();
        setPadding(true);
        getStyle().set("background", "#f8fafc");

        Button btnNuevo = new Button("+ Nuevo Cliente", VaadinIcon.PLUS.create());
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevo.addClickListener(e -> abrirFormulario(null));

        H2 titulo = new H2("👥 Gestión de Clientes");
        titulo.getStyle().set("color", "#1e3a8a").set("margin", "0");

        HorizontalLayout encabezado = new HorizontalLayout(titulo, btnNuevo);
        encabezado.setWidthFull();
        encabezado.setJustifyContentMode(JustifyContentMode.BETWEEN);
        encabezado.setDefaultVerticalComponentAlignment(VerticalLayout.Alignment.CENTER);
        encabezado.getStyle().set("margin-bottom", "16px");

        grid = new Grid<>(Cliente.class, false);
        grid.setSizeFull();
        grid.addColumn(Cliente::getNombre).setHeader("Nombre").setFlexGrow(1);
        grid.addColumn(Cliente::getCedula).setHeader("Cédula").setWidth("130px").setFlexGrow(0);
        grid.addColumn(Cliente::getTelefono).setHeader("Teléfono").setWidth("120px").setFlexGrow(0);
        grid.addColumn(Cliente::getCorreo).setHeader("Correo").setFlexGrow(1);
        grid.addComponentColumn(c -> {
            Button edit = new Button(VaadinIcon.EDIT.create());
            edit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            edit.addClickListener(ev -> abrirFormulario(c));
            Button del = new Button(VaadinIcon.TRASH.create());
            del.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            del.addClickListener(ev -> { dao.eliminar(c.getId()); cargar(); });
            return new HorizontalLayout(edit, del);
        }).setHeader("Acciones").setWidth("110px").setFlexGrow(0);

        add(encabezado, grid);
        cargar();
    }

    private void cargar() { grid.setItems(dao.listarTodos()); }

    private void abrirFormulario(Cliente c) {
        boolean esNuevo = (c == null);
        if (esNuevo) c = new Cliente();
        Cliente cli = c;

        Dialog dlg = new Dialog();
        dlg.setHeaderTitle(esNuevo ? "Nuevo Cliente" : "Editar Cliente");

        TextField txNom = new TextField("Nombre"); txNom.setWidthFull(); txNom.setValue(cli.getNombre() == null ? "" : cli.getNombre());
        TextField txCed = new TextField("Cédula"); txCed.setWidthFull(); txCed.setValue(cli.getCedula() == null ? "" : cli.getCedula());
        TextField txTel = new TextField("Teléfono"); txTel.setWidthFull(); txTel.setValue(cli.getTelefono() == null ? "" : cli.getTelefono());
        TextField txCorr = new TextField("Correo"); txCorr.setWidthFull(); txCorr.setValue(cli.getCorreo() == null ? "" : cli.getCorreo());
        TextField txDir = new TextField("Dirección"); txDir.setWidthFull(); txDir.setValue(cli.getDireccion() == null ? "" : cli.getDireccion());

        dlg.add(new VerticalLayout(txNom, txCed, txTel, txCorr, txDir) {{ setPadding(false); }});
        Button guardar = new Button("Guardar", e -> {
            cli.setNombre(txNom.getValue()); cli.setCedula(txCed.getValue());
            cli.setTelefono(txTel.getValue()); cli.setCorreo(txCorr.getValue()); cli.setDireccion(txDir.getValue());
            if (esNuevo) dao.guardar(cli); else dao.actualizar(cli);
            cargar(); dlg.close();
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dlg.getFooter().add(new Button("Cancelar", ev -> dlg.close()), guardar);
        dlg.open();
    }
}
