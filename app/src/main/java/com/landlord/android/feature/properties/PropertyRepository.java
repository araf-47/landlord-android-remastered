package com.landlord.android.feature.properties;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.auth.SessionManager;
import com.landlord.android.core.common.AppExecutors;
import com.landlord.android.core.db.AppDatabase;
import com.landlord.android.core.db.PropertyDao;
import com.landlord.android.core.db.PropertyEntity;
import com.landlord.android.core.db.UnitDao;
import com.landlord.android.core.db.UnitEntity;
import java.util.List;
import java.util.UUID;

public class PropertyRepository {

    private final Context appContext;
    private final PropertyDao propertyDao;
    private final UnitDao unitDao;

    public PropertyRepository(Context context) {
        this.appContext = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(appContext);
        this.propertyDao = db.propertyDao();
        this.unitDao = db.unitDao();
    }

    public LiveData<List<PropertyEntity>> observeProperties() {
        return propertyDao.observeAll();
    }

    public LiveData<PropertyEntity> observeProperty(String propertyId) {
        return propertyDao.getById(propertyId);
    }

    public void createProperty(String name, String address, String district, String area, String propertyType) {
        AppExecutors.DB.execute(() -> {
            PropertyEntity entity = new PropertyEntity();
            entity.localId = "prop-" + UUID.randomUUID().toString().substring(0, 8);
            entity.name = name;
            entity.address = address;
            entity.district = district;
            entity.area = area;
            entity.propertyType = propertyType;
            entity.landlordId = SessionManager.getInstance(appContext).getLandlordId();
            entity.createdAt = System.currentTimeMillis();
            propertyDao.insert(entity);
        });
    }

    public void deleteProperty(PropertyEntity property) {
        AppExecutors.DB.execute(() -> propertyDao.delete(property));
    }

    public LiveData<List<UnitEntity>> observeUnitsByProperty(String propertyId) {
        return unitDao.observeByProperty(propertyId);
    }

    public LiveData<List<UnitEntity>> observeAllUnits() {
        return unitDao.observeAll();
    }

    public LiveData<UnitEntity> observeUnit(String unitId) {
        return unitDao.getById(unitId);
    }

    public void createUnit(String propertyId, String unitNumber, double rent, String propertyType, String utilitiesJson) {
        AppExecutors.DB.execute(() -> {
            UnitEntity unit = new UnitEntity();
            unit.localId = "unit-" + UUID.randomUUID().toString().substring(0, 8);
            unit.propertyId = propertyId;
            unit.unitNumber = unitNumber;
            unit.rent = rent;
            unit.status = "vacant";
            unit.propertyType = propertyType;
            unit.adPaused = false;
            unit.utilityItemsJson = utilitiesJson;
            unit.createdAt = System.currentTimeMillis();
            unitDao.insert(unit);
        });
    }

    public void updateUnit(UnitEntity unit) {
        AppExecutors.DB.execute(() -> unitDao.update(unit));
    }

    public void deleteUnit(UnitEntity unit) {
        AppExecutors.DB.execute(() -> unitDao.delete(unit));
    }

    public LiveData<Integer> countOccupied() {
        return unitDao.countOccupied();
    }

    public LiveData<Integer> countTotalUnits() {
        return unitDao.countTotal();
    }
}
