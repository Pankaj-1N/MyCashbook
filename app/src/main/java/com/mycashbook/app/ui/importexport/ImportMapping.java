package com.mycashbook.app.ui.importexport;

import java.io.Serializable;

public class ImportMapping implements Serializable {

    // These fields represent which column index in the CSV corresponds to which data
    // e.g., 0 for first column, 1 for second, etc.
    private int dateIndex = 0;
    private int amountIndex = 1;
    private int typeIndex = 2;
    private int noteIndex = 3;

    private String dateFormat = "yyyy-MM-dd"; // To help parse dates
    private boolean hasHeader = true;

    // Default constructor is required for Gson
    public ImportMapping() {
    }

    public int getDateIndex() {
        return dateIndex;
    }

    public void setDateIndex(int dateIndex) {
        this.dateIndex = dateIndex;
    }

    public int getAmountIndex() {
        return amountIndex;
    }

    public void setAmountIndex(int amountIndex) {
        this.amountIndex = amountIndex;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public void setTypeIndex(int typeIndex) {
        this.typeIndex = typeIndex;
    }

    public int getNoteIndex() {
        return noteIndex;
    }

    public void setNoteIndex(int noteIndex) {
        this.noteIndex = noteIndex;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }
}
