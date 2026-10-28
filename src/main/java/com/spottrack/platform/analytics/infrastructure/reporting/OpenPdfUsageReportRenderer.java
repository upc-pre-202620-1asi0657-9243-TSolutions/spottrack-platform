package com.spottrack.platform.analytics.infrastructure.reporting;

import com.spottrack.platform.analytics.application.outboundservices.UsageReportRenderer;
import com.spottrack.platform.analytics.domain.model.valueobjects.EquipmentUsage;
import com.spottrack.platform.analytics.domain.model.valueobjects.HourlyOccupancy;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageHeatmap;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageReport;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Draws the usage report with built-in Helvetica (no font files needed), so text is limited to
 * WinAnsi: accents and ñ render, anything else in user-provided names becomes "?".
 * Charts are plain vector shapes drawn from the heatmap figures.
 */
@Component
public class OpenPdfUsageReportRenderer implements UsageReportRenderer {

    private static final Locale ES = Locale.of("es", "PE");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Charset WIN_ANSI = Charset.forName("windows-1252");
    private static final int MAX_NAME_LENGTH = 80;
    private static final double STRESS_THRESHOLD = 90;
    private static final float CHART_HEIGHT = 190;

    private static final Color NAVY = new Color(0x1F, 0x2A, 0x44);
    private static final Color ACCENT = new Color(0x2F, 0x6F, 0xED);
    private static final Color ACCENT_LIGHT = new Color(0xC9, 0xDA, 0xFB);
    private static final Color STRESS = new Color(0xD9, 0x4A, 0x3D);
    private static final Color GRID = new Color(0xD9, 0xDD, 0xE5);
    private static final Color MUTED = new Color(0x6B, 0x72, 0x80);
    private static final Color ROW_ALT = new Color(0xF5, 0xF7, 0xFA);

    private static final Font TITLE = new Font(Font.HELVETICA, 20, Font.BOLD, NAVY);
    private static final Font SECTION = new Font(Font.HELVETICA, 12, Font.BOLD, NAVY);
    private static final Font BODY = new Font(Font.HELVETICA, 9.5f, Font.NORMAL, Color.BLACK);
    private static final Font BODY_BOLD = new Font(Font.HELVETICA, 9.5f, Font.BOLD, Color.BLACK);
    private static final Font SMALL = new Font(Font.HELVETICA, 8, Font.NORMAL, MUTED);
    private static final Font TINY = new Font(Font.HELVETICA, 7, Font.NORMAL, MUTED);
    private static final Font KPI_VALUE = new Font(Font.HELVETICA, 17, Font.BOLD, NAVY);
    private static final Font TABLE_HEAD = new Font(Font.HELVETICA, 8.5f, Font.BOLD, Color.WHITE);

    @Override
    public byte[] render(UsageReport report) {
        var out = new ByteArrayOutputStream();
        var document = new Document(PageSize.A4, 40, 40, 40, 56);
        try {
            var writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new FooterEvent());
            document.addTitle("Reporte de uso de equipos");
            document.addAuthor("SpotTrack");
            document.addCreator("SpotTrack Platform");
            document.open();

            addHeader(document, report);
            addKpis(document, report.heatmap());
            addHourlySection(document, report.heatmap());
            addEquipmentSection(document, report);
            document.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Could not render the usage report", e);
        }
        return out.toByteArray();
    }

    private void addHeader(Document document, UsageReport report) throws DocumentException {
        var heatmap = report.heatmap();
        document.add(new Paragraph("Reporte de uso de equipos", TITLE));

        var gym = report.gymName() != null ? sanitize(report.gymName()) : "Gimnasio";
        var scope = new Paragraph();
        scope.setSpacingBefore(4);
        scope.add(new Phrase("Gimnasio: ", SMALL));
        scope.add(new Phrase(gym, BODY_BOLD));
        if (report.branchName() != null) {
            scope.add(new Phrase("     Sede: ", SMALL));
            scope.add(new Phrase(sanitize(report.branchName()), BODY_BOLD));
        }
        document.add(scope);

        var period = new Paragraph();
        period.setSpacingBefore(2);
        period.add(new Phrase("Periodo: ", SMALL));
        period.add(new Phrase(heatmap.from().format(DATE) + " - " + heatmap.to().format(DATE), BODY_BOLD));
        period.add(new Phrase("     Generado: ", SMALL));
        period.add(new Phrase(report.generatedAt().format(DATE_TIME), BODY));
        document.add(period);

        var rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        rule.setSpacingBefore(8);
        var cell = new PdfPCell();
        cell.setFixedHeight(3);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(ACCENT);
        rule.addCell(cell);
        document.add(rule);
    }

    private void addKpis(Document document, UsageHeatmap heatmap) throws DocumentException {
        var delta = heatmap.occupancyRate() - heatmap.previousOccupancyRate();
        var table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(14);
        table.addCell(kpi("Ocupación promedio", percent(heatmap.occupancyRate()),
                "Horario " + heatmap.openingHour() + ":00 - " + heatmap.closingHour() + ":00"));
        table.addCell(kpi("Variación vs. periodo anterior", signedPoints(delta),
                "Periodo anterior: " + percent(heatmap.previousOccupancyRate())));
        table.addCell(kpi("Hora pico", heatmap.peakHour() == null ? "Sin uso" : hourRange(heatmap.peakHour()),
                "Ocupación en el pico: " + percent(heatmap.peakOccupancyRate())));
        table.addCell(kpi("Equipos analizados", String.valueOf(heatmap.equipmentCount()),
                "No incluye equipos dados de baja"));
        document.add(table);
    }

    private PdfPCell kpi(String label, String value, String caption) {
        var cell = new PdfPCell();
        cell.setBorderColor(GRID);
        cell.setPadding(8);
        cell.addElement(new Paragraph(label, SMALL));
        var valueParagraph = new Paragraph(value, KPI_VALUE);
        valueParagraph.setSpacingBefore(2);
        cell.addElement(valueParagraph);
        cell.addElement(new Paragraph(caption, TINY));
        return cell;
    }

    private void addHourlySection(Document document, UsageHeatmap heatmap) throws DocumentException {
        document.add(sectionTitle("Ocupación por hora del día"));

        var table = new PdfPTable(1);
        table.setWidthPercentage(100);
        var cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(CHART_HEIGHT);
        cell.setCellEvent((c, position, canvases) ->
                drawHourlyChart(heatmap.hourly(), position, canvases[PdfPTable.BACKGROUNDCANVAS],
                        canvases[PdfPTable.LINECANVAS], canvases[PdfPTable.TEXTCANVAS]));
        table.addCell(cell);
        document.add(table);

        var hasUsage = heatmap.hourly().stream().anyMatch(h -> h.occupancyRate() > 0);
        var note = new Paragraph(hasUsage
                ? "Porcentaje promedio de equipos en uso en cada hora. Las barras rojas marcan horas con ocupación igual o superior a 90 % (picos de estrés)."
                : "Sin uso registrado en el periodo seleccionado.", SMALL);
        note.setSpacingBefore(2);
        document.add(note);
    }

    private void drawHourlyChart(List<HourlyOccupancy> hourly, Rectangle position,
                                 PdfContentByte bars, PdfContentByte lines, PdfContentByte text) {
        var left = position.getLeft() + 30;
        var right = position.getRight() - 6;
        var bottom = position.getBottom() + 22;
        var top = position.getTop() - 14;
        var height = top - bottom;

        lines.saveState();
        lines.setLineWidth(0.5f);
        lines.setColorStroke(GRID);
        for (int tick = 0; tick <= 100; tick += 25) {
            var y = bottom + (float) (tick / 100.0 * height);
            lines.moveTo(left, y);
            lines.lineTo(right, y);
            lines.stroke();
            ColumnText.showTextAligned(text, Element.ALIGN_RIGHT, new Phrase(tick + " %", TINY), left - 4, y - 2.5f, 0);
        }
        lines.restoreState();

        if (!hourly.isEmpty()) {
            var slot = (right - left) / hourly.size();
            var barWidth = slot * 0.6f;
            for (int i = 0; i < hourly.size(); i++) {
                var rate = Math.max(0, Math.min(100, hourly.get(i).occupancyRate()));
                var x = left + i * slot + (slot - barWidth) / 2;
                var barHeight = (float) (rate / 100.0 * height);
                if (barHeight > 0.3f) {
                    bars.saveState();
                    bars.setColorFill(rate >= STRESS_THRESHOLD ? STRESS : ACCENT);
                    bars.rectangle(x, bottom, barWidth, barHeight);
                    bars.fill();
                    bars.restoreState();
                    ColumnText.showTextAligned(text, Element.ALIGN_CENTER,
                            new Phrase(String.format(ES, "%.0f", rate), TINY), x + barWidth / 2, bottom + barHeight + 2, 0);
                }
                ColumnText.showTextAligned(text, Element.ALIGN_CENTER,
                        new Phrase(String.format("%02d", hourly.get(i).hour()), TINY), left + i * slot + slot / 2, bottom - 11, 0);
            }
        }

        var thresholdY = bottom + (float) (STRESS_THRESHOLD / 100.0 * height);
        lines.saveState();
        lines.setLineWidth(0.8f);
        lines.setColorStroke(STRESS);
        lines.setLineDash(3, 3, 0);
        lines.moveTo(left, thresholdY);
        lines.lineTo(right, thresholdY);
        lines.stroke();
        lines.restoreState();
        ColumnText.showTextAligned(text, Element.ALIGN_RIGHT,
                new Phrase("Umbral 90 %", new Font(Font.HELVETICA, 7, Font.NORMAL, STRESS)), right, thresholdY + 2, 0);

        lines.saveState();
        lines.setLineWidth(0.8f);
        lines.setColorStroke(MUTED);
        lines.moveTo(left, bottom);
        lines.lineTo(right, bottom);
        lines.stroke();
        lines.restoreState();
    }

    private void addEquipmentSection(Document document, UsageReport report) throws DocumentException {
        document.add(sectionTitle("Uso por equipo"));
        var equipment = report.heatmap().equipment();
        if (equipment.isEmpty()) {
            document.add(new Paragraph("No hay equipos registrados en este alcance.", SMALL));
            return;
        }

        var table = new PdfPTable(new float[]{6, 40, 28, 16, 10});
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        table.setSplitLate(true);
        for (var title : List.of("#", "Equipo", "Utilización", "Tiempo activo", "Sesiones")) {
            var head = new PdfPCell(new Phrase(title, TABLE_HEAD));
            head.setBackgroundColor(NAVY);
            head.setBorder(Rectangle.NO_BORDER);
            head.setPadding(5);
            table.addCell(head);
        }

        var sorted = equipment.stream()
                .sorted(Comparator.comparingDouble(EquipmentUsage::activeMinutes).reversed())
                .toList();
        for (int i = 0; i < sorted.size(); i++) {
            var row = sorted.get(i);
            var background = i % 2 == 1 ? ROW_ALT : Color.WHITE;
            table.addCell(bodyCell(String.valueOf(i + 1), Element.ALIGN_CENTER, background));
            table.addCell(bodyCell(equipmentName(report.equipmentNames(), row.equipmentId()), Element.ALIGN_LEFT, background));
            var utilization = bodyCell(percent(row.utilizationRate()), Element.ALIGN_RIGHT, background);
            utilization.setCellEvent((c, position, canvases) -> drawUtilizationBar(row.utilizationRate(), position, canvases[PdfPTable.BACKGROUNDCANVAS]));
            table.addCell(utilization);
            table.addCell(bodyCell(duration(row.activeMinutes()), Element.ALIGN_RIGHT, background));
            table.addCell(bodyCell(String.valueOf(row.sessionCount()), Element.ALIGN_RIGHT, background));
        }
        document.add(table);
    }

    private void drawUtilizationBar(double utilization, Rectangle position, PdfContentByte canvas) {
        var width = (position.getWidth() - 8) * (float) (Math.max(0, Math.min(100, utilization)) / 100.0);
        if (width <= 0.3f) {
            return;
        }
        canvas.saveState();
        canvas.setColorFill(ACCENT_LIGHT);
        canvas.rectangle(position.getLeft() + 4, position.getBottom() + 3, width, position.getHeight() - 6);
        canvas.fill();
        canvas.restoreState();
    }

    private PdfPCell bodyCell(String value, int alignment, Color background) {
        var cell = new PdfPCell(new Phrase(value, BODY));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(background);
        cell.setBorderColor(GRID);
        cell.setBorderWidth(0.4f);
        cell.setPadding(4);
        cell.setMinimumHeight(18);
        return cell;
    }

    private Paragraph sectionTitle(String title) {
        var paragraph = new Paragraph(title, SECTION);
        paragraph.setSpacingBefore(16);
        paragraph.setSpacingAfter(6);
        return paragraph;
    }

    private static String equipmentName(Map<String, String> names, String equipmentId) {
        var name = names.get(equipmentId);
        if (name == null || sanitize(name).isEmpty()) {
            return "Equipo " + equipmentId.substring(0, Math.min(8, equipmentId.length()));
        }
        return sanitize(name);
    }

    /** Keeps only what Helvetica/WinAnsi can draw; everything else becomes "?". */
    static String sanitize(String text) {
        if (text == null) {
            return "";
        }
        var encoder = WIN_ANSI.newEncoder();
        var clean = new StringBuilder();
        text.codePoints().forEach(codePoint -> {
            if (Character.isISOControl(codePoint) || Character.isWhitespace(codePoint)) {
                clean.append(' ');
            } else if (codePoint <= 0xFFFF && encoder.canEncode((char) codePoint)) {
                clean.appendCodePoint(codePoint);
            } else {
                clean.append('?');
            }
        });
        var result = clean.toString().replaceAll(" {2,}", " ").replaceAll("\\?{2,}", "?").strip();
        return result.length() > MAX_NAME_LENGTH ? result.substring(0, MAX_NAME_LENGTH).strip() : result;
    }

    private static String percent(double value) {
        return String.format(ES, "%.1f %%", value);
    }

    private static String signedPoints(double value) {
        return String.format(ES, "%+.1f pp", value);
    }

    private static String hourRange(int hour) {
        return String.format("%02d:00 - %02d:00", hour, hour + 1);
    }

    private static String duration(double minutes) {
        var total = (long) Math.round(minutes);
        return total < 60 ? total + " min" : String.format("%dh %02dmin", total / 60, total % 60);
    }

    private static final class FooterEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            var canvas = writer.getDirectContent();
            var y = document.bottom() - 24;
            canvas.saveState();
            canvas.setLineWidth(0.5f);
            canvas.setColorStroke(GRID);
            canvas.moveTo(document.left(), y + 12);
            canvas.lineTo(document.right(), y + 12);
            canvas.stroke();
            canvas.restoreState();
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase("SpotTrack · Reporte de uso de equipos", SMALL), document.left(), y, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), SMALL), document.right(), y, 0);
        }
    }
}
