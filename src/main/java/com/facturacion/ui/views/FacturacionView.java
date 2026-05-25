package com.facturacion.ui.views;

import com.facturacion.dao.EmpleadoDAO;
import com.facturacion.model.*;
import com.facturacion.service.AuthService;
import com.facturacion.service.FacturacionService;
import com.facturacion.ui.MainLayout;
import com.facturacion.util.PdfGeneratorUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Nueva Factura | Facturación Electrónica")
public class FacturacionView extends VerticalLayout {

    private final FacturacionService service = new FacturacionService();
    private final AuthService auth = AuthService.getInstance();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    private Factura factura = new Factura();
    private final List<DetalleFactura> detalles = new ArrayList<>();

    private ComboBox<Cliente> cmbCliente;
    private ComboBox<Producto> cmbProducto;
    private IntegerField txtCantidad;
    private Grid<DetalleFactura> grid;
    private Span lblSubtotal, lblIva, lblTotal;

    public FacturacionView() {
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background", "#f1f5f9")
                .set("min-height", "100%")
                .set("box-sizing", "border-box");

        add(crearHeader(), crearContenido());
        iniciarFactura();
    }

    // ---- HEADER ----
    private HorizontalLayout crearHeader() {
        H2 titulo = new H2("🧾 Nueva Factura Electrónica");
        titulo.getStyle()
                .set("color", "#1e3a8a")
                .set("margin", "0")
                .set("font-size", "1.2rem");

        Span numero = new Span(factura.getNumeroFactura() != null ? factura.getNumeroFactura() : "");
        numero.getStyle()
                .set("background", "#dbeafe")
                .set("color", "#1e40af")
                .set("padding", "4px 12px")
                .set("border-radius", "20px")
                .set("font-size", "0.75rem")
                .set("font-weight", "600")
                .set("white-space", "nowrap");

        HorizontalLayout hl = new HorizontalLayout(titulo, numero);
        hl.setWidthFull();
        hl.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        hl.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        hl.getStyle()
                .set("padding", "16px 20px 8px 20px")
                .set("background", "#f1f5f9");
        return hl;
    }

    // ---- CONTENIDO PRINCIPAL ----
    private HorizontalLayout crearContenido() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.getStyle()
                .set("padding", "0 20px 20px 20px")
                .set("gap", "16px")
                .set("align-items", "flex-start")
                .set("box-sizing", "border-box");

        // ---- COLUMNA IZQUIERDA ----
        VerticalLayout left = new VerticalLayout(
                panelCliente(),
                panelProducto(),
                gridDetalles()
        );
        left.getStyle().set("flex", "1").set("min-width", "0");
        left.setPadding(false);
        left.setSpacing(false);
        left.getStyle().set("gap", "12px");

        // ---- COLUMNA DERECHA ----
        VerticalLayout right = new VerticalLayout(panelResumen());
        right.getStyle()
                .set("width", "300px")
                .set("min-width", "280px")
                .set("flex-shrink", "0");
        right.setPadding(false);
        right.setSpacing(false);

        layout.add(left, right);
        return layout;
    }

    // ---- PANEL CLIENTE ----
    private Div panelCliente() {
        Div card = crearCard("👤 Cliente");

        cmbCliente = new ComboBox<>();
        cmbCliente.setItems(service.listarClientes());
        cmbCliente.setItemLabelGenerator(c -> c.getNombre() + " — CC: " + c.getCedula());
        cmbCliente.setWidthFull();
        cmbCliente.setClearButtonVisible(true);
        cmbCliente.setPlaceholder("Buscar por nombre o cédula...");

        Button btnNuevo = new Button("+ Nuevo", VaadinIcon.USER_CARD.create());
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);
        btnNuevo.addClickListener(e -> abrirDialogoNuevoCliente());
        btnNuevo.getStyle().set("white-space", "nowrap");

        HorizontalLayout fila = new HorizontalLayout(cmbCliente, btnNuevo);
        fila.setWidthFull();
        fila.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        fila.expand(cmbCliente);
        fila.setPadding(false);

        card.add(fila);
        return card;
    }

    // ---- PANEL PRODUCTO ----
    private Div panelProducto() {
        Div card = crearCard("📦 Agregar Producto");

        cmbProducto = new ComboBox<>();
        cmbProducto.setItems(service.listarProductos());
        cmbProducto.setItemLabelGenerator(p -> "[" + p.getCodigo() + "] " + p.getNombre()
                + " — $" + String.format("%,.0f", p.getPrecio()));
        cmbProducto.setWidthFull();
        cmbProducto.setClearButtonVisible(true);
        cmbProducto.setPlaceholder("Buscar producto...");

        txtCantidad = new IntegerField();
        txtCantidad.setValue(1);
        txtCantidad.setMin(1);
        txtCantidad.setStepButtonsVisible(true);
        txtCantidad.setWidth("120px");
        txtCantidad.setLabel("Cantidad");

        Button btn = new Button("Agregar", VaadinIcon.PLUS.create(), e -> agregarProducto());
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.getStyle()
                .set("background", "#1e3a8a")
                .set("white-space", "nowrap");

        HorizontalLayout fila = new HorizontalLayout(cmbProducto, txtCantidad, btn);
        fila.setWidthFull();
        fila.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        fila.expand(cmbProducto);
        fila.setPadding(false);

        card.add(fila);
        return card;
    }

    // ---- GRID DETALLES ----
    private Div gridDetalles() {
        Div card = crearCard("📋 Líneas de Factura");

        grid = new Grid<>(DetalleFactura.class, false);
        grid.setHeight("200px");
        grid.setWidthFull();

        grid.addColumn(d -> d.getProducto().getCodigo())
                .setHeader("Código").setWidth("80px").setFlexGrow(0);
        grid.addColumn(d -> d.getProducto().getNombre())
                .setHeader("Producto").setFlexGrow(1);
        grid.addColumn(DetalleFactura::getCantidad)
                .setHeader("Cant.").setWidth("60px").setFlexGrow(0);
        grid.addColumn(d -> "$" + String.format("%,.0f", d.getPrecioUnitario()))
                .setHeader("Precio").setWidth("90px").setFlexGrow(0);
        grid.addColumn(d -> d.getIva() + "%")
                .setHeader("IVA").setWidth("55px").setFlexGrow(0);
        grid.addColumn(d -> "$" + String.format("%,.0f", d.getSubtotal()))
                .setHeader("Subtotal").setWidth("100px").setFlexGrow(0);
        grid.addComponentColumn(det -> {
            Button del = new Button(VaadinIcon.TRASH.create());
            del.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR,
                    ButtonVariant.LUMO_TERTIARY);
            del.addClickListener(e -> {
                detalles.remove(det);
                grid.setItems(detalles);
                calcular();
            });
            return del;
        }).setWidth("55px").setFlexGrow(0);

        grid.setItems(detalles);
        card.add(grid);
        return card;
    }

    // ---- PANEL RESUMEN ----
    private Div panelResumen() {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 6px rgba(0,0,0,0.07)")
                .set("overflow", "hidden");

        // Encabezado azul
        Div header = new Div();
        header.getStyle()
                .set("background", "#1e3a8a")
                .set("padding", "14px 20px");
        H4 tituloRes = new H4("💰 Resumen de Factura");
        tituloRes.getStyle()
                .set("margin", "0")
                .set("color", "white")
                .set("font-size", "0.95rem");
        header.add(tituloRes);

        // Cuerpo
        Div body = new Div();
        body.getStyle().set("padding", "16px 20px");

        lblSubtotal = new Span("$0");
        lblSubtotal.getStyle().set("font-weight", "600").set("color", "#374151");
        lblIva = new Span("$0");
        lblIva.getStyle().set("font-weight", "600").set("color", "#374151");
        lblTotal = new Span("$0");
        lblTotal.getStyle()
                .set("font-size", "1.5rem")
                .set("font-weight", "800")
                .set("color", "#1e3a8a");

        body.add(
                filaResumen("Subtotal (sin IVA)", lblSubtotal),
                crearDivider(),
                filaResumen("IVA (19%)", lblIva),
                crearDivider()
        );

        // Fila total
        Div filaTotal = new Div();
        filaTotal.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("padding", "12px 0 4px 0");
        Span lblTotalTxt = new Span("TOTAL A PAGAR");
        lblTotalTxt.getStyle()
                .set("font-weight", "700")
                .set("color", "#1e3a8a")
                .set("font-size", "0.9rem");
        filaTotal.add(lblTotalTxt, lblTotal);
        body.add(filaTotal);

        // Botones
        Div botones = new Div();
        botones.getStyle()
                .set("padding", "16px 20px")
                .set("border-top", "1px solid #f3f4f6")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "8px");

        Button btnLimpiar = new Button("Limpiar todo", VaadinIcon.REFRESH.create(),
                e -> limpiar());
        btnLimpiar.setWidthFull();
        btnLimpiar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button btnGenerar = new Button("Generar Factura", VaadinIcon.CHECK_CIRCLE.create(),
                e -> generarFactura());
        btnGenerar.setWidthFull();
        btnGenerar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnGenerar.getStyle()
                .set("height", "46px")
                .set("font-size", "1rem")
                .set("font-weight", "600");

        botones.add(btnLimpiar, btnGenerar);
        card.add(header, body, botones);
        return card;
    }

    // ---- HELPERS ----

    private Div crearCard(String titulo) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "14px 18px")
                .set("box-shadow", "0 1px 6px rgba(0,0,0,0.07)");

        H4 h = new H4(titulo);
        h.getStyle()
                .set("margin", "0 0 12px 0")
                .set("color", "#374151")
                .set("font-size", "0.9rem")
                .set("border-bottom", "2px solid #e0e7ff")
                .set("padding-bottom", "8px");

        card.add(h);
        return card;
    }

    private Div crearDivider() {
        Div div = new Div();
        div.getStyle()
                .set("height", "1px")
                .set("background", "#f3f4f6")
                .set("margin", "4px 0");
        return div;
    }

    private Div filaResumen(String label, Span valor) {
        Div fila = new Div();
        fila.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("padding", "8px 0");
        Span lbl = new Span(label);
        lbl.getStyle().set("color", "#6b7280").set("font-size", "0.88rem");
        fila.add(lbl, valor);
        return fila;
    }

    private void iniciarFactura() {
        factura = new Factura();
        factura.setNumeroFactura(service.generarNumeroFactura());
        // Asignar empleado desde la sesión
        if (auth.getEmpleadoActual() != null) {
            factura.setEmpleado(auth.getEmpleadoActual());
        } else {
            // Si no hay sesión activa, usar el empleado con ID=1 (admin por defecto)
            empleadoDAO.buscarPorId(1).ifPresent(factura::setEmpleado);
        }
    }

    private void agregarProducto() {
        Producto prod = cmbProducto.getValue();
        Integer cant = txtCantidad.getValue();

        if (prod == null) { error("Seleccione un producto."); return; }
        if (cant == null || cant < 1) { error("La cantidad debe ser mayor a 0."); return; }
        if (cant > prod.getStock()) { error("Stock insuficiente. Disponible: " + prod.getStock()); return; }

        for (DetalleFactura d : detalles) {
            if (d.getProducto().getId() == prod.getId()) {
                d.setCantidad(d.getCantidad() + cant);
                grid.setItems(detalles);
                calcular();
                cmbProducto.clear();
                txtCantidad.setValue(1);
                return;
            }
        }

        detalles.add(new DetalleFactura(prod, cant));
        grid.setItems(detalles);
        calcular();
        cmbProducto.clear();
        txtCantidad.setValue(1);
    }

    private void calcular() {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleFactura d : detalles) {
            BigDecimal base = d.getPrecioUnitario().multiply(new BigDecimal(d.getCantidad()));
            subtotal = subtotal.add(base);
            total = total.add(d.getSubtotal());
        }

        BigDecimal iva = total.subtract(subtotal);
        lblSubtotal.setText("$" + String.format("%,.0f", subtotal));
        lblIva.setText("$" + String.format("%,.0f", iva));
        lblTotal.setText("$" + String.format("%,.0f", total));
    }

    private void generarFactura() {
        if (cmbCliente.getValue() == null) { error("Seleccione un cliente."); return; }
        if (detalles.isEmpty()) { error("Agregue al menos un producto."); return; }

        factura.setCliente(cmbCliente.getValue());
        factura.setDetalles(new ArrayList<>(detalles));
        factura.calcularTotal();

        // Verificar empleado antes de guardar
        if (factura.getEmpleado() == null) {
            if (auth.getEmpleadoActual() != null) {
                factura.setEmpleado(auth.getEmpleadoActual());
            } else {
                empleadoDAO.buscarPorId(1).ifPresent(factura::setEmpleado);
            }
        }

        if (factura.getEmpleado() == null) {
            error("No se pudo identificar el cajero. Inicie sesión nuevamente.");
            return;
        }

        try {
            int id = service.crearFactura(factura);
            if (id > 0) {
                exito("✅ Factura " + factura.getNumeroFactura() + " generada correctamente.");
                descargarPdf();
                limpiar();
            } else {
                error("Error al guardar la factura.");
            }
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void descargarPdf() {
        byte[] bytes = PdfGeneratorUtil.generarPdf(factura);
        String nombre = "factura-" + factura.getNumeroFactura() + ".pdf";
        StreamResource res = new StreamResource(nombre, () -> new ByteArrayInputStream(bytes));
        res.setContentType("application/pdf");
        Anchor link = new Anchor(res, "");
        link.getElement().setAttribute("download", true);
        link.setTarget("_blank");
        Button btnPdf = new Button("Descargar PDF", VaadinIcon.DOWNLOAD.create());
        btnPdf.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        link.add(btnPdf);
        Notification notif = new Notification();
        notif.setDuration(8000);
        notif.setPosition(Notification.Position.BOTTOM_END);
        notif.add(new HorizontalLayout(new Span("Factura lista: "), link));
        notif.open();
    }

    private void abrirDialogoNuevoCliente() {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Nuevo Cliente");
        dlg.setWidth("400px");

        TextField nom = new TextField("Nombre completo"); nom.setWidthFull();
        TextField ced = new TextField("Cédula / NIT");    ced.setWidthFull();
        TextField tel = new TextField("Teléfono");        tel.setWidthFull();
        TextField cor = new TextField("Correo");          cor.setWidthFull();
        TextField dir = new TextField("Dirección");       dir.setWidthFull();

        VerticalLayout form = new VerticalLayout(nom, ced, tel, cor, dir);
        form.setPadding(false);
        dlg.add(form);

        Button guardar = new Button("Guardar", e -> {
            Cliente c = new Cliente();
            c.setNombre(nom.getValue().trim());
            c.setCedula(ced.getValue().trim());
            c.setTelefono(tel.getValue().trim());
            c.setCorreo(cor.getValue().trim());
            c.setDireccion(dir.getValue().trim());
            com.facturacion.dao.ClienteDAO dao = new com.facturacion.dao.ClienteDAO();
            int id = dao.guardar(c);
            if (id > 0) {
                cmbCliente.setItems(service.listarClientes());
                cmbCliente.setValue(c);
                exito("Cliente registrado.");
                dlg.close();
            } else {
                error("Error al guardar el cliente.");
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dlg.getFooter().add(new Button("Cancelar", e -> dlg.close()), guardar);
        dlg.open();
    }

    private void limpiar() {
        detalles.clear();
        cmbCliente.clear();
        cmbProducto.clear();
        txtCantidad.setValue(1);
        grid.setItems(detalles);
        calcular();
        iniciarFactura();
    }

    private void error(String msg) {
        Notification n = Notification.show(msg, 4000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void exito(String msg) {
        Notification n = Notification.show(msg, 4000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
