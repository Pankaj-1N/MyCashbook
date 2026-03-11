package com.mycashbook.app.ui.transaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mycashbook.app.R;

public class ExportFormatSelectorDialog extends BottomSheetDialogFragment {

    public interface OnFormatSelectedListener {
        void onFormatSelected(String format);
    }

    private OnFormatSelectedListener listener;

    public void setListener(OnFormatSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // We'll reuse a common list layout or create a quick one
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.setBackgroundResource(R.drawable.bg_summary_card_detailed); // Reuse glass effect

        addOption(layout, "Export as PDF", "pdf");
        addOption(layout, "Export as Excel (.xlsx)", "excel");
        addOption(layout, "Export as CSV", "csv");

        return layout;
    }

    private void addOption(LinearLayout layout, String label, String format) {
        com.google.android.material.button.MaterialButton btn = new com.google.android.material.button.MaterialButton(
                getContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btn.setText(label);
        btn.setAllCaps(false);
        btn.setTextColor(android.graphics.Color.WHITE);
        btn.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#33FFFFFF")));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> {
            if (listener != null)
                listener.onFormatSelected(format);
            dismiss();
        });

        layout.addView(btn);
    }
}
