package com.facturacion.util;

import com.facturacion.model.DetalleFactura;
import com.facturacion.model.Factura;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

public class PdfGeneratorUtil {

    private static final Logger logger = LoggerFactory.getLogger(PdfGeneratorUtil.class);

    private static final Font FONT_TITULO    = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    private static final Font FONT_NORMAL    = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL);
    private static final Font FONT_BOLD      = new Font(Font.FontFamily.HELVETICA, 9,  Font.BOLD);
    private static final Font FONT_SMALL     = new Font(Font.FontFamily.HELVETICA, 8,  Font.NORMAL);

    private static final BaseColor COLOR_HEADER = new BaseColor(30, 58, 138);
    private static final BaseColor COLOR_ROW_ALT = new BaseColor(240, 244, 255);

    private PdfGeneratorUtil() {}

    public static byte[] generarPdf(Factura factura) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            agregarEncabezado(doc, factura);
            agregarInfoFactura(doc, factura);
            agregarTablaDetalles(doc, factura);
            agregarTotales(doc, factura);
            agregarPie(doc);

            doc.close();
            logger.info("PDF generado para factura #{}", factura.getNumeroFactura());
        } catch (DocumentException ex) {
            logger.error("Error al generar PDF: {}", ex.getMessage(), ex);
        }
        return baos.toByteArray();
    }

    private static void agregarEncabezado(Document doc, Factura f) throws DocumentException {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{60, 40});
        tabla.setSpacingAfter(10);

        PdfPCell celdaEmpresa = new PdfPCell();
        celdaEmpresa.setBorder(Rectangle.NO_BORDER);

        Paragraph empresa = new Paragraph("SUPERMERCADO EL BUEN PRECIO S.A.S", FONT_TITULO);
        empresa.add(Chunk.NEWLINE);
        empresa.add(new Chunk("NIT: 900.123.456-7", FONT_NORMAL));
        empresa.add(Chunk.NEWLINE);
        empresa.add(new Chunk("Calle 45 # 23-10 | Bogotá, Colombia", FONT_NORMAL));
        empresa.add(Chunk.NEWLINE);
        empresa.add(new Chunk("Tel: (601) 234-5678 | info@buenprecio.com", FONT_NORMAL));

        celdaEmpresa.addElement(empresa);
        tabla.addCell(celdaEmpresa);

        PdfPCell celdaFactura = new PdfPCell();
        celdaFactura.setBackgroundColor(COLOR_HEADER);
        celdaFactura.setBorder(Rectangle.NO_BORDER);
        celdaFactura.setHorizontalAlignment(Element.ALIGN_CENTER);
        celdaFactura.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celdaFactura.setPadding(10);

        Font blanco = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        Font blancoSm = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.WHITE);

        Paragraph numFact = new Paragraph();
        numFact.add(new Chunk("FACTURA ELECTRÓNICA DE VENTA\n", blanco));
        numFact.add(new Chunk("No. " + f.getNumeroFactura(), blanco));
        numFact.add(Chunk.NEWLINE);
        numFact.add(new Chunk("Fecha: " +
                f.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), blancoSm));

        numFact.setAlignment(Element.ALIGN_CENTER);
        celdaFactura.addElement(numFact);
        tabla.addCell(celdaFactura);

        doc.add(tabla);

        // Línea separadora (CORREGIDO)
        LineSeparator ls = new LineSeparator();
        ls.setLineWidth(1f);
        ls.setPercentage(100);
        doc.add(ls);

        doc.add(Chunk.NEWLINE);
    }

    private static void agregarInfoFactura(Document doc, Factura f) throws DocumentException {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(10);

        agregarFila2Col(tabla, "DATOS DEL CLIENTE", "DATOS DEL CAJERO", true);

        agregarFila2Col(tabla,
                "Nombre: " + (f.getCliente() != null ? f.getCliente().getNombre() : "-"),
                "Cajero: " + (f.getEmpleado() != null ? f.getEmpleado().getNombre() : "-"), false);

        agregarFila2Col(tabla,
                "Cédula/NIT: " + (f.getCliente() != null ? f.getCliente().getCedula() : "-"),
                "Cargo: " + (f.getEmpleado() != null ? f.getEmpleado().getCargo() : "-"), false);

        agregarFila2Col(tabla,
                "Teléfono: " + (f.getCliente() != null ? f.getCliente().getTelefono() : "-"),
                "Estado: " + f.getEstado().name(), false);

        agregarFila2Col(tabla,
                "Correo: " + (f.getCliente() != null ? f.getCliente().getCorreo() : "-"),
                "", false);

        doc.add(tabla);
    }

    private static void agregarTablaDetalles(Document doc, Factura f) throws DocumentException {
        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{8, 35, 10, 15, 10, 15});
        tabla.setSpacingAfter(5);

        String[] headers = {"#", "Producto", "Cant.", "Precio Unit.", "IVA %", "Subtotal"};

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h,
                    new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE)));
            cell.setBackgroundColor(COLOR_HEADER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            tabla.addCell(cell);
        }

        int i = 1;
        for (DetalleFactura det : f.getDetalles()) {
            BaseColor fondo = (i % 2 == 0) ? COLOR_ROW_ALT : BaseColor.WHITE;
            agregarFilaDetalle(tabla, i++, det, fondo);
        }

        doc.add(tabla);
    }

    private static void agregarFilaDetalle(PdfPTable tabla, int num, DetalleFactura det, BaseColor fondo) {
        addCell(tabla, String.valueOf(num), fondo, Element.ALIGN_CENTER);
        addCell(tabla, det.getProducto().getNombre(), fondo, Element.ALIGN_LEFT);
        addCell(tabla, String.valueOf(det.getCantidad()), fondo, Element.ALIGN_CENTER);
        addCell(tabla, "$" + String.format("%,.2f", det.getPrecioUnitario()), fondo, Element.ALIGN_RIGHT);
        addCell(tabla, det.getIva() + "%", fondo, Element.ALIGN_CENTER);
        addCell(tabla, "$" + String.format("%,.2f", det.getSubtotal()), fondo, Element.ALIGN_RIGHT);
    }

    private static void agregarTotales(Document doc, Factura f) throws DocumentException {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(45);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.setSpacingAfter(15);

        agregarFilaTotales(tabla, "Subtotal:", "$" + f.getSubtotalSinIVA(), false);
        agregarFilaTotales(tabla, "IVA:", "$" + f.getTotalIVA(), false);
        agregarFilaTotales(tabla, "TOTAL:", "$" + f.getTotal(), true);

        doc.add(tabla);
    }

    private static void agregarFilaTotales(PdfPTable t, String label, String valor, boolean resaltar) {
        Font f = resaltar ? FONT_BOLD : FONT_NORMAL;

        PdfPCell c1 = new PdfPCell(new Phrase(label, f));
        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell c2 = new PdfPCell(new Phrase(valor, f));
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);

        t.addCell(c1);
        t.addCell(c2);
    }

    private static void agregarPie(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);

        Paragraph p = new Paragraph(
                "Factura electrónica válida ante la DIAN.\nGracias por su compra.",
                FONT_SMALL);

        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
    }

    private static void agregarFila2Col(PdfPTable t, String col1, String col2, boolean esHeader) {
        Font f = esHeader ? FONT_BOLD : FONT_NORMAL;

        PdfPCell c1 = new PdfPCell(new Phrase(col1, f));
        PdfPCell c2 = new PdfPCell(new Phrase(col2, f));

        t.addCell(c1);
        t.addCell(c2);
    }

    private static void addCell(PdfPTable t, String texto, BaseColor fondo, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_NORMAL));
        cell.setBackgroundColor(fondo);
        cell.setHorizontalAlignment(align);
        t.addCell(cell);
    }
}