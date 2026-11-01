package com.landlord.android.feature.reports;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.db.ExpenseEntity;
import com.landlord.android.core.db.InvoiceEntity;
import com.landlord.android.core.db.PaymentEntity;
import com.landlord.android.core.db.PropertyEntity;
import com.landlord.android.core.db.TenantEntity;
import com.landlord.android.feature.expenses.ExpenseRepository;
import com.landlord.android.feature.payments.PaymentRepository;
import com.landlord.android.feature.payments.TenantDueDto;
import com.landlord.android.feature.properties.PropertyRepository;
import com.landlord.android.feature.tenants.TenantRepository;
import java.util.List;

public class ReportsViewModel extends AndroidViewModel {

    private final PaymentRepository paymentRepo;
    private final ExpenseRepository expenseRepo;
    private final PropertyRepository propertyRepo;
    private final TenantRepository tenantRepo;

    public ReportsViewModel(@NonNull Application application) {
        super(application);
        this.paymentRepo = new PaymentRepository(application);
        this.expenseRepo = new ExpenseRepository(application);
        this.propertyRepo = new PropertyRepository(application);
        this.tenantRepo = new TenantRepository(application);
    }

    public LiveData<Double> getTotalIncome() {
        return paymentRepo.observeTotalCollected();
    }

    public LiveData<Double> getTotalExpenses() {
        return expenseRepo.observeTotalExpenses();
    }

    public LiveData<Double> getTotalOutstanding() {
        return paymentRepo.observeTotalOutstanding();
    }

    public LiveData<Integer> getOccupiedUnits() {
        return propertyRepo.countOccupied();
    }

    public LiveData<Integer> getTotalUnits() {
        return propertyRepo.countTotalUnits();
    }

    public LiveData<List<InvoiceEntity>> getInvoices() {
        return paymentRepo.observeInvoices();
    }

    public LiveData<List<PaymentEntity>> getPayments() {
        return paymentRepo.observePayments();
    }

    public LiveData<List<ExpenseEntity>> getExpenses() {
        return expenseRepo.observeExpenses();
    }

    public LiveData<List<PropertyEntity>> getProperties() {
        return propertyRepo.observeProperties();
    }

    public LiveData<List<TenantEntity>> getTenants() {
        return tenantRepo.observeTenants();
    }

    public LiveData<List<TenantDueDto>> getTenantDues() {
        return paymentRepo.observeTenantMonthlyDues();
    }
}
