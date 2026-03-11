package com.mycashbook.app.ui.transaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mycashbook.app.R;
import com.mycashbook.app.model.PaymentCategory;
import com.mycashbook.app.model.PaymentOption;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CATEGORY = 0;
    private static final int TYPE_OPTION = 1;

    private List<Object> displayList = new ArrayList<>(); // Contains both categories and options
    private final List<PaymentCategory> allCategories;
    private final OnPaymentMethodSelectedListener listener;

    public interface OnPaymentMethodSelectedListener {
        void onMethodSelected(PaymentOption option);
    }

    public PaymentMethodAdapter(List<PaymentCategory> categories, OnPaymentMethodSelectedListener listener) {
        this.allCategories = categories;
        this.listener = listener;
        updateDisplayList();
    }

    private void updateDisplayList() {
        displayList.clear();
        for (PaymentCategory cat : allCategories) {
            displayList.add(cat);
            if (cat.isExpanded()) {
                displayList.addAll(cat.getOptions());
            }
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query.isEmpty()) {
            updateDisplayList();
            return;
        }

        displayList.clear();
        String lowerQuery = query.toLowerCase();
        for (PaymentCategory cat : allCategories) {
            // If category matches, show it (collapsed? maybe expanded if options match)
            // Strategy: Show matching options directly, or category if name matches

            boolean catMatches = cat.getName().toLowerCase().contains(lowerQuery);
            if (catMatches) {
                displayList.add(cat);
                // If category matches, maybe show all options? Let's show all for now
                displayList.addAll(cat.getOptions());
            } else {
                List<PaymentOption> matchingOptions = new ArrayList<>();
                for (PaymentOption opt : cat.getOptions()) {
                    if (opt.getName().toLowerCase().contains(lowerQuery)) {
                        matchingOptions.add(opt);
                    }
                }
                if (!matchingOptions.isEmpty()) {
                    // Add category (expanded) then options
                    // We modify the copy state for display purposes, but let's just add them
                    displayList.add(cat);
                    displayList.addAll(matchingOptions);
                }
            }
        }
        // Always add "Other" option if search is active? User logic: "Manual entry if
        // not found"
        // The dialog can handle the logic to show a "Custom: [Query]" item if user hits
        // enter or types.
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (displayList.get(position) instanceof PaymentCategory) {
            return TYPE_CATEGORY;
        } else {
            return TYPE_OPTION;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CATEGORY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_category, parent, false);
            return new CategoryViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_option, parent, false);
            return new OptionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_CATEGORY) {
            ((CategoryViewHolder) holder).bind((PaymentCategory) displayList.get(position));
        } else {
            ((OptionViewHolder) holder).bind((PaymentOption) displayList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon;
        ImageView arrow;

        CategoryViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textCategoryName);
            icon = itemView.findViewById(R.id.imgCategoryIcon);
            arrow = itemView.findViewById(R.id.imgArrow);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    PaymentCategory cat = (PaymentCategory) displayList.get(pos);

                    // Smart Auto-Select:
                    // If category has only 1 option, select it directly instead of expanding
                    if (cat.getOptions().size() == 1) {
                        listener.onMethodSelected(cat.getOptions().get(0));
                    } else {
                        // Otherwise, expand/collapse as usual
                        cat.setExpanded(!cat.isExpanded());
                        updateDisplayList();
                    }
                }
            });
        }

        void bind(PaymentCategory category) {
            name.setText(category.getName());
            icon.setImageResource(category.getIconResId());
            if (category.isExpanded()) {
                arrow.setRotation(90);
            } else {
                arrow.setRotation(0);
            }
        }
    }

    class OptionViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        OptionViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textOptionName);
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    PaymentOption opt = (PaymentOption) displayList.get(pos);
                    listener.onMethodSelected(opt);
                }
            });
        }

        void bind(PaymentOption option) {
            name.setText(option.getName());
        }
    }
}
