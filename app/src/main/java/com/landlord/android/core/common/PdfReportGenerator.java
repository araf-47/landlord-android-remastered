package com.landlord.android.core.common;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class PdfReportGenerator {

    private PdfReportGenerator() {}

    public static File generateReportPdf(Context context, String reportTitle, String subtitle,
                                         List<String> summaryLines, List<String> tableHeaders,
                                         List<String[]> tableRows) {
        PdfDocument document = new PdfDocument();
        int pageWidth = 595;
        int pageHeight = 842;
        int marginX = 36;
        int usableWidth = pageWidth - (marginX * 2);

        int pageNumber = 1;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Header Background Banner
        paint.setColor(Color.parseColor("#1E1B4B")); // Deep Midnight Indigo
        canvas.drawRect(0, 0, pageWidth, 110, paint);

        // Header Accent Stripe
        paint.setColor(Color.parseColor("#4F46E5")); // Bright Indigo
        canvas.drawRect(0, 110, pageWidth, 114, paint);

        // Header Title
        paint.setColor(Color.WHITE);
        paint.setTextSize(18);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(reportTitle.toUpperCase(Locale.US), marginX, 48, paint);

        // Header Subtitle
        paint.setColor(Color.parseColor("#C7D2FE"));
        paint.setTextSize(10.5f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText(subtitle, marginX, 70, paint);

        String generatedAt = "Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());
        paint.setColor(Color.parseColor("#94A3B8"));
        paint.setTextSize(9.5f);
        canvas.drawText(generatedAt, marginX, 90, paint);

        int currentY = 135;

        // Summary Metric Box
        if (summaryLines != null && !summaryLines.isEmpty()) {
            paint.setColor(Color.parseColor("#F8FAFC"));
            int boxHeight = (summaryLines.size() * 20) + 16;
            RectF summaryBox = new RectF(marginX, currentY, marginX + usableWidth, currentY + boxHeight);
            canvas.drawRoundRect(summaryBox, 8, 8, paint);

            paint.setColor(Color.parseColor("#CBD5E1"));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1f);
            canvas.drawRoundRect(summaryBox, 8, 8, paint);
            paint.setStyle(Paint.Style.FILL);

            paint.setColor(Color.parseColor("#0F172A"));
            paint.setTextSize(10.5f);

            int lineY = currentY + 18;
            for (String line : summaryLines) {
                if (line.contains(":") || line.contains("•")) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                } else {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                }
                canvas.drawText(fitText(line, paint, usableWidth - 24), marginX + 12, lineY, paint);
                lineY += 20;
            }
            currentY += boxHeight + 18;
        }

        // Table Header & Rows
        if (tableHeaders != null && !tableHeaders.isEmpty()) {
            int colCount = tableHeaders.size();
            float[] colWeights = computeColumnWeights(colCount, tableHeaders);
            float[] colWidths = new float[colCount];
            float[] colPositions = new float[colCount];

            float runningX = marginX;
            for (int i = 0; i < colCount; i++) {
                colWidths[i] = usableWidth * colWeights[i];
                colPositions[i] = runningX;
                runningX += colWidths[i];
            }

            // Draw Table Header Bar
            paint.setColor(Color.parseColor("#EEF2FF"));
            canvas.drawRoundRect(new RectF(marginX, currentY, marginX + usableWidth, currentY + 26), 6, 6, paint);

            paint.setColor(Color.parseColor("#312E81"));
            paint.setTextSize(9.5f);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            for (int i = 0; i < colCount; i++) {
                float cellLeft = colPositions[i] + 8;
                float cellRight = colPositions[i] + colWidths[i] - 6;
                float maxTextWidth = cellRight - cellLeft;
                String headerText = fitText(tableHeaders.get(i).toUpperCase(Locale.US), paint, maxTextWidth);

                canvas.save();
                canvas.clipRect(colPositions[i], currentY, colPositions[i] + colWidths[i], currentY + 26);
                canvas.drawText(headerText, cellLeft, currentY + 17, paint);
                canvas.restore();
            }
            currentY += 32;

            // Draw Table Rows with strict cell boundaries & no overlapping
            if (tableRows != null) {
                paint.setTextSize(9.5f);
                int rowCount = 0;
                for (String[] row : tableRows) {
                    if (currentY > pageHeight - 80) {
                        // Multi-page support: start a fresh page for remaining rows
                        drawFooter(canvas, paint, pageWidth, pageHeight, marginX, usableWidth);
                        document.finishPage(page);

                        pageNumber++;
                        pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();

                        currentY = 40;
                        // Repeat table header on continuation page
                        paint.setColor(Color.parseColor("#EEF2FF"));
                        canvas.drawRoundRect(new RectF(marginX, currentY, marginX + usableWidth, currentY + 26), 6, 6, paint);

                        paint.setColor(Color.parseColor("#312E81"));
                        paint.setTextSize(9.5f);
                        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                        for (int i = 0; i < colCount; i++) {
                            float cellLeft = colPositions[i] + 8;
                            float maxTextWidth = colWidths[i] - 14;
                            String headerText = fitText(tableHeaders.get(i).toUpperCase(Locale.US), paint, maxTextWidth);
                            canvas.save();
                            canvas.clipRect(colPositions[i], currentY, colPositions[i] + colWidths[i], currentY + 26);
                            canvas.drawText(headerText, cellLeft, currentY + 17, paint);
                            canvas.restore();
                        }
                        currentY += 32;
                    }

                    if (rowCount % 2 == 1) {
                        paint.setColor(Color.parseColor("#F8FAFC"));
                        canvas.drawRect(marginX, currentY - 11, marginX + usableWidth, currentY + 9, paint);
                    }

                    paint.setColor(Color.parseColor("#1E293B"));
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

                    for (int c = 0; c < row.length && c < colCount; c++) {
                        String rawText = row[c] != null ? row[c] : "";
                        float cellLeft = colPositions[c] + 8;
                        float cellRight = colPositions[c] + colWidths[c] - 6;
                        float maxTextWidth = Math.max(10, cellRight - cellLeft);

                        String fitted = fitText(rawText, paint, maxTextWidth);

                        canvas.save();
                        canvas.clipRect(colPositions[c], currentY - 12, colPositions[c] + colWidths[c], currentY + 10);
                        canvas.drawText(fitted, cellLeft, currentY, paint);
                        canvas.restore();
                    }

                    currentY += 20;
                    rowCount++;
                }
            }
        }

        drawFooter(canvas, paint, pageWidth, pageHeight, marginX, usableWidth);
        document.finishPage(page);

        File reportsDir = new File(context.getCacheDir(), "reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
        String cleanTitle = reportTitle.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase(Locale.US);
        File pdfFile = new File(reportsDir, "report_" + cleanTitle + "_" + System.currentTimeMillis() + ".pdf");
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            document.writeTo(fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
        return pdfFile;
    }

    private static void drawFooter(Canvas canvas, Paint paint, int pageWidth, int pageHeight, int marginX, int usableWidth) {
        paint.setColor(Color.parseColor("#E2E8F0"));
        canvas.drawLine(marginX, pageHeight - 55, marginX + usableWidth, pageHeight - 55, paint);

        paint.setColor(Color.parseColor("#94A3B8"));
        paint.setTextSize(8.5f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Certified report generated by LandLord Management Suite • Secure Local Architecture", marginX, pageHeight - 38, paint);
        canvas.drawText("Confidential property and financial data for owner and authorized personnel only.", marginX, pageHeight - 26, paint);
    }

    private static float[] computeColumnWeights(int count, List<String> headers) {
        if (count == 3) {
            return new float[]{0.44f, 0.28f, 0.28f};
        } else if (count == 4) {
            return new float[]{0.34f, 0.30f, 0.20f, 0.16f};
        } else if (count == 5) {
            return new float[]{0.18f, 0.32f, 0.16f, 0.16f, 0.18f};
        } else {
            float[] equal = new float[count];
            float w = 1f / count;
            for (int i = 0; i < count; i++) equal[i] = w;
            return equal;
        }
    }

    private static String fitText(String text, Paint paint, float maxWidth) {
        if (text == null || text.isEmpty()) return "";
        if (paint.measureText(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        float ellipsisWidth = paint.measureText(ellipsis);
        if (ellipsisWidth >= maxWidth) {
            return "";
        }

        int len = text.length();
        while (len > 0 && paint.measureText(text.substring(0, len)) + ellipsisWidth > maxWidth) {
            len--;
        }
        return text.substring(0, len) + ellipsis;
    }

    public static void openOrShareReport(Context context, File pdfFile, String title) {
        if (pdfFile == null || !pdfFile.exists()) {
            Toast.makeText(context, "Error: PDF report could not be generated", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception ex) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(shareIntent, "Open or Share " + title));
        }
    }
}
