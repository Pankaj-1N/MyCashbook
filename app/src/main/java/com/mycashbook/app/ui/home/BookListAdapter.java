package com.mycashbook.app.ui.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mycashbook.app.R;
import com.mycashbook.app.model.Book;
import com.mycashbook.app.model.BookWithBalance;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.util.TypedValue;

public class BookListAdapter extends ListAdapter<BookWithBalance, BookListAdapter.BookViewHolder> {

    private static final String TAG = "BookListAdapter";
    private final Context context;
    private final OnBookClickListener clickListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());

    public interface OnBookClickListener {
        void onBookClick(Book book);

        void onBookEdit(Book book);

        void onBookDelete(Book book);
    }

    public BookListAdapter(Context context, OnBookClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.clickListener = clickListener;
        Log.d(TAG, "BookListAdapter initialized");
    }

    private static final DiffUtil.ItemCallback<BookWithBalance> DIFF_CALLBACK = new DiffUtil.ItemCallback<BookWithBalance>() {
        @Override
        public boolean areItemsTheSame(@NonNull BookWithBalance oldItem, @NonNull BookWithBalance newItem) {
            return oldItem.book.getId() == newItem.book.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull BookWithBalance oldItem, @NonNull BookWithBalance newItem) {
            return oldItem.book.getName().equals(newItem.book.getName())
                    && safeEquals(oldItem.book.getDescription(), newItem.book.getDescription())
                    && safeEquals(oldItem.totalBalance, newItem.totalBalance)
                    && safeDate(oldItem.book.getCreatedAt(), newItem.book.getCreatedAt())
                    && safeDate(oldItem.book.getUpdatedAt(), newItem.book.getUpdatedAt())
                    && oldItem.book.isLocked() == newItem.book.isLocked();
        }

        private boolean safeEquals(Object a, Object b) {
            if (a == null && b == null)
                return true;
            if (a != null)
                return a.equals(b);
            return false;
        }

        private boolean safeDate(Object a, Object b) {
            if (a == null && b == null)
                return true;
            if (a != null)
                return a.equals(b);
            return false;
        }
    };

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        BookWithBalance item = getItem(position);
        if (item != null) {
            holder.bind(item);
        }
    }

    class BookViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iconBook;
        private final ImageView iconLock;
        private final TextView textBookName;
        private final TextView textBookDescription;
        private final TextView textBookDate;
        private final TextView textBookAmount;
        private final TextView textCategory;
        private final ImageButton buttonDelete;
        private final ImageButton buttonEdit;
        private final CardView bookCard;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);

            iconBook = itemView.findViewById(R.id.iconBook);
            iconLock = itemView.findViewById(R.id.iconLock);
            textBookName = itemView.findViewById(R.id.textBookName);
            textBookDescription = itemView.findViewById(R.id.textBookDescription);
            textBookDate = itemView.findViewById(R.id.textBookDate);
            textBookAmount = itemView.findViewById(R.id.textBookAmount);
            textCategory = itemView.findViewById(R.id.textCategory);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            bookCard = itemView.findViewById(R.id.bookCard);
        }

        public void bind(BookWithBalance item) {
            if (item == null || item.book == null) {
                return;
            }

            Book book = item.book;

            // Set book name
            if (textBookName != null) {
                textBookName.setText(book.getName());
            }

            // Set lock icon visibility
            if (iconLock != null) {
                iconLock.setVisibility(book.isLocked() ? View.VISIBLE : View.GONE);
            }

            // Set description
            if (textBookDescription != null) {
                String desc = book.getDescription();
                if (desc == null || desc.isEmpty()) {
                    textBookDescription.setVisibility(View.GONE);
                } else {
                    textBookDescription.setVisibility(View.VISIBLE);
                    textBookDescription.setText(desc);
                }
            }

            // Set amount (Total Balance)
            if (textBookAmount != null) {
                double balance = item.totalBalance != null ? item.totalBalance : 0.0;
                textBookAmount.setText(com.mycashbook.app.utils.CurrencyUtils.formatCurrency(balance, book));

                // Optional: Color coding
                if (balance > 0) {
                    textBookAmount
                            .setTextColor(context.getResources().getColor(R.color.income_green, context.getTheme()));
                } else if (balance < 0) {
                    textBookAmount
                            .setTextColor(context.getResources().getColor(R.color.expense_red, context.getTheme()));
                } else {
                    TypedValue typedValue = new TypedValue();
                    context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue,
                            true);
                    textBookAmount.setTextColor(typedValue.data);
                }
            }

            // Set category if available
            if (textCategory != null) {
                textCategory.setText("BOOK");
            }

            // Set date
            if (textBookDate != null) {
                if (book.getCreatedAt() != null) {
                    textBookDate.setText("Created: " + dateFormat.format(book.getCreatedAt()));
                } else {
                    textBookDate.setText("Created: -");
                }
            }

            // Card click - open book
            if (bookCard != null) {
                bookCard.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onBookClick(book);
                    }
                });
            }

            // Delete button click
            if (buttonDelete != null) {
                buttonDelete.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onBookDelete(book);
                    }
                });
            }

            // Edit button click
            if (buttonEdit != null) {
                buttonEdit.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onBookEdit(book);
                    }
                });
            }
        }
    }
}