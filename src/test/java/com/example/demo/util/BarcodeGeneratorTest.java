package com.example.demo.util;

import com.example.demo.model.BarcodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BarcodeGeneratorTest {

    @Test
    void testGenerateQRCodeBase64() {
        String qr = BarcodeGenerator.generateQRCodeBase64("https://example.com/verify/123", 100, 100);
        assertNotNull(qr);
        assertTrue(qr.startsWith("data:image/png;base64,"));
    }

    @Test
    void testGenerateBarcodeBase64Code128() {
        String barcode = BarcodeGenerator.generateBarcodeBase64("REG-2026-001", BarcodeType.CODE_128, 150, 50);
        assertNotNull(barcode);
        assertTrue(barcode.startsWith("data:image/png;base64,"));
    }

    @Test
    void testGenerateBarcodeBase64Ean13WithValidDigits() {
        // EAN-13 expects exactly 13 digits (12 digit inputs are padded to 13 digits with calculated checksum)
        String barcode = BarcodeGenerator.generateBarcodeBase64("123456789012", BarcodeType.EAN_13, 150, 50);
        assertNotNull(barcode);
        assertTrue(barcode.startsWith("data:image/png;base64,"));
    }

    @Test
    void testGenerateBarcodeBase64Ean13FallbackToCode128() {
        // If input contains alphanumeric characters or too few digits, it should safely fallback to Code 128
        String barcode = BarcodeGenerator.generateBarcodeBase64("ABC-SHORT", BarcodeType.EAN_13, 150, 50);
        assertNotNull(barcode);
        assertTrue(barcode.startsWith("data:image/png;base64,"));
    }
}
