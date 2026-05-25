package com.facturacion.ui.views;

import com.facturacion.dao.EmpleadoDAO;
import com.facturacion.model.Empleado;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.select.Select;

@Route(value = "usuarios", layout = com.facturacion.ui.MainLayout.class)
@PageTitle("Usuarios")

public class UsuariosView extends VerticalLayout {

    private final EmpleadoDAO dao = new EmpleadoDAO();
    private Grid<Empleado> grid;

    public UsuariosView() {
        setSizeFull();
        setPadding(true);
        getStyle().set("background", "#f8fafc");

        Button btnNuevo = new Button("+ Nuevo Usuario", VaadinIcon.PLUS.create());
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevo.addClickListener(e -> abrirFormulario(null));

        H2 titulo = new H2("👤 Gestión de Usuarios");
        titulo.getStyle().set("color", "#1e3a8a").set("margin", "0");

        HorizontalLayout enc = new HorizontalLayout(titulo, btnNuevo);
        enc.setWidthFull();
        enc.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        enc.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        enc.getStyle().set("margin-bottom", "16px");

        grid = new Grid<>(Empleado.class, false);
        grid.setSizeFull();

        grid.addColumn(Empleado::getNombre)
                .setHeader("Nombre")
                .setFlexGrow(1);

        grid.addColumn(Empleado::getUsuario)
                .setHeader("Usuario")
                .setWidth("140px");

        grid.addColumn(e -> e.getCargo().name())
                .setHeader("Cargo")
                .setWidth("130px");

        grid.addColumn(e -> e.isActivo() ? "✅ Activo" : "❌ Inactivo")
                .setHeader("Estado")
                .setWidth("110px");

        grid.addComponentColumn(emp -> {
                    Button edit = new Button(VaadinIcon.EDIT.create());
                    edit.addThemeVariants(
                            ButtonVariant.LUMO_SMALL,
                            ButtonVariant.LUMO_TERTIARY
                    );
                    edit.addClickListener(e -> abrirFormulario(emp));

                    Button del = new Button(VaadinIcon.TRASH.create());
                    del.addThemeVariants(
                            ButtonVariant.LUMO_SMALL,
                            ButtonVariant.LUMO_ERROR,
                            ButtonVariant.LUMO_TERTIARY
                    );
                    del.addClickListener(e -> {
                        dao.eliminar(emp.getId());
                        cargar();
                    });

                    return new HorizontalLayout(edit, del);
                })
                .setHeader("Acciones")
                .setWidth("110px");

        add(enc, grid);
        cargar();
    }

    private void cargar() {
        grid.setItems(dao.listarTodos());
    }

    private void abrirFormulario(Empleado emp) {
        boolean esNuevo = (emp == null);

        if (esNuevo) {
            emp = new Empleado();
        }

        Empleado e = emp;

        Dialog dlg = new Dialog();
        dlg.setHeaderTitle(esNuevo ? "Nuevo Usuario" : "Editar Usuario");

        TextField txNom = new TextField("Nombre");
        txNom.setWidthFull();
        txNom.setValue(e.getNombre() == null ? "" : e.getNombre());

        TextField txUsr = new TextField("Usuario");
        txUsr.setWidthFull();
        txUsr.setValue(e.getUsuario() == null ? "" : e.getUsuario());

        PasswordField txPass = new PasswordField("Contraseña");
        txPass.setWidthFull();

        Select<Empleado.Cargo> selCargo = new Select<>();
        selCargo.setLabel("Cargo");
        selCargo.setItems(Empleado.Cargo.values());
        selCargo.setValue(
                e.getCargo() != null ? e.getCargo() : Empleado.Cargo.CAJERO
        );
        selCargo.setWidthFull();

        VerticalLayout form = new VerticalLayout(txNom, txUsr, txPass, selCargo);
        form.setPadding(false);

        dlg.add(form);

        Button guardar = new Button("Guardar", ev -> {

            e.setNombre(txNom.getValue());
            e.setUsuario(txUsr.getValue());
            e.setCargo(selCargo.getValue());

            // VALIDACIÓN CORRECTA DE CONTRASEÑA
            if (txPass.getValue() != null && !txPass.getValue().isEmpty()) {
                e.setContrasena(
                        com.facturacion.service.AuthService.hashPassword(txPass.getValue())
                );
            }

            if (esNuevo) {
                dao.guardar(e);
            } else {
                dao.actualizar(e);
            }

            cargar();
            dlg.close();
        });

        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dlg.getFooter().add(
                new Button("Cancelar", ev -> dlg.close()),
                guardar
        );

        dlg.open();
    }
}