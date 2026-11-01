package com.landlord.android.feature.dashboard;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.auth.SessionManager;
import com.landlord.android.feature.expenses.ExpenseRepository;
import com.landlord.android.feature.maintenance.MaintenanceRepository;
import com.landlord.android.feature.payments.PaymentRepository;
import com.landlord.android.feature.properties.PropertyRepository;

public class DashboardViewModel extends AndroidViewModel {

    private final PropertyRepository propertyRepo;
    private final PaymentRepository paymentRepo;
    private final ExpenseRepository expenseRepo;
    private final MaintenanceRepository maintenanceRepo;
    private final SessionManager sessionManager;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.propertyRepo = new PropertyRepository(application);
        this.paymentRepo = new PaymentRepository(application);
        this.expenseRepo = new ExpenseRepository(application);
        this.maintenanceRepo = new MaintenanceRepository(application);
        this.sessionManager = SessionManager.getInstance(application);
    }

    public String getLandlordName() {
        return sessionManager.getDisplayName();
    }

    public LiveData<Integer> getOccupiedUnitsCount() {
        return propertyRepo.countOccupied();
    }

    public LiveData<Integer> getTotalUnitsCount() {
        return propertyRepo.countTotalUnits();
    }

    public LiveData<Double> getTotalCollected() {
        return paymentRepo.observeTotalCollected();
    }

    public LiveData<Double> getTotalOutstanding() {
        return paymentRepo.observeTotalOutstanding();
    }

    public LiveData<Double> getTotalExpenses() {
        return expenseRepo.observeTotalExpenses();
    }

    public LiveData<Integer> getPendingMaintenanceCount() {
        return maintenanceRepo.observePendingCount();
    }

    public LiveData<java.util.List<com.landlord.android.core.db.PaymentEntity>> getPayments() {
        return paymentRepo.observePayments();
    }
}
