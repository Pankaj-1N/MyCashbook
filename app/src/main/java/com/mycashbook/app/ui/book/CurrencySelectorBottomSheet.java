package com.mycashbook.app.ui.book;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mycashbook.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CurrencySelectorBottomSheet extends BottomSheetDialogFragment {

    public interface OnCurrencySelectedListener {
        void onCurrencySelected(String code, String symbol, String name);
    }

    private OnCurrencySelectedListener listener;
    private List<CurrencyItem> allCurrencies = new ArrayList<>();
    private List<CurrencyItem> filteredCurrencies = new ArrayList<>();
    private CurrencyAdapter adapter;

    public void setListener(OnCurrencySelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_currency_selector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadCurrencies();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerCurrencies);
        EditText editSearch = view.findViewById(R.id.editSearch);
        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        adapter = new CurrencyAdapter(filteredCurrencies, item -> {
            if (listener != null) {
                listener.onCurrencySelected(item.code, item.symbol, item.name);
            }
            dismiss();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadCurrencies() {
        Set<Currency> currencies = Currency.getAvailableCurrencies();
        for (Currency c : currencies) {
            allCurrencies.add(new CurrencyItem(
                    c.getCurrencyCode(),
                    c.getSymbol(),
                    c.getDisplayName()));
        }

        // Sort by Code
        Collections.sort(allCurrencies, (o1, o2) -> o1.code.compareTo(o2.code));

        filteredCurrencies.addAll(allCurrencies);
    }

    private void filter(String query) {
        filteredCurrencies.clear();
        if (query.isEmpty()) {
            filteredCurrencies.addAll(allCurrencies);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (CurrencyItem item : allCurrencies) {
                if (item.code.toLowerCase().contains(lowerQuery) ||
                        item.name.toLowerCase().contains(lowerQuery)) {
                    filteredCurrencies.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private static class CurrencyItem {
        String code;
        String symbol;
        String name;

        CurrencyItem(String code, String symbol, String name) {
            this.code = code;
            this.symbol = symbol;
            this.name = name;
        }
    }

    private static class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.ViewHolder> {
        private final List<CurrencyItem> items;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(CurrencyItem item);
        }

        CurrencyAdapter(List<CurrencyItem> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currency, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CurrencyItem item = items.get(position);
            holder.textSymbol.setText(item.symbol);
            holder.textCode.setText(item.code);
            holder.textName.setText(item.name);
            holder.textFlag.setText(getFlagEmoji(item.code));
            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        /**
         * Convert currency code to country flag emoji
         * Uses first 2 chars of currency code as country code (works for most
         * currencies)
         */
        private String getFlagEmoji(String currencyCode) {
            if (currencyCode == null || currencyCode.length() < 2) {
                return "🏳️";
            }
            // Map some common currencies that don't match country codes
            String countryCode = currencyCode.substring(0, 2);
            switch (currencyCode) {
                case "EUR":
                    countryCode = "EU";
                    break;
                case "XOF":
                    countryCode = "SN";
                    break; // CFA Franc
                case "XAF":
                    countryCode = "CM";
                    break; // Central African CFA
                case "XCD":
                    countryCode = "AG";
                    break; // East Caribbean Dollar
            }
            try {
                // Convert country code to flag emoji using regional indicator symbols
                int firstChar = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6;
                int secondChar = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6;
                return new String(Character.toChars(firstChar)) + new String(Character.toChars(secondChar));
            } catch (Exception e) {
                return "🏳️";
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textFlag, textSymbol, textCode, textName;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                textFlag = itemView.findViewById(R.id.textFlag);
                textSymbol = itemView.findViewById(R.id.textSymbol);
                textCode = itemView.findViewById(R.id.textCode);
                textName = itemView.findViewById(R.id.textName);
            }
        }
    }
}
