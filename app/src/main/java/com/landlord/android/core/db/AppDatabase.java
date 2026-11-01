package com.landlord.android.core.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.landlord.android.core.common.AppExecutors;

@Database(
    entities = {
        PropertyEntity.class,
        UnitEntity.class,
        TenantEntity.class,
        RentalAgreementEntity.class,
        InvoiceEntity.class,
        PaymentEntity.class,
        ExpenseEntity.class,
        MaintenanceTicketEntity.class,
        MarketplaceRequestEntity.class,
        ConversationEntity.class,
        MessageEntity.class,
        NotificationEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract PropertyDao propertyDao();
    public abstract UnitDao unitDao();
    public abstract TenantDao tenantDao();
    public abstract RentalAgreementDao rentalAgreementDao();
    public abstract InvoiceDao invoiceDao();
    public abstract PaymentDao paymentDao();
    public abstract ExpenseDao expenseDao();
    public abstract MaintenanceTicketDao maintenanceTicketDao();
    public abstract MarketplaceRequestDao marketplaceRequestDao();
    public abstract ConversationDao conversationDao();
    public abstract MessageDao messageDao();
    public abstract NotificationDao notificationDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "landlord_standalone.db"
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            AppExecutors.DB.execute(() -> {
                                if (instance != null) {
                                    DatabaseSeeder.seed(instance);
                                }
                            });
                        }
                    })
                    .build();
                }
            }
        }
        return instance;
    }
}
