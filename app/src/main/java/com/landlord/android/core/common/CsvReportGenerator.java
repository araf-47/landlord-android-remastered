package com.landlord.android.core.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public final class CsvReportGenerator {

    private CsvReportGenerator() {}

    public static File generateReportCsv(Context context, String reportTitle,
                                         List<String> summaryLines,
                                         List<String> tableHeaders,
                                         List<String[]> tableRows) {
        File reportsDir = new File(context.getCacheDir(), "reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }

        String cleanTitle = reportTitle.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase(Locale.US);
        File csvFile = new File(reportsDir, "report_" + cleanTitle + "_" + System.currentTimeMillis() + ".csv");

        try (FileWriter writer = new FileWriter(csvFile)) {
            // Title
            writer.append("\"").append(reportTitle).append("\"\n\n");

            // Summary metrics
            if (summaryLines != null) {
                for (String line : summaryLines) {
                    writer.append("\"").append(line.replace("\"", "\"\"")).append("\"\n");
                }
                writer.append("\n");
            }

            // Headers
            if (tableHeaders != null && !tableHeaders.isEmpty()) {
                for (int i = 0; i < tableHeaders.size(); i++) {
                    writer.append("\"").append(tableHeaders.get(i).replace("\"", "\"\"")).append("\"");
                    if (i < tableHeaders.size() - 1) writer.append(",");
                }
                writer.append("\n");
            }

            // Rows
            if (tableRows != null) {
                for (String[] row : tableRows) {
                    for (int i = 0; i < row.length; i++) {
                        String cell = row[i] != null ? row[i] : "";
                        writer.append("\"").append(cell.replace("\"", "\"\"")).append("\"");
                        if (i < row.length - 1) writer.append(",");
                    }
                    writer.append("\n");
                }
            }

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return csvFile;
    }

    public static void openOrShareCsv(Context context, File csvFile, String title) {
        if (csvFile == null || !csvFile.exists()) {
            Toast.makeText(context, "Error: Spreadsheet file could not be generated", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", csvFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "text/csv");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception ex) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(shareIntent, "Open or Share " + title));
        }
    }
}
