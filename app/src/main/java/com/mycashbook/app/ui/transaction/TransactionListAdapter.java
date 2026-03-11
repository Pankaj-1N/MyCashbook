package com.mycashbook.app.ui.transaction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mycashbook.app.R;
import com.mycashbook.app.model.Transaction;
import com.mycashbook.app.utils.CurrencyUtils;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionListAdapter extends ListAdapter<Transaction, TransactionListAdapter.TransactionViewHolder> {

    private static final String TAG = "TransactionListAdapter";

    private final Context context;
    private final OnTransactionClickListener listener;
    private com.mycashbook.app.model.Book currentBook;

    public void updateBook(com.mycashbook.app.model.Book book) {
        this.currentBook = book;
        notifyDataSetChanged();
    }

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);

        void onTransactionEditClick(Transaction transaction);

        void onTransactionDeleteClick(Transaction transaction);
    }

    public TransactionListAdapter(Context context, OnTransactionClickListener listener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK = new DiffUtil.ItemCallback<Transaction>() {
        @Override
        public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getAmount() == newItem.getAmount() &&
                    oldItem.getType().equals(newItem.getType()) &&
                    java.util.Objects.equals(oldItem.getNote(), newItem.getNote()) &&
                    java.util.Objects.equals(oldItem.getContact(), newItem.getContact()) &&
                    java.util.Objects.equals(oldItem.getPaymentMethod(), newItem.getPaymentMethod()) &&
                    java.util.Objects.equals(oldItem.getPaymentApp(), newItem.getPaymentApp()) &&
                    java.util.Objects.equals(oldItem.getDate(), newItem.getDate());
        }
    };

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = getItem(position);
        holder.bind(transaction, currentBook, listener);
        setAnimation(holder.itemView, position);
    }

    private int lastPosition = -1;

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_item_load);
            animation.setStartOffset(position * 30L);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAmount;
        private final TextView tvDescription;
        private final TextView tvContact;
        private final TextView tvPayment;
        private final TextView tvDateDay;
        private final android.widget.ImageView imgIcon;
        private final android.widget.ImageView btnDelete;
        private final View cardView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.textAmount);
            tvDescription = itemView.findViewById(R.id.textDescription);
            tvContact = itemView.findViewById(R.id.textContact);
            tvPayment = itemView.findViewById(R.id.textPayment);
            tvDateDay = itemView.findViewById(R.id.textDateDay);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            btnDelete = itemView.findViewById(R.id.btnDeleteTransaction);
            cardView = itemView.findViewById(R.id.transactionCard);
        }

        public void bind(Transaction transaction, com.mycashbook.app.model.Book book,
                OnTransactionClickListener listener) {

            boolean isCredit = "CREDIT".equalsIgnoreCase(transaction.getType())
                    || "IN".equalsIgnoreCase(transaction.getType());

            if (isCredit) {
                tvAmount.setTextColor(Color.parseColor("#00D09C"));
                tvAmount.setText("+ " + CurrencyUtils.formatCurrency(transaction.getAmount(), book));
                cardView.setBackgroundResource(R.drawable.bg_transaction_card_income);
                imgIcon.setBackgroundResource(R.drawable.bg_icon_green);
                imgIcon.setImageResource(R.drawable.ic_trending_up);
                imgIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#00D09C")));
            } else {
                tvAmount.setTextColor(Color.parseColor("#FF5252"));
                tvAmount.setText("- " + CurrencyUtils.formatCurrency(transaction.getAmount(), book));
                cardView.setBackgroundResource(R.drawable.bg_transaction_card_expense);
                imgIcon.setBackgroundResource(R.drawable.bg_icon_red);
                imgIcon.setImageResource(R.drawable.ic_trending_down);
                imgIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF5252")));
            }

            // Note / Description
            String note = transaction.getNote();
            if (note != null && !note.isEmpty()) {
                tvDescription.setText(note);
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Contact
            String contact = transaction.getContact();
            if (contact != null && !contact.isEmpty()) {
                tvContact.setText(contact);
                tvContact.setVisibility(View.VISIBLE);
            } else {
                tvContact.setVisibility(View.GONE);
            }

            // Payment Detail
            String payDetail = (transaction.getPaymentApp() != null && !transaction.getPaymentApp().isEmpty())
                    ? transaction.getPaymentApp()
                    : transaction.getPaymentMethod();

            if (payDetail != null && !payDetail.isEmpty() && !"Unspecified".equalsIgnoreCase(payDetail)) {
                tvPayment.setText(payDetail);
                tvPayment.setVisibility(View.VISIBLE);
            } else {
                tvPayment.setVisibility(View.GONE);
            }

            // Date & Time
            if (transaction.getDate() != null) {
                SimpleDateFormat fullFormat = new SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault());
                tvDateDay.setText(fullFormat.format(transaction.getDate()));
            } else {
                tvDateDay.setText("-");
            }

            // Click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTransactionClick(transaction);
                }
            });

            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onTransactionDeleteClick(transaction);
                    }
                });
            }
        }
    }
}