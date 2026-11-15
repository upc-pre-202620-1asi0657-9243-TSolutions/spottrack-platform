package com.spottrack.platform.monitoring.infrastructure.qr;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZxingQrCodeRendererTest {

    private static final String PAIRING_JSON =
            "{\"v\":1,\"deviceId\":\"066dd222-31ed-496a-bddf-e4711b0ee957\",\"secret\":\"Zk3Qm-_9aB1cD2eF3gH4iJ5kL6mN7oP8\","
                    + "\"host\":\"34.123.45.67\",\"port\":8883,"
                    + "\"caSha256\":\"9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08\",\"exp\":1790000000}";

    private final ZxingQrCodeRenderer renderer = new ZxingQrCodeRenderer();

    private static String decode(byte[] png) throws Exception {
        var image = ImageIO.read(new ByteArrayInputStream(png));
        var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        return new MultiFormatReader().decode(bitmap).getText();
    }

    @Test
    void producesAPngThatDecodesBackToTheSameContent() throws Exception {
        var png = renderer.renderPng(PAIRING_JSON, 320);

        assertArrayEquals(new byte[]{(byte) 0x89, 'P', 'N', 'G'}, java.util.Arrays.copyOf(png, 4));
        assertEquals(PAIRING_JSON, decode(png));
    }

    @Test
    void theImageHasTheRequestedSize() throws Exception {
        var image = ImageIO.read(new ByteArrayInputStream(renderer.renderPng(PAIRING_JSON, 400)));

        assertEquals(400, image.getWidth());
        assertEquals(400, image.getHeight());
    }

    @Test
    void nonAsciiContentSurvivesTheRoundTrip() throws Exception {
        assertEquals("año-ñandú-✓", decode(renderer.renderPng("año-ñandú-✓", 300)));
    }

    @Test
    void blankContentIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> renderer.renderPng(" ", 320));
        assertThrows(IllegalArgumentException.class, () -> renderer.renderPng(null, 320));
    }

    @Test
    void nonPositiveSizeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> renderer.renderPng(PAIRING_JSON, 0));
    }

    @Test
    void contentTooLargeForAQrIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> renderer.renderPng("x".repeat(5000), 320));
    }
}
