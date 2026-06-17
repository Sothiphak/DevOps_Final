package com.example.demo.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.example.demo.model.BarcodeType;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class BarcodeGenerator {

    public static byte[] generateQRCodeImage(String text, int width, int height) throws Exception {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", bos);
        return bos.toByteArray();
    }

    public static String generateQRCodeBase64(String text, int width, int height) {
        try {
            byte[] image = generateQRCodeImage(text, width, height);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(image);
        } catch (Exception e) {
            return "";
        }
    }

    public static byte[] generateBarcodeImage(String text, BarcodeType type, int width, int height) throws Exception {
        BarcodeFormat format = BarcodeFormat.CODE_128;
        String sanitizedText = text;
        if (type == BarcodeType.EAN_13) {
            // EAN-13 needs exactly 13 digits.
            // Let's sanitize text to contain only digits, pad or truncate to 12 digits, then calculate checksum.
            String digitsOnly = text.replaceAll("\\D", "");
            if (digitsOnly.length() >= 12) {
                sanitizedText = digitsOnly.substring(0, 12);
                int sum = 0;
                for (int i = 0; i < 12; i++) {
                    int digit = Character.getNumericValue(sanitizedText.charAt(i));
                    sum += (i % 2 == 0) ? digit : digit * 3;
                }
                int checksum = (10 - (sum % 10)) % 10;
                sanitizedText = sanitizedText + checksum;
                format = BarcodeFormat.EAN_13;
            } else {
                // Fallback to Code 128 if not enough digits for EAN-13
                format = BarcodeFormat.CODE_128;
            }
        }
        
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix bitMatrix = writer.encode(sanitizedText, format, width, height);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", bos);
        return bos.toByteArray();
    }

    public static String generateBarcodeBase64(String text, BarcodeType type, int width, int height) {
        try {
            byte[] image = generateBarcodeImage(text, type, width, height);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(image);
        } catch (Exception e) {
            return "";
        }
    }
}
