package com.mycashbook.app.model;

public class ImportMapping {

    private int dateColumn;
    private int amountColumn;
    private int typeColumn;
    private int noteColumn;

    public ImportMapping(int dateColumn, int amountColumn, int typeColumn, int noteColumn) {
        this.dateColumn = dateColumn;
        this.amountColumn = amountColumn;
        this.typeColumn = typeColumn;
        this.noteColumn = noteColumn;
    }

    public int getDateColumn() {
        return dateColumn;
    }

    public int getAmountColumn() {
        return amountColumn;
    }

    public int getTypeColumn() {
        return typeColumn;
    }

    public int getNoteColumn() {
        return noteColumn;
    }
}
