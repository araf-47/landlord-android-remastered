package com.landlord.android.feature.marketplace;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.db.MarketplaceRequestEntity;
import java.util.List;

public class MarketplaceViewModel extends AndroidViewModel {

    private final MarketplaceRepository repository;

    public MarketplaceViewModel(@NonNull Application application) {
        super(application);
        this.repository = new MarketplaceRepository(application);
    }

    public LiveData<List<MarketplaceRequestEntity>> getRequests() {
        return repository.observeRequests();
    }

    public void approveRequest(MarketplaceRequestEntity request) {
        repository.updateStatus(request, "approved");
    }

    public void rejectRequest(MarketplaceRequestEntity request) {
        repository.updateStatus(request, "rejected");
    }
}
