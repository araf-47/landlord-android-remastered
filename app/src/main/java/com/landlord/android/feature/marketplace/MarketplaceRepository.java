package com.landlord.android.feature.marketplace;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.common.AppExecutors;
import com.landlord.android.core.db.AppDatabase;
import com.landlord.android.core.db.MarketplaceRequestDao;
import com.landlord.android.core.db.MarketplaceRequestEntity;
import java.util.List;

public class MarketplaceRepository {

    private final MarketplaceRequestDao requestDao;

    public MarketplaceRepository(Context context) {
        this.requestDao = AppDatabase.getInstance(context).marketplaceRequestDao();
    }

    public LiveData<List<MarketplaceRequestEntity>> observeRequests() {
        return requestDao.observeAll();
    }

    public LiveData<Integer> observePendingCount() {
        return requestDao.observePendingCount();
    }

    public void updateStatus(MarketplaceRequestEntity request, String newStatus) {
        AppExecutors.DB.execute(() -> {
            request.status = newStatus;
            requestDao.update(request);
        });
    }

    public void deleteRequest(MarketplaceRequestEntity request) {
        AppExecutors.DB.execute(() -> requestDao.delete(request));
    }
}
