package com.landlord.android;

import android.app.Application;
import com.google.android.material.color.DynamicColors;
import com.landlord.android.core.common.AppExecutors;
import com.landlord.android.core.db.AppDatabase;
import com.landlord.android.core.db.DatabaseSeeder;

public class LandlordApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Apply Material You dynamic colors if available (Android 12+)
        DynamicColors.applyToActivitiesIfAvailable(this);

        // Warm up and ensure database is populated with rich demo data on fresh install
        AppExecutors.DB.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            if (db.propertyDao().getAll().isEmpty()) {
                DatabaseSeeder.seed(db);
            }
        });
    }
}
