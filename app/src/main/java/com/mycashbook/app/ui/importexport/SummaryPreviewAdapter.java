package com.mycashbook.app.ui.importexport;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mycashbook.app.R;
import com.mycashbook.app.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SummaryPreviewAdapter extends ArrayAdapter<Transaction> {

    private final Context context;
    private final List<Transaction> transactionList;
    private final SimpleDateFormat dateFormat;

    public SummaryPreviewAdapter(@NonNull Context context, List<Transaction> list) {
        super(context, 0, list);
        this.context = context;
        this.transactionList = list;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
            holder = new ViewHolder();

            holder.textDesc = convertView.findViewById(R.id.textDescription);
            holder.textDateDay = convertView.findViewById(R.id.textDateDay);
            // holder.textDateMonth = convertView.findViewById(R.id.textDateMonth); // Not
            // used in new layout logic
            holder.textAmount = convertView.findViewById(R.id.textAmount);
            holder.imgIcon = convertView.findViewById(R.id.imgIcon); // Added

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Transaction transaction = getItem(position);

        if (transaction != null) {
            // Handle Description
            String remarks = transaction.getDescription();
            if (remarks == null || remarks.isEmpty()) {
                holder.textDesc.setText("No Description");
            } else {
                holder.textDesc.setText(remarks);
            }

            // Handle Date (Combined)
            if (transaction.getDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
                holder.textDateDay.setText(dateFormat.format(transaction.getDate()));
            } else {
                holder.textDateDay.setText("-");
            }

            // Handle Amount & Type
            String amountStr = String.valueOf(transaction.getAmount());
            String type = transaction.getType();

            // Check if type is Income/Credit
            boolean isIncome = "1".equals(type) || "IN".equalsIgnoreCase(type) || "CREDIT".equalsIgnoreCase(type);

            if (isIncome) {
                // CASH IN (Green)
                holder.textAmount.setText("+ " + amountStr);
                holder.textAmount.setTextColor(Color.parseColor("#00D09C"));
                if (holder.imgIcon != null) {
                    holder.imgIcon.setBackgroundResource(R.drawable.bg_icon_green);
                    holder.imgIcon.setImageResource(R.drawable.ic_trending_up);
                    holder.imgIcon.setColorFilter(Color.parseColor("#00D09C"));
                }
            } else {
                // CASH OUT (Red)
                holder.textAmount.setText("- " + amountStr);
                holder.textAmount.setTextColor(Color.parseColor("#FF5252"));
                if (holder.imgIcon != null) {
                    holder.imgIcon.setBackgroundResource(R.drawable.bg_icon_red);
                    holder.imgIcon.setImageResource(R.drawable.ic_trending_down);
                    holder.imgIcon.setColorFilter(Color.parseColor("#FF5252"));
                }
            }
        }

        return convertView;
    }

    static class ViewHolder {
        TextView textDesc;
        TextView textDateDay;
        // TextView textDateMonth;
        TextView textAmount;
        android.widget.ImageView imgIcon;
    }
}
