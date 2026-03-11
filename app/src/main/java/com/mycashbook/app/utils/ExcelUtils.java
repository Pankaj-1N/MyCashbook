package com.mycashbook.app.utils;

import com.mycashbook.app.model.Transaction;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExcelUtils {

    // ----------------------------------------------------------------
    // 1. EXPORT LOGIC (Was missing in your file)
    // ----------------------------------------------------------------

    /**
     * Helper to write transactions to a file.
     */
    public static void exportTransactions(File file, List<Transaction> list) {
        try (FileOutputStream os = new FileOutputStream(file)) {
            writeTransactionsToExcel(os, list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the list of transactions into an Excel sheet.
     */
    public static void writeTransactionsToExcel(OutputStream os, List<Transaction> transactions) throws IOException {
        Workbook workbook = new XSSFWorkbook(); // Create new XLSX workbook
        Sheet sheet = workbook.createSheet("Transactions");

        // --- Header Row ---
        Row headerRow = sheet.createRow(0);
        String[] columns = {"Date", "Amount", "Type", "Note"};

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            // Optional: Make header bold
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        // --- Data Rows ---
        int rowNum = 1;
        for (Transaction t : transactions) {
            Row row = sheet.createRow(rowNum++);

            // Col 0: Date
            Cell dateCell = row.createCell(0);
            if (t.getDate() != null) {
                dateCell.setCellValue(t.getDate().toString());
            } else {
                dateCell.setCellValue("");
            }

            // Col 1: Amount
            row.createCell(1).setCellValue(t.getAmount());

            // Col 2: Type
            row.createCell(2).setCellValue(t.getType());

            // Col 3: Note
            row.createCell(3).setCellValue(t.getNote() != null ? t.getNote() : "");
        }

        // Resize columns to fit content
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(os);
        workbook.close();
    }

    // ----------------------------------------------------------------
    // 2. IMPORT LOGIC (Fixed errors)
    // ----------------------------------------------------------------

    public static List<Transaction> readTransactionsFromExcel(InputStream is) {
        List<Transaction> list = new ArrayList<>();

        try {
            Workbook workbook = null;

            try {
                workbook = new XSSFWorkbook(is);
            } catch (Exception e) {
                try {
                    if (is.markSupported()) is.reset();
                    workbook = new HSSFWorkbook(is);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            }

            if (workbook == null) return null;

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();

            // Skip header row -> start at i = 1
            for (int i = 1; i < rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Transaction t = new Transaction();

                // Column 0 -> Date
                Cell c0 = row.getCell(0);
                if (c0 != null) {
                    // FIX: Use getCellTypeEnum() for POI 3.17 to avoid int vs CellType error
                    if (c0.getCellTypeEnum() == CellType.NUMERIC && DateUtil.isCellDateFormatted(c0)) {
                        // FIX: setDate expects a Date object, not long
                        t.setDate(c0.getDateCellValue());
                    } else {
                        t.setDate(new Date()); // Default to now
                    }
                } else {
                    t.setDate(new Date());
                }

                // Column 1 -> Amount
                Cell c1 = row.getCell(1);
                if (c1 != null) {
                    if (c1.getCellTypeEnum() == CellType.NUMERIC) {
                        t.setAmount(c1.getNumericCellValue());
                    } else {
                        try {
                            t.setAmount(Double.parseDouble(c1.toString()));
                        } catch (NumberFormatException e) {
                            t.setAmount(0);
                        }
                    }
                } else {
                    t.setAmount(0);
                }

                // Column 2 -> Type
                Cell c2 = row.getCell(2);
                t.setType(c2 != null ? c2.toString().trim() : "DEBIT");

                // Column 3 -> Note
                Cell c3 = row.getCell(3);
                // FIX: Mapped to Note (Transaction doesn't have setTitle)
                t.setNote(c3 != null ? c3.toString() : "");

                list.add(t);
            }
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return list;
    }
}
