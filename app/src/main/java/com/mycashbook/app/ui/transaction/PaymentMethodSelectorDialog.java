package com.mycashbook.app.ui.transaction;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mycashbook.app.R;
import com.mycashbook.app.model.PaymentCategory;
import com.mycashbook.app.model.PaymentOption;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodSelectorDialog extends BottomSheetDialogFragment {

    private PaymentMethodAdapter adapter;
    private OnPaymentMethodSelectedListener listener;

    public interface OnPaymentMethodSelectedListener {
        void onPaymentMethodSelected(PaymentOption option);
    }

    public static PaymentMethodSelectorDialog newInstance() {
        return new PaymentMethodSelectorDialog();
    }

    public void setListener(OnPaymentMethodSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_payment_selector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText editSearch = view.findViewById(R.id.editSearch);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerPaymentMethods);

        List<PaymentCategory> categories = getPaymentData();
        adapter = new PaymentMethodAdapter(categories, option -> {
            if (listener != null) {
                listener.onPaymentMethodSelected(option);
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
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private List<PaymentCategory> getPaymentData() {
        List<PaymentCategory> categories = new ArrayList<>();

        // 1. Cash
        List<PaymentOption> cashOptions = new ArrayList<>();
        cashOptions.add(new PaymentOption("Cash", R.drawable.ic_wallet, "Cash"));
        categories.add(new PaymentCategory("Cash", R.drawable.ic_wallet, cashOptions));

        // 2. Online / UPI / Wallets
        List<PaymentOption> upiOptions = new ArrayList<>();
        upiOptions.add(new PaymentOption("GPay (Google Pay)", R.drawable.ic_google, "UPI"));
        upiOptions.add(new PaymentOption("PhonePe", R.drawable.ic_start_up, "UPI"));
        upiOptions.add(new PaymentOption("Paytm", R.drawable.ic_wallet, "UPI"));
        upiOptions.add(new PaymentOption("BHIM UPI", R.drawable.ic_shield, "UPI"));
        upiOptions.add(new PaymentOption("Amazon Pay", R.drawable.ic_wallet, "Wallet"));
        upiOptions.add(new PaymentOption("WhatsApp Pay", R.drawable.ic_start_up, "UPI"));
        upiOptions.add(new PaymentOption("Airtel Money", R.drawable.ic_wallet, "Wallet"));
        upiOptions.add(new PaymentOption("JioPay", R.drawable.ic_wallet, "Wallet"));
        upiOptions.add(new PaymentOption("MobiKwik", R.drawable.ic_wallet, "Wallet"));
        upiOptions.add(new PaymentOption("Freecharge", R.drawable.ic_wallet, "Wallet"));
        categories.add(new PaymentCategory("Online (UPI & Wallets)", R.drawable.ic_fingerprint, upiOptions));

        // 3. Cards (Credit/Debit)
        List<PaymentOption> cardOptions = new ArrayList<>();
        cardOptions.add(new PaymentOption("Debit Card", R.drawable.ic_wallet, "Card"));
        cardOptions.add(new PaymentOption("Credit Card", R.drawable.ic_wallet, "Card"));
        cardOptions.add(new PaymentOption("Visa", R.drawable.ic_wallet, "Card"));
        cardOptions.add(new PaymentOption("MasterCard", R.drawable.ic_wallet, "Card"));
        cardOptions.add(new PaymentOption("RuPay", R.drawable.ic_wallet, "Card"));
        cardOptions.add(new PaymentOption("Amex (American Express)", R.drawable.ic_wallet, "Card"));
        cardOptions.add(new PaymentOption("Diners Club", R.drawable.ic_wallet, "Card"));
        cardOptions.add(new PaymentOption("Discover", R.drawable.ic_wallet, "Card"));
        cardOptions.add(new PaymentOption("Prepaid Card", R.drawable.ic_wallet, "Card"));
        categories.add(new PaymentCategory("Cards", R.drawable.ic_book, cardOptions));

        // 4. Net Banking & Transfers
        List<PaymentOption> bankOptions = new ArrayList<>();
        bankOptions.add(new PaymentOption("Bank Transfer", R.drawable.ic_book, "Bank"));
        bankOptions.add(new PaymentOption("Net Banking", R.drawable.ic_book, "Bank"));
        bankOptions.add(new PaymentOption("IMPS", R.drawable.ic_start_up, "Transfer"));
        bankOptions.add(new PaymentOption("NEFT", R.drawable.ic_book, "Transfer"));
        bankOptions.add(new PaymentOption("RTGS", R.drawable.ic_book, "Transfer"));
        bankOptions.add(new PaymentOption("Cheque", R.drawable.ic_edit, "Cheque"));
        bankOptions.add(new PaymentOption("Demand Draft", R.drawable.ic_edit, "DD"));
        categories.add(new PaymentCategory("Net Banking & Transfers", R.drawable.ic_book, bankOptions));

        // 5. International
        List<PaymentOption> intlOptions = new ArrayList<>();
        intlOptions.add(new PaymentOption("PayPal", R.drawable.ic_start_up, "International"));
        intlOptions.add(new PaymentOption("Stripe", R.drawable.ic_start_up, "International"));
        intlOptions.add(new PaymentOption("Wise", R.drawable.ic_start_up, "International"));
        intlOptions.add(new PaymentOption("Remitly", R.drawable.ic_start_up, "International"));
        intlOptions.add(new PaymentOption("Payoneer", R.drawable.ic_wallet, "International"));
        intlOptions.add(new PaymentOption("Skrill", R.drawable.ic_wallet, "International"));
        intlOptions.add(new PaymentOption("Western Union", R.drawable.ic_start_up, "International"));
        intlOptions.add(new PaymentOption("SWIFT Transfer", R.drawable.ic_book, "International"));
        intlOptions.add(new PaymentOption("Apple Pay", R.drawable.ic_wallet, "Wallet"));
        intlOptions.add(new PaymentOption("Samsung Pay", R.drawable.ic_wallet, "Wallet"));
        categories.add(new PaymentCategory("International", R.drawable.ic_start_up, intlOptions));

        // 6. Crypto & Digital Assets
        List<PaymentOption> cryptoOptions = new ArrayList<>();
        cryptoOptions.add(new PaymentOption("Bitcoin (BTC)", R.drawable.ic_trending_up, "Crypto"));
        cryptoOptions.add(new PaymentOption("Ethereum (ETH)", R.drawable.ic_trending_up, "Crypto"));
        cryptoOptions.add(new PaymentOption("USDT (Tether)", R.drawable.ic_trending_up, "Crypto"));
        cryptoOptions.add(new PaymentOption("Binance Pay", R.drawable.ic_trending_up, "Crypto"));
        cryptoOptions.add(new PaymentOption("Crypto Wallet", R.drawable.ic_wallet, "Crypto"));
        cryptoOptions.add(new PaymentOption("Coinbase", R.drawable.ic_trending_up, "Crypto"));
        categories.add(new PaymentCategory("Crypto & Digital Assets", R.drawable.ic_trending_up, cryptoOptions));

        // 7. Business & Revenue
        List<PaymentOption> businessOptions = new ArrayList<>();
        businessOptions.add(new PaymentOption("POS Terminal", R.drawable.ic_fingerprint, "Business"));
        businessOptions.add(new PaymentOption("QR Code", R.drawable.ic_fingerprint, "Business"));
        businessOptions.add(new PaymentOption("Stocks / Equity", R.drawable.ic_trending_up, "Investment"));
        businessOptions.add(new PaymentOption("Mutual Funds", R.drawable.ic_trending_up, "Investment"));
        businessOptions.add(new PaymentOption("Dividends", R.drawable.ic_wallet, "Revenue"));
        businessOptions.add(new PaymentOption("Rental Income", R.drawable.ic_wallet, "Revenue"));
        businessOptions.add(new PaymentOption("Royalties", R.drawable.ic_start_up, "Revenue"));
        businessOptions.add(new PaymentOption("Freelance / Contract", R.drawable.ic_edit, "Income"));
        businessOptions.add(new PaymentOption("Store Credit", R.drawable.ic_refresh, "Credit"));
        categories.add(new PaymentCategory("Business & Revenue", R.drawable.ic_start_up, businessOptions));

        // 8. Other
        List<PaymentOption> otherOptions = new ArrayList<>();
        otherOptions.add(new PaymentOption("Other", R.drawable.ic_edit, "Other"));
        categories.add(new PaymentCategory("Other", R.drawable.ic_more_vert, otherOptions));

        return categories;
    }
}
