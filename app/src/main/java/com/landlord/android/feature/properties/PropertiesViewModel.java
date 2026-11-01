package com.landlord.android.feature.properties;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.db.PropertyEntity;
import java.util.List;

public class PropertiesViewModel extends AndroidViewModel {

    private final PropertyRepository repository;

    public PropertiesViewModel(@NonNull Application application) {
        super(application);
        this.repository = new PropertyRepository(application);
    }

    public LiveData<List<PropertyEntity>> getProperties() {
        return repository.observeProperties();
    }

    public LiveData<List<com.landlord.android.core.db.UnitEntity>> getAllUnits() {
        return repository.observeAllUnits();
    }

    public void addProperty(String name, String address, String district, String area, String propertyType) {
        repository.createProperty(name, address, district, area, propertyType);
    }

    public void deleteProperty(PropertyEntity property) {
        repository.deleteProperty(property);
    }
}
