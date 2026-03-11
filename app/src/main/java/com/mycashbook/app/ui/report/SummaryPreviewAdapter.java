package com.mycashbook.app.ui.report;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mycashbook.app.R;
import com.mycashbook.app.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SummaryPreviewAdapter extends ArrayAdapter<Transaction> {

    private final Context context;
    private final List<Transaction> list;
    private final SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public SummaryPreviewAdapter(Context ctx, List<Transaction> items) {
        super(ctx, R.layout.item_transaction_preview, items);
        this.context = ctx;
        this.list = items;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convert, ViewGroup parent) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_transaction_preview, parent, false);

        Transaction t = list.get(position);

        TextView txtAmount = v.findViewById(R.id.textPreviewAmount);
        TextView txtType = v.findViewById(R.id.textPreviewType);
        TextView txtNote = v.findViewById(R.id.textPreviewNote);
        TextView txtDate = v.findViewById(R.id.textPreviewDate);

        txtAmount.setText("₹" + t.getAmount());
        txtType.setText(t.getType());
        txtNote.setText(t.getNote());
        txtDate.setText(df.format(t.getDate()));

        if (t.getType().equals("DEBIT"))
            txtAmount.setTextColor(context.getColor(R.color.red));
        else
            txtAmount.setTextColor(context.getColor(R.color.green));

        return v;
    }
}
