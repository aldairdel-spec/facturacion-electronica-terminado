package com.facturacion.ui;

import com.facturacion.service.AuthService;
import com.facturacion.ui.views.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.sidenav.*;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final AuthService authService = AuthService.getInstance();

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addToNavbar(true, crearNavbar());
        addToDrawer(crearDrawer());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!authService.estaAutenticado()) {
            event.rerouteTo(LoginView.class);
        }
    }

    // ---- NAVBAR SUPERIOR ----
    private HorizontalLayout crearNavbar() {
        DrawerToggle toggle = new DrawerToggle();

        H1 titulo = new H1("Facturación Electrónica");
        titulo.getStyle()
                .set("font-size", "1rem")
                .set("margin", "0")
                .set("color", "#1e3a8a")
                .set("font-weight", "700");

        // Nombre del usuario logueado en la navbar
        String nombreUser = authService.estaAutenticado()
                ? authService.getEmpleadoActual().getNombre() : "Usuario";
        Span usuarioSpan = new Span("👤 " + nombreUser);
        usuarioSpan.getStyle()
                .set("font-size", "0.85rem")
                .set("color", "#374151")
                .set("margin-right", "16px");

        HorizontalLayout layout = new HorizontalLayout(toggle, titulo);
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        layout.setWidthFull();
        layout.getStyle()
                .set("padding", "0 16px")
                .set("border-bottom", "1px solid #e5e7eb");
        layout.expand(titulo);
        layout.add(usuarioSpan);
        return layout;
    }

    // ---- DRAWER LATERAL ----
    private VerticalLayout crearDrawer() {

        // ---- LOGO ----
        Div logo = new Div();
        logo.getStyle()
                .set("background", "linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%)")
                .set("padding", "24px 16px")
                .set("text-align", "center")
                .set("width", "100%");

        Span emoji = new Span("🧾");
        emoji.getStyle()
                .set("font-size", "2.2rem")
                .set("display", "block")
                .set("margin-bottom", "6px");

        H2 nombre = new H2("SuperMarket");
        nombre.getStyle()
                .set("color", "white")
                .set("font-size", "1.15rem")
                .set("margin", "0")
                .set("font-weight", "700")
                .set("letter-spacing", "0.5px");

        Span sub = new Span("Sistema de Facturación");
        sub.getStyle()
                .set("color", "#93c5fd")
                .set("font-size", "0.72rem")
                .set("margin-top", "2px")
                .set("display", "block");

        logo.add(emoji, nombre, sub);

        // ---- INFO USUARIO ----
        String nombreUser = authService.estaAutenticado()
                ? authService.getEmpleadoActual().getNombre() : "Usuario";
        String cargoUser = authService.estaAutenticado()
                ? authService.getEmpleadoActual().getCargo().name() : "";

        Avatar avatar = new Avatar(nombreUser);
        avatar.setColorIndex(2);
        avatar.getStyle()
                .set("width", "36px")
                .set("height", "36px")
                .set("flex-shrink", "0");

        Span spanNombre = new Span(nombreUser);
        spanNombre.getStyle()
                .set("font-weight", "600")
                .set("font-size", "0.85rem")
                .set("color", "#111827");

        // Badge de cargo
        Span spanCargo = new Span(cargoUser);
        spanCargo.getStyle()
                .set("font-size", "0.68rem")
                .set("background", "#dbeafe")
                .set("color", "#1e40af")
                .set("padding", "2px 8px")
                .set("border-radius", "20px")
                .set("font-weight", "500");

        VerticalLayout infoUser = new VerticalLayout(spanNombre, spanCargo);
        infoUser.setSpacing(false);
        infoUser.setPadding(false);

        HorizontalLayout userBox = new HorizontalLayout(avatar, infoUser);
        userBox.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        userBox.setWidthFull();
        userBox.getStyle()
                .set("padding", "14px 16px")
                .set("background", "#f8fafc")
                .set("border-bottom", "1px solid #e5e7eb");

        // ---- LABEL MENÚ ----
        Span labelMenu = new Span("MENÚ PRINCIPAL");
        labelMenu.getStyle()
                .set("font-size", "0.65rem")
                .set("color", "#9ca3af")
                .set("font-weight", "700")
                .set("letter-spacing", "1px")
                .set("padding", "12px 16px 4px 16px")
                .set("display", "block");

        // ---- NAVEGACIÓN ----
        SideNav nav = new SideNav();
        nav.getStyle()
                .set("padding", "4px 8px")
                .set("width", "100%");

        SideNavItem itemFactura = new SideNavItem(
                "Nueva Factura", FacturacionView.class, VaadinIcon.PLUS_CIRCLE.create());
        SideNavItem itemHistorial = new SideNavItem(
                "Historial", HistorialView.class, VaadinIcon.LIST.create());
        SideNavItem itemProductos = new SideNavItem(
                "Productos", ProductosView.class, VaadinIcon.PACKAGE.create());
        SideNavItem itemClientes = new SideNavItem(
                "Clientes", ClientesView.class, VaadinIcon.USER.create());

        nav.addItem(itemFactura, itemHistorial, itemProductos, itemClientes);

        // Items solo para administrador
        if (authService.esAdministrador()) {
            Span labelAdmin = new Span("ADMINISTRACIÓN");
            labelAdmin.getStyle()
                    .set("font-size", "0.65rem")
                    .set("color", "#9ca3af")
                    .set("font-weight", "700")
                    .set("letter-spacing", "1px")
                    .set("padding", "12px 16px 4px 16px")
                    .set("display", "block");

            SideNav navAdmin = new SideNav();
            navAdmin.getStyle()
                    .set("padding", "4px 8px")
                    .set("width", "100%");

            navAdmin.addItem(
                    new SideNavItem("Usuarios", UsuariosView.class, VaadinIcon.USERS.create()),
                    new SideNavItem("Reportes", ReportesView.class, VaadinIcon.CHART.create())
            );

            // ---- VERSIÓN ----
            Span version = new Span("v1.0 | DIAN 2026");
            version.getStyle()
                    .set("font-size", "0.7rem")
                    .set("color", "#d1d5db")
                    .set("text-align", "center")
                    .set("display", "block")
                    .set("padding", "8px");

            // ---- LOGOUT ----
            Button logout = new Button("Cerrar Sesión", VaadinIcon.SIGN_OUT.create(), e -> {
                authService.logout();
                UI.getCurrent().navigate(LoginView.class);
            });
            logout.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            logout.getStyle()
                    .set("width", "calc(100% - 32px)")
                    .set("margin", "0 16px 16px 16px");

            Div spacer = new Div();
            VerticalLayout drawer = new VerticalLayout(
                    logo, userBox, labelMenu, nav, labelAdmin, navAdmin,
                    spacer, version, logout
            );
            drawer.setSizeFull();
            drawer.setPadding(false);
            drawer.setSpacing(false);
            drawer.expand(spacer);
            return drawer;
        }

        // ---- VERSIÓN ----
        Span version = new Span("v1.0 | DIAN 2026");
        version.getStyle()
                .set("font-size", "0.7rem")
                .set("color", "#d1d5db")
                .set("text-align", "center")
                .set("display", "block")
                .set("padding", "8px");

        // ---- LOGOUT ----
        Button logout = new Button("Cerrar Sesión", VaadinIcon.SIGN_OUT.create(), e -> {
            authService.logout();
            UI.getCurrent().navigate(LoginView.class);
        });
        logout.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        logout.getStyle()
                .set("width", "calc(100% - 32px)")
                .set("margin", "0 16px 16px 16px");

        Div spacer = new Div();
        VerticalLayout drawer = new VerticalLayout(
                logo, userBox, labelMenu, nav,
                spacer, version, logout
        );
        drawer.setSizeFull();
        drawer.setPadding(false);
        drawer.setSpacing(false);
        drawer.expand(spacer);
        return drawer;
    }
}
