package com.mycashbook.app.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;
import com.mycashbook.app.R;
import com.mycashbook.app.model.Transaction;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportUtils {

    private static final String TAG = "ExportUtils";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

    public static File exportToCSV(Context context, List<Transaction> transactions, String subBookName) {
        String fileName = "Transactions_" + subBookName.replaceAll("\\s+", "_") + "_" + System.currentTimeMillis()
                + ".csv";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            // Header
            String[] header = { "ID", "Date", "Type", "Amount", "Contact", "Payment Method", "App/Details", "Note" };
            writer.writeNext(header);

            for (Transaction t : transactions) {
                String[] data = {
                        String.valueOf(t.getId()),
                        t.getDate() != null ? dateFormat.format(t.getDate()) : "-",
                        t.getType(),
                        String.valueOf(t.getAmount()),
                        t.getContact() != null ? t.getContact() : "",
                        t.getPaymentMethod() != null ? t.getPaymentMethod() : "",
                        t.getPaymentApp() != null ? t.getPaymentApp() : "",
                        t.getNote() != null ? t.getNote() : ""
                };
                writer.writeNext(data);
            }
            return file;
        } catch (IOException e) {
            LogUtils.e(TAG, "CSV Export failed", e);
            return null;
        }
    }

    public static File exportToPDF(Context context, List<Transaction> transactions, String subBookName,
            String dateRange, String currency, String userName, boolean isWhiteLabel) {
        String fileName = "Transactions_" + subBookName.replaceAll("\\s+", "_") + "_" + System.currentTimeMillis()
                + ".pdf";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

        PdfDocument document = new PdfDocument();

        // Sort transactions by date (Oldest to Newest)
        Collections.sort(transactions, (t1, t2) -> {
            if (t1.getDate() == null || t2.getDate() == null)
                return 0;
            return t1.getDate().compareTo(t2.getDate());
        });

        // Setup Paints
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(10f);
        textPaint.setColor(android.graphics.Color.parseColor("#1E293B"));

        Paint titlePaint = new Paint();
        titlePaint.setAntiAlias(true);
        titlePaint.setColor(android.graphics.Color.parseColor("#1E293B"));
        titlePaint.setTextSize(22f);
        titlePaint.setFakeBoldText(true);

        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(android.graphics.Color.parseColor("#4F46E5")); // Indigo 600

        Paint headerTextPaint = new Paint();
        headerTextPaint.setAntiAlias(true);
        headerTextPaint.setColor(android.graphics.Color.WHITE);
        headerTextPaint.setTextSize(10f);
        headerTextPaint.setFakeBoldText(true);

        Paint rowShadePaint = new Paint();
        rowShadePaint.setColor(android.graphics.Color.parseColor("#F8FAFC")); // Slate 50

        Paint linePaint = new Paint();
        linePaint.setColor(android.graphics.Color.parseColor("#E2E8F0")); // Slate 200
        linePaint.setStrokeWidth(0.5f);

        // Page setup
        int pageWidth = 595; // A4 width in points
        int pageHeight = 842;
        int margin = 40;
        int tableWidth = pageWidth - (2 * margin);

        // Load Logo for Header and Watermark
        Bitmap logoBitmap = null;
        Drawable logoDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_wallet_logo);
        if (logoDrawable != null) {
            logoBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            Canvas logoCanvas = new Canvas(logoBitmap);
            logoDrawable.setBounds(0, 0, logoCanvas.getWidth(), logoCanvas.getHeight());
            logoDrawable.draw(logoCanvas);
        }

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int yOffset = 50;

        // --- HEADER ---
        if (logoBitmap != null) {
            Rect destRect = new Rect(margin, yOffset - 15, margin + 40, yOffset + 25);
            canvas.drawBitmap(logoBitmap, null, destRect, paint);
        }
        canvas.drawText("MyCashBook", margin + 50, yOffset + 5, titlePaint);

        Paint reportTitlePaint = new Paint(titlePaint);
        reportTitlePaint.setTextSize(14f);
        reportTitlePaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Transaction Report", pageWidth - margin, yOffset + 5, reportTitlePaint);

        yOffset += 45;

        // --- SUBTITLE & METADATA ---
        paint.setTextSize(10f);
        paint.setColor(android.graphics.Color.parseColor("#64748B"));

        canvas.drawText("Book Name: " + subBookName, margin, yOffset, paint);
        canvas.drawText(
                "Generated On: " + new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date()),
                pageWidth - margin - 180, yOffset, paint);

        yOffset += 18;
        canvas.drawText("Date Range: " + (dateRange != null ? dateRange : "All Time"), margin, yOffset, paint);
        canvas.drawText("Currency: " + (currency != null ? currency : "INR"), pageWidth - margin - 180, yOffset, paint);

        if (userName != null && !userName.isEmpty()) {
            yOffset += 18;
            canvas.drawText("User Name: " + userName, margin, yOffset, paint);
        }

        yOffset += 30;

        // --- COLUMN CONFIG (7 Columns) ---
        // 1. Date, 2. Type, 3. Payment, 4. Description, 5. In, 6. Out, 7. Balance
        int colWidthDate = 65;
        int colWidthType = 60;
        int colWidthPay = 75;
        int colWidthIn = 75;
        int colWidthOut = 75;
        int colWidthBal = 75;
        int colWidthDesc = tableWidth
                - (colWidthDate + colWidthType + colWidthPay + colWidthIn + colWidthOut + colWidthBal);

        int[] colStarts = new int[7];
        colStarts[0] = margin;
        colStarts[1] = colStarts[0] + colWidthDate;
        colStarts[2] = colStarts[1] + colWidthType;
        colStarts[3] = colStarts[2] + colWidthPay;
        colStarts[4] = colStarts[3] + colWidthDesc;
        colStarts[5] = colStarts[4] + colWidthIn;
        colStarts[6] = colStarts[5] + colWidthOut;

        // Draw Table Header
        canvas.drawRect(margin, yOffset - 18, pageWidth - margin, yOffset + 12, headerBgPaint);

        String[] headers = { "Date", "Type", "Payment", "Description", "Cash In", "Cash Out", "Balance" };
        headerTextPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(headers[0], colStarts[0] + 5, yOffset, headerTextPaint);
        canvas.drawText(headers[1], colStarts[1] + 5, yOffset, headerTextPaint);
        canvas.drawText(headers[2], colStarts[2] + 5, yOffset, headerTextPaint);
        canvas.drawText(headers[3], colStarts[3] + 5, yOffset, headerTextPaint);

        headerTextPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(headers[4], colStarts[5] - 5, yOffset, headerTextPaint);
        canvas.drawText(headers[5], colStarts[6] - 5, yOffset, headerTextPaint);
        canvas.drawText(headers[6], pageWidth - margin - 5, yOffset, headerTextPaint);

        yOffset += 12;

        // --- DRAW WATERMARK ---
        if (logoBitmap != null) {
            drawWatermark(canvas, logoBitmap, pageWidth, pageHeight);
        }

        double totalIn = 0;
        double totalOut = 0;
        double runningBalance = 0;
        int rowCount = 0;

        for (Transaction t : transactions) {
            if (yOffset > pageHeight - 150) {
                drawFooter(canvas, pageWidth, pageHeight, document.getPages().size());
                document.finishPage(page);

                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.getPages().size() + 1)
                        .create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                if (logoBitmap != null)
                    drawWatermark(canvas, logoBitmap, pageWidth, pageHeight);
                yOffset = 50;

                // Redraw Header on new page
                canvas.drawRect(margin, yOffset - 18, pageWidth - margin, yOffset + 12, headerBgPaint);
                headerTextPaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(headers[0], colStarts[0] + 5, yOffset, headerTextPaint);
                canvas.drawText(headers[1], colStarts[1] + 5, yOffset, headerTextPaint);
                canvas.drawText(headers[2], colStarts[2] + 5, yOffset, headerTextPaint);
                canvas.drawText(headers[3], colStarts[3] + 5, yOffset, headerTextPaint);
                headerTextPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(headers[4], colStarts[5] - 5, yOffset, headerTextPaint);
                canvas.drawText(headers[5], colStarts[6] - 5, yOffset, headerTextPaint);
                canvas.drawText(headers[6], pageWidth - margin - 5, yOffset, headerTextPaint);
                yOffset += 12;
            }

            // Alternate Row Shading
            if (rowCount % 2 != 0) {
                canvas.drawRect(margin, yOffset, pageWidth - margin, yOffset + 25, rowShadePaint); // Initial estimate
            }

            // Data Preparation
            String dateStr = t.getDate() != null
                    ? new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(t.getDate())
                    : "-";
            String typeStr = t.getType() != null ? t.getType().toUpperCase() : "-";
            String payStr = (t.getPaymentApp() != null && !t.getPaymentApp().isEmpty()) ? t.getPaymentApp()
                    : (t.getPaymentMethod() != null ? t.getPaymentMethod() : "-");
            String descStr = t.getNote() != null ? t.getNote().replaceAll("Contact: [^\\n]*\\n?", "")
                    .replaceAll("Via: [^\\n]*\\n?", "").replaceAll("Note: ", "").trim() : "-";
            if (descStr.isEmpty())
                descStr = "-";

            boolean isCredit = "CREDIT".equalsIgnoreCase(t.getType());
            double amount = t.getAmount();
            if (isCredit) {
                totalIn += amount;
                runningBalance += amount;
            } else {
                totalOut += amount;
                runningBalance -= amount;
            }

            // Row Painting
            yOffset += 18;
            textPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(dateStr, colStarts[0] + 5, yOffset, textPaint);
            canvas.drawText(typeStr, colStarts[1] + 5, yOffset, textPaint);

            // Payment (Truncate if needed)
            if (payStr.length() > 12)
                payStr = payStr.substring(0, 10) + "..";
            canvas.drawText(payStr, colStarts[2] + 5, yOffset, textPaint);

            // Description with wrapping
            StaticLayout sl = new StaticLayout(descStr, textPaint, colWidthDesc - 10, Layout.Alignment.ALIGN_NORMAL,
                    1.0f, 0.0f, false);
            canvas.save();
            canvas.translate(colStarts[3] + 5, yOffset - 10);
            sl.draw(canvas);
            canvas.restore();

            // Amounts
            textPaint.setTextAlign(Paint.Align.RIGHT);
            String amtStr = String.format("%.2f", amount);
            if (isCredit) {
                textPaint.setColor(android.graphics.Color.parseColor("#059669")); // Green
                canvas.drawText(amtStr, colStarts[5] - 5, yOffset, textPaint);
                textPaint.setColor(android.graphics.Color.parseColor("#1E293B")); // Reset
            } else {
                textPaint.setColor(android.graphics.Color.parseColor("#DC2626")); // Red
                canvas.drawText(amtStr, colStarts[6] - 5, yOffset, textPaint);
                textPaint.setColor(android.graphics.Color.parseColor("#1E293B")); // Reset
            }

            // Running Balance
            canvas.drawText(String.format("%.2f", runningBalance), pageWidth - margin - 5, yOffset, textPaint);

            int rowHeight = Math.max(25, sl.getHeight() + 5);
            yOffset += (rowHeight - 15); // Adjust based on wrapped text

            canvas.drawLine(margin, yOffset, pageWidth - margin, yOffset, linePaint);
            rowCount++;
        }

        // --- SUMMARY SECTION ---
        yOffset += 40;
        if (yOffset > pageHeight - 100) {
            drawFooter(canvas, pageWidth, pageHeight, document.getPages().size());
            document.finishPage(page);
            pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.getPages().size() + 1).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            if (logoBitmap != null)
                drawWatermark(canvas, logoBitmap, pageWidth, pageHeight);
            yOffset = 100;
        }

        Paint summaryBgPaint = new Paint();
        summaryBgPaint.setColor(android.graphics.Color.parseColor("#F1F5F9"));
        canvas.drawRoundRect(pageWidth - margin - 220, yOffset - 20, pageWidth - margin, yOffset + 70, 8, 8,
                summaryBgPaint);

        Paint summaryLabelPaint = new Paint();
        summaryLabelPaint.setTextSize(11f);
        summaryLabelPaint.setFakeBoldText(true);
        summaryLabelPaint.setColor(android.graphics.Color.parseColor("#475569"));

        canvas.drawText("Total Cash In:", pageWidth - margin - 210, yOffset, summaryLabelPaint);
        Paint summaryValPaint = new Paint(summaryLabelPaint);
        summaryValPaint.setTextAlign(Paint.Align.RIGHT);
        summaryValPaint.setColor(android.graphics.Color.parseColor("#059669"));
        canvas.drawText(String.format("%.2f", totalIn), pageWidth - margin - 10, yOffset, summaryValPaint);

        yOffset += 22;
        canvas.drawText("Total Cash Out:", pageWidth - margin - 210, yOffset, summaryLabelPaint);
        summaryValPaint.setColor(android.graphics.Color.parseColor("#DC2626"));
        canvas.drawText(String.format("%.2f", totalOut), pageWidth - margin - 10, yOffset, summaryValPaint);

        yOffset += 28;
        Paint netBalancePaint = new Paint(summaryLabelPaint);
        netBalancePaint.setTextSize(13f);
        canvas.drawText("Net Balance:", pageWidth - margin - 210, yOffset, netBalancePaint);

        double net = totalIn - totalOut;
        summaryValPaint.setTextSize(13f);
        summaryValPaint.setColor(
                net >= 0 ? android.graphics.Color.parseColor("#059669") : android.graphics.Color.parseColor("#DC2626"));
        canvas.drawText(String.format("%.2f %s", Math.abs(net), net >= 0 ? "Cr" : "Dr"), pageWidth - margin - 10,
                yOffset, summaryValPaint);

        drawFooter(canvas, pageWidth, pageHeight, document.getPages().size());
        document.finishPage(page);

        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            return file;
        } catch (IOException e) {
            LogUtils.e(TAG, "PDF Export failed", e);
            document.close();
            return null;
        }
    }

    private static void drawWatermark(Canvas canvas, Bitmap logo, int pageWidth, int pageHeight) {
        Paint watermarkPaint = new Paint();
        watermarkPaint.setAlpha(15); // ~5-10% opacity (0-255)

        // Center the watermark
        int logoSize = 300;
        Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, logoSize, logoSize, true);
        canvas.drawBitmap(scaledLogo, (pageWidth - logoSize) / 2f, (pageHeight - logoSize) / 2f, watermarkPaint);
    }

    private static void drawFooter(Canvas canvas, int pageWidth, int pageHeight, int pageIndex) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(9f);
        paint.setColor(android.graphics.Color.parseColor("#94A3B8"));

        canvas.drawText("MyCashBook - Professional Financial Report", 40, pageHeight - 35, paint);
        canvas.drawText("Page " + (pageIndex + 1), pageWidth - 80, pageHeight - 35, paint);
        canvas.drawLine(40, pageHeight - 50, pageWidth - 40, pageHeight - 50, paint);
    }

    public static File exportToExcel(Context context, List<Transaction> transactions, String subBookName,
            boolean isWhiteLabel) {
        String fileName = "Transactions_" + subBookName.replaceAll("\\s+", "_") + "_" + System.currentTimeMillis()
                + ".xlsx";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = { "Date", "Type", "Amount", "Contact", "Payment Method", "App/Details", "Note" };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data
            int rowNum = 1;
            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(t.getDate() != null ? dateFormat.format(t.getDate()) : "-");
                row.createCell(1).setCellValue(t.getType());
                row.createCell(2).setCellValue(t.getAmount());
                row.createCell(3).setCellValue(t.getContact() != null ? t.getContact() : "");
                row.createCell(4).setCellValue(t.getPaymentMethod() != null ? t.getPaymentMethod() : "");
                row.createCell(5).setCellValue(t.getPaymentApp() != null ? t.getPaymentApp() : "");
                row.createCell(6).setCellValue(t.getNote() != null ? t.getNote() : "");
            }

            // Watermark for free tier
            if (!isWhiteLabel) {
                Row footerRow = sheet.createRow(rowNum + 1);
                footerRow.createCell(0).setCellValue("Generated by MyCashBook • mycashbook.app");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            return file;
        } catch (IOException e) {
            LogUtils.e(TAG, "Excel Export failed", e);
            return null;
        }
    }

    public static void shareFile(Context context, File file, String type) {
        if (file == null || !file.exists())
            return;

        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(type);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Share Transaction Report"));
    }
}
