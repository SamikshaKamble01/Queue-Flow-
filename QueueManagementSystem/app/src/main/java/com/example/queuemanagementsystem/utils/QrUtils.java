package com.example.queuemanagementsystem.utils;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public final class QrUtils {

    private QrUtils() {
    }

    public static Bitmap createQrCode(String value) throws WriterException {
        BarcodeEncoder encoder = new BarcodeEncoder();
        BitMatrix matrix = encoder.encode(value, BarcodeFormat.QR_CODE, 600, 600);
        return encoder.createBitmap(matrix);
    }
}
