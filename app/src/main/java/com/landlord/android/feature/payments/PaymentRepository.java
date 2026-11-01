package com.landlord.android.feature.payments;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.common.AppExecutors;
import com.landlord.android.core.db.AppDatabase;
import com.landlord.android.core.db.InvoiceDao;
import com.landlord.android.core.db.InvoiceEntity;
import com.landlord.android.core.db.PaymentDao;
import com.landlord.android.core.db.PaymentEntity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PaymentRepository {

    private final InvoiceDao invoiceDao;
    private final PaymentDao paymentDao;

    public PaymentRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.invoiceDao = db.invoiceDao();
        this.paymentDao = db.paymentDao();
    }

    public LiveData<List<InvoiceEntity>> observeInvoices() {
        return invoiceDao.observeAll();
    }

    public LiveData<List<PaymentEntity>> observePayments() {
        return paymentDao.observeAll();
    }

    public LiveData<List<InvoiceEntity>> observeTenantInvoices(String tenantId) {
        return invoiceDao.observeByTenant(tenantId);
    }

    public LiveData<List<PaymentEntity>> observeTenantPayments(String tenantId) {
        return paymentDao.observeByTenant(tenantId);
    }

    public LiveData<Double> observeTotalCollected() {
        return paymentDao.observeTotalCollected();
    }

    public LiveData<Double> observeTotalOutstanding() {
        return invoiceDao.observeTotalOutstanding();
    }

    public LiveData<Double> observePeriodCollected(String period) {
        return paymentDao.observePeriodCollected(period);
    }

    public LiveData<Double> observePeriodOutstanding(String period) {
        return invoiceDao.observePeriodOutstanding(period);
    }

    public LiveData<List<TenantDueDto>> observeTenantMonthlyDues() {
        return invoiceDao.observeTenantMonthlyDues();
    }

    public void recordPayment(String tenantId, String invoiceId, double amount, String method, String transactionRef) {
        AppExecutors.DB.execute(() -> {
            long now = System.currentTimeMillis();
            String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(now));

            PaymentEntity payment = new PaymentEntity();
            payment.localId = "pay-" + UUID.randomUUID().toString().substring(0, 8);
            payment.tenantId = tenantId;
            payment.invoiceId = invoiceId != null ? invoiceId : "";
            payment.amount = amount;
            payment.method = method;
            payment.status = "confirmed";
            payment.date = dateStr;
            payment.transactionRef = transactionRef;
            payment.createdAt = now;
            paymentDao.insert(payment);

            // Update invoice balance
            if (invoiceId != null && !invoiceId.isEmpty()) {
                InvoiceEntity invoice = invoiceDao.getByIdSync(invoiceId);
                if (invoice != null) {
                    double newBalance = invoice.balance - amount;
                    if (newBalance <= 0) {
                        invoice.balance = 0;
                        invoice.status = "paid";
                    } else {
                        invoice.balance = newBalance;
                        invoice.status = "partial";
                    }
                    invoiceDao.update(invoice);
                }
            } else if (tenantId != null && !tenantId.isEmpty()) {
                // Auto-allocate payment across open/unpaid invoices for this tenant
                List<InvoiceEntity> openInvoices = invoiceDao.getUnpaidInvoicesByTenantSync(tenantId);
                double remaining = amount;
                for (InvoiceEntity inv : openInvoices) {
                    if (remaining <= 0) break;
                    if (remaining >= inv.balance) {
                        remaining -= inv.balance;
                        inv.balance = 0;
                        inv.status = "paid";
                    } else {
                        inv.balance -= remaining;
                        inv.status = "partial";
                        remaining = 0;
                    }
                    invoiceDao.update(inv);
                }
            }
        });
    }

    public void generateBill(String tenantId, String unitId, String period, double rent,
                             double utilityTotal, String utilitiesJson, String dueDate) {
        AppExecutors.DB.execute(() -> {
            long now = System.currentTimeMillis();
            double total = rent + utilityTotal;

            InvoiceEntity inv = new InvoiceEntity();
            inv.localId = "inv-" + UUID.randomUUID().toString().substring(0, 8);
            inv.tenantId = tenantId;
            inv.unitId = unitId;
            inv.period = period;
            inv.rent = rent;
            inv.utilityTotal = utilityTotal;
            inv.utilityItemsJson = utilitiesJson;
            inv.prevUnpaidRolled = 0;
            inv.amount = total;
            inv.balance = total;
            inv.status = "unpaid";
            inv.dueDate = dueDate;
            inv.createdAt = now;
            invoiceDao.insert(inv);
        });
    }
}
