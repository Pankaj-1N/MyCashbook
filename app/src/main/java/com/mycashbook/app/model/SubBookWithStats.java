package com.mycashbook.app.model;

import androidx.room.Embedded;

import java.util.Objects;

public class SubBookWithStats {

    @Embedded
    public SubBook subBook;

    public double totalIncome;
    public double totalExpense;

    // Default constructor is needed for Room
    public SubBookWithStats() {
    }

    // Calculate balance from actual transaction totals (not stale balance column)
    public double getBalance() {
        return totalIncome - totalExpense;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SubBookWithStats that = (SubBookWithStats) o;
        return Double.compare(that.totalIncome, totalIncome) == 0 &&
                Double.compare(that.totalExpense, totalExpense) == 0 &&
                Objects.equals(subBook, that.subBook);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subBook, totalIncome, totalExpense);
    }
}
