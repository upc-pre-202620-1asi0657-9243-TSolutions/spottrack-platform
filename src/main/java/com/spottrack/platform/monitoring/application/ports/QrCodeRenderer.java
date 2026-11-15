package com.spottrack.platform.monitoring.application.ports;

/** Draws text as a QR code image. */
public interface QrCodeRenderer {
    /**
     * @param content text to encode (UTF-8)
     * @param sizePx  side of the square image, in pixels
     * @return a PNG image
     * @throws IllegalArgumentException when the content is blank, the size is not positive or the content does not fit in a QR
     */
    byte[] renderPng(String content, int sizePx);
}
