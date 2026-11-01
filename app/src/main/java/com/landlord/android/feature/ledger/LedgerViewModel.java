package com.landlord.android.feature.ledger;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.landlord.android.core.db.ExpenseEntity;
import com.landlord.android.core.db.PaymentEntity;
import com.landlord.android.feature.expenses.ExpenseRepository;
import com.landlord.android.feature.payments.PaymentRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LedgerViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<LedgerEntry>> ledgerEntries = new MediatorLiveData<>();
    private List<PaymentEntity> cachedPayments = new ArrayList<>();
    private List<ExpenseEntity> cachedExpenses = new ArrayList<>();

    public LedgerViewModel(@NonNull Application application) {
        super(application);
        PaymentRepository paymentRepo = new PaymentRepository(application);
        ExpenseRepository expenseRepo = new ExpenseRepository(application);

        LiveData<List<PaymentEntity>> paymentsLive = paymentRepo.observePayments();
        LiveData<List<ExpenseEntity>> expensesLive = expenseRepo.observeExpenses();

        ledgerEntries.addSource(paymentsLive, payments -> {
            cachedPayments = payments != null ? payments : new ArrayList<>();
            recalculate();
        });

        ledgerEntries.addSource(expensesLive, expenses -> {
            cachedExpenses = expenses != null ? expenses : new ArrayList<>();
            recalculate();
        });
    }

    public LiveData<List<LedgerEntry>> getLedgerEntries() {
        return ledgerEntries;
    }

    private void recalculate() {
        List<LedgerEntry> combined = new ArrayList<>();

        for (PaymentEntity p : cachedPayments) {
            String desc = p.transactionRef != null && !p.transactionRef.isEmpty()
                    ? "Payment (" + p.transactionRef + ")"
                    : "Rent Payment Received";
            combined.add(new LedgerEntry(p.localId, p.date, true, desc, p.amount));
        }

        for (ExpenseEntity e : cachedExpenses) {
            combined.add(new LedgerEntry(e.localId, e.date, false, e.description, e.amount));
        }

        // Sort descending by date
        Collections.sort(combined, (a, b) -> b.date.compareTo(a.date));
        ledgerEntries.setValue(combined);
    }
}
