package com.spottrack.platform.analytics.infrastructure.reporting;

import com.spottrack.platform.analytics.domain.model.valueobjects.EquipmentUsage;
import com.spottrack.platform.analytics.domain.model.valueobjects.HourlyOccupancy;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageHeatmap;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageReport;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenPdfUsageReportRendererTest {

    private static final LocalDate FROM = LocalDate.of(2026, 10, 1);
    private static final LocalDate TO = LocalDate.of(2026, 10, 28);

    private final OpenPdfUsageReportRenderer renderer = new OpenPdfUsageReportRenderer();

    @Test
    void producesANonEmptyDocumentThatStartsWithPdfAndEndsWithEof() {
        var pdf = renderer.render(report("Gym Central", "Sede San Isidro", 5));

        assertTrue(pdf.length > 1_000);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.ISO_8859_1));
        var tail = new String(pdf, Math.max(0, pdf.length - 32), Math.min(32, pdf.length), StandardCharsets.ISO_8859_1);
        assertTrue(tail.contains("%%EOF"));
    }

    @Test
    void containsTheKeyTextsOfTheReport() throws IOException {
        var text = textOf(renderer.render(report("Gym Ñuñoa Central", "Sede San Isidro", 5)));

        assertTrue(text.contains("Reporte de uso de equipos"));
        assertTrue(text.contains("Gym Ñuñoa Central"));
        assertTrue(text.contains("Sede San Isidro"));
        assertTrue(text.contains("01/10/2026 - 28/10/2026"));
        assertTrue(text.contains("Ocupación promedio"));
        assertTrue(text.contains("Variación vs. periodo anterior"));
        assertTrue(text.contains("Hora pico"));
        assertTrue(text.contains("18:00 - 19:00"));
        assertTrue(text.contains("Ocupación por hora del día"));
        assertTrue(text.contains("Umbral 90 %"));
        assertTrue(text.contains("Uso por equipo"));
        assertTrue(text.contains("Prensa de piernas 0"));
        assertTrue(text.contains("Página 1"));
    }

    @Test
    void omitsTheBranchWhenTheReportCoversTheWholeGym() throws IOException {
        var text = textOf(renderer.render(report("Gym Central", null, 2)));

        assertTrue(text.contains("Gym Central"));
        assertFalse(text.contains("Sede:"));
    }

    @Test
    void rendersAnEmptyHeatmapWithoutFailing() throws IOException {
        var empty = new UsageHeatmap(FROM, TO, 6, 22, 0, 0, 0, null, 0, List.of(), List.of());
        var pdf = renderer.render(new UsageReport("Gym Central", null, LocalDateTime.of(2026, 10, 28, 9, 30), empty, Map.of()));

        var text = textOf(pdf);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.ISO_8859_1));
        assertTrue(text.contains("Sin uso registrado en el periodo seleccionado."));
        assertTrue(text.contains("No hay equipos registrados en este alcance."));
        assertTrue(text.contains("Sin uso"));
        assertEquals(1, pageCount(pdf));
    }

    @Test
    void rendersEquipmentWithoutAnyUsageAsAFlatChart() throws IOException {
        var hourly = new ArrayList<HourlyOccupancy>();
        for (int hour = 6; hour < 22; hour++) {
            hourly.add(new HourlyOccupancy(hour, 0));
        }
        var idle = List.of(new EquipmentUsage("eq-1-abcdefgh", "zone", 0, 0, 0, 0));
        var heatmap = new UsageHeatmap(FROM, TO, 6, 22, 1, 0, 0, null, 0, hourly, idle);

        var text = textOf(renderer.render(new UsageReport("Gym", null, LocalDateTime.now(), heatmap, Map.of("eq-1-abcdefgh", "Remo"))));

        assertTrue(text.contains("Sin uso registrado en el periodo seleccionado."));
        assertTrue(text.contains("Remo"));
    }

    @Test
    void spillsOverToMoreThanOnePageWithManyEquipment() throws IOException {
        var pdf = renderer.render(report("Gym Central", null, 150));

        assertTrue(pageCount(pdf) > 1);
        var text = textOf(pdf);
        assertTrue(text.contains("Página 2"));
        assertTrue(text.contains("Prensa de piernas 0"));
        assertTrue(text.contains("Prensa de piernas 149"));
    }

    @Test
    void replacesCharactersOutsideWinAnsiInsteadOfFailing() throws IOException {
        var heatmap = heatmap(1);
        var names = Map.of("eq-0-abcdefgh", "Cinta 🏋️ Pro ñandú");
        var pdf = renderer.render(new UsageReport("日本語 Gym", "Sede 東京", LocalDateTime.now(), heatmap, names));

        var text = textOf(pdf);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.ISO_8859_1));
        assertTrue(text.contains("Cinta ? Pro ñandú"));
        assertTrue(text.contains("? Gym"));
        assertTrue(text.contains("Sede ?"));
    }

    @Test
    void fallsBackToAShortIdWhenTheEquipmentNameIsUnknown() throws IOException {
        var text = textOf(renderer.render(new UsageReport("Gym", null, LocalDateTime.now(), heatmap(1), Map.of())));

        assertTrue(text.contains("Equipo eq-0-abc"));
    }

    @Test
    void sanitizeKeepsAccentsAndDropsEverythingElse() {
        assertEquals("Peña Ñandú Café", OpenPdfUsageReportRenderer.sanitize("Peña Ñandú Café"));
        assertEquals("a b c", OpenPdfUsageReportRenderer.sanitize("a\nb\t c"));
        assertEquals("x ? y", OpenPdfUsageReportRenderer.sanitize("x ☃☃ y"));
        assertEquals("", OpenPdfUsageReportRenderer.sanitize(null));
        assertEquals(80, OpenPdfUsageReportRenderer.sanitize("a".repeat(200)).length());
    }

    private static UsageReport report(String gym, String branch, int equipmentCount) {
        var names = new HashMap<String, String>();
        for (int i = 0; i < equipmentCount; i++) {
            names.put("eq-" + i + "-abcdefgh", "Prensa de piernas " + i);
        }
        return new UsageReport(gym, branch, LocalDateTime.of(2026, 10, 28, 9, 30), heatmap(equipmentCount), names);
    }

    private static UsageHeatmap heatmap(int equipmentCount) {
        var hourly = new ArrayList<HourlyOccupancy>();
        for (int hour = 6; hour < 22; hour++) {
            hourly.add(new HourlyOccupancy(hour, hour == 18 ? 94.3 : Math.max(0, 70 - Math.abs(hour - 12) * 8.5)));
        }
        var equipment = new ArrayList<EquipmentUsage>();
        for (int i = 0; i < equipmentCount; i++) {
            equipment.add(new EquipmentUsage("eq-" + i + "-abcdefgh", "zone-1", 3000.0 - i * 10, 40, Math.max(0, 80 - i * 0.5), 0.5));
        }
        return new UsageHeatmap(FROM, TO, 6, 22, equipmentCount, 52.4, 47.1, 18, 94.3, hourly, equipment);
    }

    private static int pageCount(byte[] pdf) throws IOException {
        var reader = new PdfReader(pdf);
        try {
            return reader.getNumberOfPages();
        } finally {
            reader.close();
        }
    }

    private static String textOf(byte[] pdf) throws IOException {
        var reader = new PdfReader(pdf);
        try {
            var extractor = new PdfTextExtractor(reader);
            var text = new StringBuilder();
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                text.append(extractor.getTextFromPage(page)).append('\n');
            }
            return text.toString().replaceAll("\\s+", " ");
        } finally {
            reader.close();
        }
    }
}
