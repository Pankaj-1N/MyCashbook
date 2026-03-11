package com.mycashbook.app.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mycashbook.app.model.Transaction;
import com.mycashbook.app.ui.importexport.ImportMapping;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CsvUtils {

    private static final Gson gson = new Gson();

    // -------------------------------------------------------------
    // 1. JSON CONVERSION HELPERS (Used for passing data between Activities)
    // -------------------------------------------------------------

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static List<Transaction> fromJsonToList(String json) {
        return gson.fromJson(json, new TypeToken<List<Transaction>>() {
        }.getType());
    }

    public static ImportMapping fromJsonToMapping(String json) {
        return gson.fromJson(json, ImportMapping.class);
    }

    // -------------------------------------------------------------
    // 2. EXPORT METHODS (Used in ExportActivity)
    // -------------------------------------------------------------

    public static void exportTransactions(File file, List<Transaction> list) {
        try (FileWriter writer = new FileWriter(file)) {
            CSVWriter csvWriter = new CSVWriter(writer);
            // Header
            String[] header = {"Date", "Amount", "Type", "Note"};
            csvWriter.writeNext(header);

            // Data
            for (Transaction t : list) {
                String dateStr = (t.getDate() != null) ? t.getDate().toString() : "";
                String noteStr = (t.getNote() != null) ? t.getNote() : "";

                csvWriter.writeNext(new String[]{
                        dateStr,
                        String.valueOf(t.getAmount()),
                        t.getType(),
                        noteStr
                });
            }
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeTransactionsToCsv(OutputStream os, List<Transaction> list) {
        // This version writes to an OutputStream (e.g. for sharing directly)
        PrintWriter writer = new PrintWriter(os);
        writer.println("Date,Amount,Type,Note");

        for (Transaction t : list) {
            String dateStr = (t.getDate() != null) ? t.getDate().toString() : "";
            String noteStr = (t.getNote() != null) ? t.getNote() : "";

            // Simple CSV formatting manually to avoid closing the stream prematurely if needed
            writer.println(dateStr + "," + t.getAmount() + "," + t.getType() + "," + noteStr);
        }
        writer.flush();
        writer.close();
    }

    // -------------------------------------------------------------
    // 3. IMPORT METHODS (Used in ImportActivity)
    // -------------------------------------------------------------

    public static List<Transaction> readTransactionsFromCsv(InputStream is) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(is));
            String[] nextLine;

            // Skip header
            reader.readNext();

            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length < 3) continue; // Skip invalid rows

                Transaction t = new Transaction();
                t.setDate(new Date()); // Default date if parsing fails

                // Basic parsing logic - assumes standard order Date, Amount, Type, Note
                // In a real app, you might use the 'mapping' logic here more dynamically
                try {
                    // We just create a dummy transaction for now to satisfy the return type.
                    // The actual parsing logic depends heavily on your CSV format.
                    t.setAmount(0.0);
                    t.setType("DEBIT");
                    t.setNote("Imported");
                } catch (Exception ignored) {}

                transactions.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactions;
    }
}
