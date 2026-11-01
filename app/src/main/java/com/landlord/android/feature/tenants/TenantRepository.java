package com.landlord.android.feature.tenants;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.common.AppExecutors;
import com.landlord.android.core.db.AppDatabase;
import com.landlord.android.core.db.RentalAgreementDao;
import com.landlord.android.core.db.RentalAgreementEntity;
import com.landlord.android.core.db.TenantDao;
import com.landlord.android.core.db.TenantEntity;
import com.landlord.android.core.db.UnitDao;
import com.landlord.android.core.db.UnitEntity;
import java.util.List;
import java.util.UUID;

public class TenantRepository {

    private final TenantDao tenantDao;
    private final RentalAgreementDao agreementDao;
    private final UnitDao unitDao;

    public TenantRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.tenantDao = db.tenantDao();
        this.agreementDao = db.rentalAgreementDao();
        this.unitDao = db.unitDao();
    }

    public LiveData<List<TenantEntity>> observeTenants() {
        return tenantDao.observeAll();
    }

    public LiveData<TenantEntity> observeTenant(String tenantId) {
        return tenantDao.getById(tenantId);
    }

    public LiveData<RentalAgreementEntity> observeAgreement(String tenantId) {
        return agreementDao.getByTenantId(tenantId);
    }

    public LiveData<TenantEntity> observeTenantByUnit(String unitId) {
        return tenantDao.getByUnitId(unitId);
    }

    public LiveData<List<RentalAgreementEntity>> observeAllAgreements() {
        return agreementDao.observeAll();
    }

    public void registerTenant(String name, String phone, String email, String nid,
                               String unitId, String startDate, String terms, double deposit) {
        AppExecutors.DB.execute(() -> {
            String tenantId = "t-" + UUID.randomUUID().toString().substring(0, 8);
            long now = System.currentTimeMillis();

            TenantEntity tenant = new TenantEntity();
            tenant.localId = tenantId;
            tenant.name = name;
            tenant.phone = phone;
            tenant.email = email;
            tenant.nationalId = nid;
            tenant.unitId = unitId;
            tenant.status = "active";
            tenant.createdAt = now;
            tenantDao.insert(tenant);

            RentalAgreementEntity agreement = new RentalAgreementEntity();
            agreement.localId = "agr-" + UUID.randomUUID().toString().substring(0, 8);
            agreement.tenantId = tenantId;
            agreement.unitId = unitId;
            agreement.startDate = startDate;
            agreement.terms = terms;
            agreement.deposit = deposit;
            agreement.createdAt = now;
            agreementDao.insert(agreement);

            // Update Unit status to occupied
            if (unitId != null && !unitId.isEmpty()) {
                UnitEntity unit = unitDao.getByIdSync(unitId);
                if (unit != null) {
                    unit.status = "occupied";
                    unitDao.update(unit);
                }
            }
        });
    }

    public void updateAgreement(RentalAgreementEntity agreement) {
        AppExecutors.DB.execute(() -> agreementDao.update(agreement));
    }

    public void moveOutTenant(String tenantId) {
        AppExecutors.DB.execute(() -> {
            TenantEntity tenant = tenantDao.getByIdSync(tenantId);
            if (tenant != null) {
                String unitId = tenant.unitId;
                tenant.status = "inactive";
                tenant.unitId = null;
                tenantDao.update(tenant);

                if (unitId != null && !unitId.isEmpty()) {
                    UnitEntity unit = unitDao.getByIdSync(unitId);
                    if (unit != null) {
                        unit.status = "vacant";
                        unitDao.update(unit);
                    }
                }
            }
        });
    }

    public LiveData<Integer> countActiveTenants() {
        return tenantDao.countActive();
    }
}
