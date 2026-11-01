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
import com.landlord.android.core.db.PaymentEntity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public final class PdfReceiptGenerator {

    private PdfReceiptGenerator() {}

    public static File generateReceiptPdf(Context context, PaymentEntity payment, String tenantName, String unitInfo) {
        PdfDocument document = new PdfDocument();
        int pageWidth = 595;
        int pageHeight = 842;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Header Background Banner
        paint.setColor(Color.parseColor("#312E81")); // Deep Indigo
        canvas.drawRect(0, 0, pageWidth, 120, paint);

        // Header Title
        paint.setColor(Color.WHITE);
        paint.setTextSize(22);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("LANDLORD MANAGEMENT SUITE", 40, 50, paint);

        // Header Subtitle
        paint.setColor(Color.parseColor("#C7D2FE"));
        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Official Rent Payment Receipt & Financial Acknowledgment", 40, 75, paint);

        // Receipt Meta Info Box
        paint.setColor(Color.parseColor("#F8FAFC"));
        RectF metaBox = new RectF(40, 140, pageWidth - 40, 240);
        canvas.drawRoundRect(metaBox, 12, 12, paint);

        paint.setColor(Color.parseColor("#E2E8F0"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        canvas.drawRoundRect(metaBox, 12, 12, paint);
        paint.setStyle(Paint.Style.FILL);

        // Meta Text - Left Column
        paint.setColor(Color.parseColor("#64748B"));
        paint.setTextSize(11);
        canvas.drawText("RECEIPT ID", 60, 168, paint);
        canvas.drawText("PAYMENT DATE", 60, 198, paint);
        canvas.drawText("PAYMENT METHOD", 60, 224, paint);

        paint.setColor(Color.parseColor("#0F172A"));
        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(payment.localId != null ? payment.localId : "N/A", 180, 168, paint);
        canvas.drawText(payment.date != null ? payment.date : "N/A", 180, 198, paint);
        canvas.drawText(payment.method != null ? payment.method.toUpperCase(Locale.US) : "CASH", 180, 224, paint);

        // Meta Text - Right Column
        paint.setColor(Color.parseColor("#64748B"));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(11);
        canvas.drawText("TENANT", 320, 168, paint);
        canvas.drawText("UNIT / PROPERTY", 320, 198, paint);
        canvas.drawText("TRANSACTION REF", 320, 224, paint);

        paint.setColor(Color.parseColor("#0F172A"));
        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(tenantName != null ? tenantName : "Tenant", 430, 168, paint);
        canvas.drawText(unitInfo != null ? unitInfo : "Assigned Unit", 430, 198, paint);
        String ref = payment.transactionRef != null && !payment.transactionRef.isEmpty() ? payment.transactionRef : "Direct Deposit";
        canvas.drawText(ref, 430, 224, paint);

        // Table Header
        paint.setColor(Color.parseColor("#F1F5F9"));
        canvas.drawRoundRect(new RectF(40, 260, pageWidth - 40, 295), 8, 8, paint);

        paint.setColor(Color.parseColor("#334155"));
        paint.setTextSize(11);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DESCRIPTION", 60, 282, paint);
        canvas.drawText("STATUS", 320, 282, paint);
        canvas.drawText("AMOUNT (BDT)", pageWidth - 160, 282, paint);

        // Table Row Item
        paint.setColor(Color.parseColor("#0F172A"));
        paint.setTextSize(13);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Monthly Apartment Rent Payment", 60, 330, paint);
        canvas.drawText("CONFIRMED", 320, 330, paint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(String.format(Locale.US, "৳%,.2f", payment.amount), pageWidth - 160, 330, paint);

        // Table Divider Line
        paint.setColor(Color.parseColor("#CBD5E1"));
        paint.setStrokeWidth(1f);
        canvas.drawLine(40, 355, pageWidth - 40, 355, paint);

        // Total Received Box
        paint.setColor(Color.parseColor("#EEF2FF"));
        RectF totalBox = new RectF(pageWidth - 260, 375, pageWidth - 40, 425);
        canvas.drawRoundRect(totalBox, 8, 8, paint);

        paint.setColor(Color.parseColor("#312E81"));
        paint.setTextSize(11);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("TOTAL RECEIVED", pageWidth - 245, 395, paint);

        paint.setColor(Color.parseColor("#4F46E5"));
        paint.setTextSize(18);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(String.format(Locale.US, "৳%,.2f", payment.amount), pageWidth - 245, 417, paint);

        // Verified Stamp Badge
        paint.setColor(Color.parseColor("#ECFDF5"));
        RectF stampBox = new RectF(40, 375, 260, 425);
        canvas.drawRoundRect(stampBox, 8, 8, paint);

        paint.setColor(Color.parseColor("#10B981"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        canvas.drawRoundRect(stampBox, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.parseColor("#065F46"));
        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("✓ PAID & VERIFIED IN FULL", 58, 405, paint);

        // Footer Notes & Divider
        paint.setColor(Color.parseColor("#E2E8F0"));
        canvas.drawLine(40, pageHeight - 80, pageWidth - 40, pageHeight - 80, paint);

        paint.setColor(Color.parseColor("#94A3B8"));
        paint.setTextSize(10);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("This is an official computer-generated rent payment voucher issued by LandLord Management Suite.", 40, pageHeight - 55, paint);
        canvas.drawText("All financial transactions are stored locally with tamper-proof audit trails.", 40, pageHeight - 40, paint);

        document.finishPage(page);

        // Save file to cache receipts directory
        File receiptsDir = new File(context.getCacheDir(), "receipts");
        if (!receiptsDir.exists()) {
            receiptsDir.mkdirs();
        }
        File pdfFile = new File(receiptsDir, "receipt_" + payment.localId + ".pdf");
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            document.writeTo(fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
        return pdfFile;
    }

    public static void openOrShareReceipt(Context context, File pdfFile) {
        if (pdfFile == null || !pdfFile.exists()) {
            Toast.makeText(context, "Error: PDF file could not be generated", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception ex) {
            // Fallback to share intent if no default PDF viewer installed
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(shareIntent, "Open or Share Rent Receipt PDF"));
        }
    }
}
