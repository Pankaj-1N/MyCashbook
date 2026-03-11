package com.mycashbook.app.billing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mycashbook.app.R;

import java.util.List;

public class BillingAdapter extends RecyclerView.Adapter<BillingAdapter.PlanViewHolder> {

    public interface OnPlanSelectedListener {
        void onPlanSelected(BillingPlan plan);
    }

    private final Context context;
    private final List<BillingPlan> planList;
    private int selectedPosition = -1;
    private final OnPlanSelectedListener listener;

    public BillingAdapter(Context context, List<BillingPlan> planList, OnPlanSelectedListener listener) {
        this.context = context;
        this.planList = planList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Note: We are using the layout name 'item_billing_plan'
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        BillingPlan plan = planList.get(position);

        holder.txtPlanName.setText(plan.getPlanName());
        holder.txtPlanPrice.setText(plan.getPriceText());
        holder.txtPlanDetails.setText(plan.getDescription());
        holder.radioButton.setChecked(position == selectedPosition);

        // Click on the whole card selects the item
        holder.itemView.setOnClickListener(v -> {
            updateSelection(holder.getAdapterPosition(), plan);
        });

        // Click on the radio button specifically selects the item
        holder.radioButton.setOnClickListener(v -> {
            updateSelection(holder.getAdapterPosition(), plan);
        });
    }

    private void updateSelection(int position, BillingPlan plan) {
        selectedPosition = position;
        notifyDataSetChanged();
        if (listener != null) {
            listener.onPlanSelected(plan);
        }
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {

        TextView txtPlanName, txtPlanPrice, txtPlanDetails;
        RadioButton radioButton;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPlanName = itemView.findViewById(R.id.txtPlanName);
            txtPlanPrice = itemView.findViewById(R.id.txtPlanPrice);
            txtPlanDetails = itemView.findViewById(R.id.txtPlanDetails);
            radioButton = itemView.findViewById(R.id.radioSelectPlan);
        }
    }
}
