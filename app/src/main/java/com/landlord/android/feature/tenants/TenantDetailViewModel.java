package com.landlord.android.feature.tenants;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.db.RentalAgreementEntity;
import com.landlord.android.core.db.TenantEntity;

public class TenantDetailViewModel extends AndroidViewModel {

    private final TenantRepository repository;

    public TenantDetailViewModel(@NonNull Application application) {
        super(application);
        this.repository = new TenantRepository(application);
    }

    public LiveData<TenantEntity> getTenant(String tenantId) {
        return repository.observeTenant(tenantId);
    }

    public LiveData<RentalAgreementEntity> getAgreement(String tenantId) {
        return repository.observeAgreement(tenantId);
    }

    public void moveOutTenant(String tenantId) {
        repository.moveOutTenant(tenantId);
    }

    public void updateAgreement(RentalAgreementEntity agreement) {
        repository.updateAgreement(agreement);
    }
}
