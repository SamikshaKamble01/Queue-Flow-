package com.example.queuemanagementsystem.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.example.queuemanagementsystem.data.model.TokenItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

public final class ReportExportHelper {

    private ReportExportHelper() {
    }

    public static String exportCsv(Context context, String fileName, List<TokenItem> tokens) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("Token,Service,Date,Time,Status,Booked At\n");
        for (TokenItem token : tokens) {
            builder.append(token.getTokenNumber()).append(',')
                    .append(safe(token.getServiceName())).append(',')
                    .append(safe(token.getSlotDate())).append(',')
                    .append(safe(token.getSlotStartTime())).append(" - ").append(safe(token.getSlotEndTime())).append(',')
                    .append(safe(token.getStatus())).append(',')
                    .append(DateTimeUtils.formatDateTime(token.getCreatedAt()))
                    .append('\n');
        }

        try (OutputStream stream = openDownloadStream(context, fileName, "text/csv")) {
            stream.write(builder.toString().getBytes());
        }
        return fileName;
    }

    public static String exportPdf(Context context, String fileName, List<TokenItem> tokens) throws Exception {
        PdfDocument document = new PdfDocument();
        Paint titlePaint = new Paint();
        titlePaint.setFakeBoldText(true);
        titlePaint.setTextSize(18f);

        Paint bodyPaint = new Paint();
        bodyPaint.setTextSize(11f);

        int pageNumber = 1;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(842, 1191, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int y = 48;
        canvas.drawText("QueueFlow Pro Report", 40, y, titlePaint);
        y += 28;
        canvas.drawText("Generated " + DateTimeUtils.formatDateTime(System.currentTimeMillis()), 40, y, bodyPaint);
        y += 34;

        for (TokenItem token : tokens) {
            if (y > 1120) {
                document.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(842, 1191, pageNumber).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 48;
            }

            String line = String.format(
                    Locale.getDefault(),
                    "%s | %s | %s %s-%s | %s",
                    safe(token.getTokenNumber()),
                    safe(token.getServiceName()),
                    safe(token.getSlotDate()),
                    safe(token.getSlotStartTime()),
                    safe(token.getSlotEndTime()),
                    safe(token.getStatus())
            );
            canvas.drawText(line, 40, y, bodyPaint);
            y += 20;
        }

        document.finishPage(page);
        try (OutputStream stream = openDownloadStream(context, fileName, "application/pdf")) {
            document.writeTo(stream);
        } finally {
            document.close();
        }
        return fileName;
    }

    private static OutputStream openDownloadStream(Context context, String fileName, String mimeType) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, mimeType);
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QueueFlowReports");
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                throw new IllegalStateException("Could not create download file.");
            }
            OutputStream stream = resolver.openOutputStream(uri);
            if (stream == null) {
                throw new IllegalStateException("Could not open output stream.");
            }
            return stream;
        }

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "QueueFlowReports");
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException("Could not create reports folder.");
        }
        return new FileOutputStream(new File(folder, fileName));
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace(",", " ");
    }
}