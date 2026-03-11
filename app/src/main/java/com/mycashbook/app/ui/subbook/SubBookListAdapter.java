package com.mycashbook.app.ui.subbook;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mycashbook.app.R;
import com.mycashbook.app.model.SubBook;
import com.mycashbook.app.model.SubBookWithStats;
import com.mycashbook.app.ui.transaction.TransactionDetailsActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SubBookListAdapter extends ListAdapter<SubBookWithStats, SubBookListAdapter.SubBookViewHolder> {

    private static final String TAG = "SubBookListAdapter";

    private final Context context;
    private final OnSubBookClickListener listener;
    private com.mycashbook.app.model.Book currentBook;

    public void updateBook(com.mycashbook.app.model.Book book) {
        this.currentBook = book;
        notifyDataSetChanged();
    }

    // Interface for item click callbacks
    public interface OnSubBookClickListener {
        void onSubBookClick(SubBook subBook);

        void onSubBookEditClick(SubBook subBook);

        void onSubBookDeleteClick(SubBook subBook);
    }

    // Constructor
    public SubBookListAdapter(Context context, OnSubBookClickListener listener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;
    }

    // DiffUtil for efficient list updates
    private static final DiffUtil.ItemCallback<SubBookWithStats> DIFF_CALLBACK = new DiffUtil.ItemCallback<SubBookWithStats>() {
        @Override
        public boolean areItemsTheSame(@NonNull SubBookWithStats oldItem, @NonNull SubBookWithStats newItem) {
            return oldItem.subBook.getId() == newItem.subBook.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SubBookWithStats oldItem, @NonNull SubBookWithStats newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public SubBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_subbook, parent, false);
        return new SubBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubBookViewHolder holder, int position) {
        SubBookWithStats subBookWithStats = getItem(position);
        holder.bind(subBookWithStats, context, currentBook, listener);
    }

    // ============================================================
    // VIEW HOLDER
    // ============================================================

    public static class SubBookViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvSubBookName;
        private final TextView tvBalance;
        private final TextView tvTotalIn;
        private final TextView tvTotalOut;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;
        private final View cardView;

        public SubBookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubBookName = itemView.findViewById(R.id.tvSubBookName);
            tvBalance = itemView.findViewById(R.id.tvBalance);
            tvTotalIn = itemView.findViewById(R.id.tvTotalIn);
            tvTotalOut = itemView.findViewById(R.id.tvTotalOut);
            btnEdit = itemView.findViewById(R.id.btnEditSubBook);
            btnDelete = itemView.findViewById(R.id.btnDeleteSubBook);
            cardView = itemView.findViewById(R.id.subBookCard);
        }

        public void bind(SubBookWithStats item, Context context, com.mycashbook.app.model.Book book,
                OnSubBookClickListener listener) {
            SubBook subBook = item.subBook;

            // Set sub-book name
            tvSubBookName.setText(subBook.getName());

            // Calculate balance from transactions (not stale column)
            double balance = item.getBalance();

            // Set balance with currency formatting
            tvBalance.setText(com.mycashbook.app.utils.CurrencyUtils.formatCurrency(balance, book));

            // Color code balance
            if (balance < 0) {
                tvBalance.setTextColor(context.getColor(R.color.red_500));
            } else if (balance > 0) {
                tvBalance.setTextColor(context.getColor(R.color.green_500));
            } else {
                android.util.TypedValue typedValue = new android.util.TypedValue();
                context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant,
                        typedValue, true);
                tvBalance.setTextColor(typedValue.data);
            }

            // Set Total In/Out
            if (tvTotalIn != null) {
                tvTotalIn.setText(com.mycashbook.app.utils.CurrencyUtils.formatCurrency(item.totalIncome, book));
            }
            if (tvTotalOut != null) {
                tvTotalOut.setText(com.mycashbook.app.utils.CurrencyUtils.formatCurrency(item.totalExpense, book));
            }

            // Main card click - open transactions for this sub-book
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSubBookClick(subBook);
                }
            });

            // Edit button click
            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onSubBookEditClick(subBook);
                    }
                });
            }

            // Delete button click
            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onSubBookDeleteClick(subBook);
                    }
                });
            }
        }

    }
}