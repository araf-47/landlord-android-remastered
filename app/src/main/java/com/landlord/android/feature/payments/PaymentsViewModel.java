package com.landlord.android.feature.payments;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.db.InvoiceEntity;
import com.landlord.android.core.db.PaymentEntity;
import com.landlord.android.core.db.TenantEntity;
import com.landlord.android.feature.tenants.TenantRepository;
import java.util.List;

public class PaymentsViewModel extends AndroidViewModel {

    private final PaymentRepository paymentRepo;
    private final TenantRepository tenantRepo;

    public PaymentsViewModel(@NonNull Application application) {
        super(application);
        this.paymentRepo = new PaymentRepository(application);
        this.tenantRepo = new TenantRepository(application);
    }

    public LiveData<List<InvoiceEntity>> getInvoices() {
        return paymentRepo.observeInvoices();
    }

    public LiveData<List<PaymentEntity>> getPayments() {
        return paymentRepo.observePayments();
    }

    public LiveData<List<TenantDueDto>> getTenantMonthlyDues() {
        return paymentRepo.observeTenantMonthlyDues();
    }

    public LiveData<Double> getTotalOutstanding() {
        return paymentRepo.observeTotalOutstanding();
    }

    public LiveData<Double> getTotalCollected() {
        return paymentRepo.observeTotalCollected();
    }

    public LiveData<List<TenantEntity>> getTenants() {
        return tenantRepo.observeTenants();
    }

    public void recordPayment(String tenantId, String invoiceId, double amount, String method, String ref) {
        paymentRepo.recordPayment(tenantId, invoiceId, amount, method, ref);
    }

    public void generateBill(String tenantId, String unitId, String period, double rent, double utilities, String dueDate) {
        paymentRepo.generateBill(tenantId, unitId, period, rent, utilities, "[]", dueDate);
    }
}
