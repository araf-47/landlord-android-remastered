package com.landlord.android.feature.tenants;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.db.TenantEntity;
import com.landlord.android.core.db.UnitEntity;
import com.landlord.android.feature.properties.PropertyRepository;
import java.util.List;

public class TenantsViewModel extends AndroidViewModel {

    private final TenantRepository tenantRepo;
    private final PropertyRepository propertyRepo;
    private final com.landlord.android.feature.payments.PaymentRepository paymentRepo;

    public TenantsViewModel(@NonNull Application application) {
        super(application);
        this.tenantRepo = new TenantRepository(application);
        this.propertyRepo = new PropertyRepository(application);
        this.paymentRepo = new com.landlord.android.feature.payments.PaymentRepository(application);
    }

    public LiveData<List<TenantEntity>> getTenants() {
        return tenantRepo.observeTenants();
    }

    public LiveData<List<UnitEntity>> getAllUnits() {
        return propertyRepo.observeAllUnits();
    }

    public LiveData<List<com.landlord.android.core.db.RentalAgreementEntity>> getAllAgreements() {
        return tenantRepo.observeAllAgreements();
    }

    public LiveData<List<com.landlord.android.feature.payments.TenantDueDto>> getTenantMonthlyDues() {
        return paymentRepo.observeTenantMonthlyDues();
    }

    public void registerTenant(String name, String phone, String email, String nid,
                               String unitId, String startDate, String terms, double deposit) {
        tenantRepo.registerTenant(name, phone, email, nid, unitId, startDate, terms, deposit);
    }
}
