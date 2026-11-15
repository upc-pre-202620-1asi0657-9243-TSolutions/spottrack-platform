package com.spottrack.platform.monitoring.infrastructure.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.spottrack.platform.monitoring.application.ports.QrCodeRenderer;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class ZxingQrCodeRenderer implements QrCodeRenderer {
    private static final Map<EncodeHintType, Object> HINTS = Map.of(
            EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name(),
            EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN, 2);

    @Override
    public byte[] renderPng(String content, int sizePx) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("QR content must not be blank");
        }
        if (sizePx <= 0) {
            throw new IllegalArgumentException("QR size must be positive");
        }
        try {
            var matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, HINTS);
            var out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException e) {
            throw new IllegalArgumentException("The content cannot be encoded as a QR: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
