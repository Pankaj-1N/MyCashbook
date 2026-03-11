package com.mycashbook.app.billing;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.mycashbook.app.R;

import java.util.List;

/**
 * SubscriptionPlanAdapter - RecyclerView adapter for subscription plans
 * GLITCH PREVENTION:
 * - Null safety for all operations
 * - Proper view holder pattern
 * - Safe list updates
 */
public class SubscriptionPlanAdapter extends RecyclerView.Adapter<SubscriptionPlanAdapter.PlanViewHolder> {

    private final Context context;
    private List<SubscriptionPlan> plans;
    private final OnPlanClickListener listener;

    public interface OnPlanClickListener {
        void onPlanClick(SubscriptionPlan plan);
    }

    public SubscriptionPlanAdapter(Context context, List<SubscriptionPlan> plans, OnPlanClickListener listener) {
        this.context = context;
        this.plans = plans;
        this.listener = listener;
    }

    /**
     * Update plans list safely
     * GLITCH PREVENTION: Null check and notify adapter
     */
    public void updatePlans(List<SubscriptionPlan> newPlans) {
        if (newPlans != null) {
            this.plans = newPlans;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subscription_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        if (plans == null || position >= plans.size()) {
            return;
        }

        SubscriptionPlan plan = plans.get(position);
        if (plan == null) {
            return;
        }

        // Set plan name
        holder.textPlanName.setText(plan.name);

        // Set price
        if (plan.price == 0) {
            holder.textPrice.setText("₹0");
            holder.textPricePeriod.setVisibility(View.GONE);
        } else {
            holder.textPrice.setText("₹" + plan.price);
            holder.textPricePeriod.setText("/" + plan.period);
            holder.textPricePeriod.setVisibility(View.VISIBLE);
        }

        // Set icon color
        try {
            holder.iconContainer.setCardBackgroundColor(Color.parseColor(plan.color));
        } catch (Exception e) {
            holder.iconContainer.setCardBackgroundColor(Color.parseColor("#00B4D8"));
        }

        // Show/hide badge
        if (plan.isPopular) {
            holder.textBadge.setVisibility(View.VISIBLE);
            if (plan.name.contains("Premium")) {
                holder.textBadge.setText("BEST VALUE");
            } else {
                holder.textBadge.setText("MOST POPULAR");
            }
        } else {
            holder.textBadge.setVisibility(View.GONE);
        }

        // Add features
        holder.featuresContainer.removeAllViews();
        if (plan.features != null) {
            for (String feature : plan.features) {
                addFeatureView(holder.featuresContainer, feature);
            }
        }

        // Set button text and state
        if (plan.isCurrent) {
            holder.btnUpgrade.setText("Current Plan");
            holder.btnUpgrade.setEnabled(false);
            holder.btnUpgrade.setBackgroundTintList(context.getColorStateList(R.color.text_secondary));
        } else {
            if (plan.price == 0) {
                holder.btnUpgrade.setText("Downgrade");
            } else {
                holder.btnUpgrade.setText("Upgrade Now");
            }
            holder.btnUpgrade.setEnabled(true);
            try {
                holder.btnUpgrade.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor(plan.color)));
            } catch (Exception e) {
                holder.btnUpgrade.setBackgroundTintList(context.getColorStateList(R.color.cyan));
            }
        }

        // Handle click
        holder.btnUpgrade.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlanClick(plan);
            }
        });
    }

    /**
     * Add feature item to container
     * GLITCH PREVENTION: Safe view creation
     */
    private void addFeatureView(LinearLayout container, String feature) {
        try {
            View featureView = LayoutInflater.from(context).inflate(R.layout.item_feature, container, false);
            TextView textFeature = featureView.findViewById(R.id.textFeature);
            if (textFeature != null) {
                textFeature.setText(feature);
            }
            container.addView(featureView);
        } catch (Exception e) {
            // Fallback: create simple TextView
            TextView textView = new TextView(context);
            textView.setText("✓ " + feature);
            textView.setTextColor(context.getColor(android.R.color.white));
            textView.setTextSize(14);
            textView.setPadding(0, 8, 0, 8);
            container.addView(textView);
        }
    }

    @Override
    public int getItemCount() {
        return plans != null ? plans.size() : 0;
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView textBadge;
        CardView iconContainer;
        ImageView iconPlan;
        TextView textPlanName;
        TextView textPrice;
        TextView textPricePeriod;
        LinearLayout featuresContainer;
        MaterialButton btnUpgrade;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            textBadge = itemView.findViewById(R.id.textBadge);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            iconPlan = itemView.findViewById(R.id.iconPlan);
            textPlanName = itemView.findViewById(R.id.textPlanName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textPricePeriod = itemView.findViewById(R.id.textPricePeriod);
            featuresContainer = itemView.findViewById(R.id.featuresContainer);
            btnUpgrade = itemView.findViewById(R.id.btnUpgrade);
        }
    }
}
