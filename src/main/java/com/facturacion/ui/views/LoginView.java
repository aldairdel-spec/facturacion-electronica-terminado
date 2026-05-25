package com.facturacion.ui.views;

import com.facturacion.service.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.router.*;


@Route("login")
@PageTitle("Iniciar Sesión | Facturación Electrónica")

public class LoginView extends VerticalLayout {

    private final AuthService authService = AuthService.getInstance();

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%)");

        add(crearTarjetaLogin());
    }

    private VerticalLayout crearTarjetaLogin() {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("380px");
        card.setAlignItems(Alignment.CENTER);

        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("box-shadow", "0 10px 40px rgba(30,58,138,0.15)")
                .set("padding", "40px 32px");

        // Ícono
        Div iconDiv = new Div();
        iconDiv.getStyle()
                .set("background", "linear-gradient(135deg, #1e3a8a, #2563eb)")
                .set("border-radius", "50%")
                .set("width", "64px")
                .set("height", "64px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-bottom", "8px");

        Span icono = new Span("🧾");
        icono.getStyle().set("font-size", "1.8rem");
        iconDiv.add(icono);

        // Título
        H2 titulo = new H2("Facturación Electrónica");
        titulo.getStyle()
                .set("margin", "0")
                .set("color", "#1e3a8a")
                .set("font-size", "1.3rem")
                .set("text-align", "center");

        Paragraph subtitulo = new Paragraph("Ingrese sus credenciales para continuar");
        subtitulo.getStyle()
                .set("color", "#6b7280")
                .set("margin", "4px 0 24px 0")
                .set("font-size", "0.85rem");

        // Campos
        TextField txtUsuario = new TextField("Usuario");
        txtUsuario.setWidthFull();
        txtUsuario.setPrefixComponent(new Span("👤"));
        txtUsuario.setPlaceholder("Ingrese su usuario");

        PasswordField txtPassword = new PasswordField("Contraseña");
        txtPassword.setWidthFull();
        txtPassword.setPrefixComponent(new Span("🔒"));
        txtPassword.setPlaceholder("Ingrese su contraseña");

        // Botón
        Button btnLogin = new Button("Ingresar al Sistema");
        btnLogin.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnLogin.setWidthFull();

        btnLogin.getStyle()
                .set("background", "linear-gradient(135deg, #1e3a8a, #2563eb)")
                .set("color", "white")
                .set("height", "44px")
                .set("font-size", "0.95rem")
                .set("border-radius", "8px")
                .set("margin-top", "8px");

        // LOGIN CORREGIDO
        Runnable doLogin = () -> {
            String usuario = txtUsuario.getValue();
            String password = txtPassword.getValue();

            // Validación correcta para Strings
            if (usuario == null || usuario.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {

                mostrarError("Por favor complete todos los campos.");
                return;
            }

            if (authService.login(usuario.trim(), password)) {
                UI.getCurrent().navigate(FacturacionView.class);
            } else {
                mostrarError("Usuario o contraseña incorrectos.");
                txtPassword.clear();
            }
        };

        btnLogin.addClickListener(e -> doLogin.run());
        txtPassword.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> doLogin.run());

        // Footer
        Span version = new Span("v1.0 | Sistema de Facturación DIAN");
        version.getStyle()
                .set("font-size", "0.7rem")
                .set("color", "#9ca3af")
                .set("margin-top", "16px");

        card.add(iconDiv, titulo, subtitulo, txtUsuario, txtPassword, btnLogin, version);

        return card;
    }

    private void mostrarError(String mensaje) {
        Notification notif = Notification.show(mensaje, 3000, Notification.Position.TOP_CENTER);
        notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}