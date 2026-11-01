package com.landlord.android.feature.properties;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.db.PropertyEntity;
import com.landlord.android.core.db.UnitEntity;
import java.util.List;

public class PropertyDetailViewModel extends AndroidViewModel {

    private final PropertyRepository repository;

    public PropertyDetailViewModel(@NonNull Application application) {
        super(application);
        this.repository = new PropertyRepository(application);
    }

    public LiveData<PropertyEntity> getProperty(String propertyId) {
        return repository.observeProperty(propertyId);
    }

    public LiveData<List<UnitEntity>> getUnits(String propertyId) {
        return repository.observeUnitsByProperty(propertyId);
    }

    public void addUnit(String propertyId, String unitNumber, double rent, String propertyType, String utilitiesJson) {
        repository.createUnit(propertyId, unitNumber, rent, propertyType, utilitiesJson);
    }

    public void deleteProperty(PropertyEntity property) {
        repository.deleteProperty(property);
    }

    public void toggleUnitStatus(UnitEntity unit) {
        unit.status = "vacant".equalsIgnoreCase(unit.status) ? "occupied" : "vacant";
        repository.updateUnit(unit);
    }
}
