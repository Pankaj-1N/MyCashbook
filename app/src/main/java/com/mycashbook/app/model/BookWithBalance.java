package com.mycashbook.app.model;

import androidx.room.Embedded;
import androidx.room.ColumnInfo;

public class BookWithBalance {
    @Embedded
    public Book book;

    @ColumnInfo(name = "totalBalance")
    public Double totalBalance;
}
