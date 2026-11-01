package com.landlord.android.feature.notifications;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.common.AppExecutors;
import com.landlord.android.core.db.AppDatabase;
import com.landlord.android.core.db.NotificationDao;
import com.landlord.android.core.db.NotificationEntity;
import java.util.List;

public class NotificationRepository {

    private final NotificationDao notificationDao;

    public NotificationRepository(Context context) {
        this.notificationDao = AppDatabase.getInstance(context).notificationDao();
    }

    public LiveData<List<NotificationEntity>> observeNotifications() {
        return notificationDao.observeAll();
    }

    public LiveData<Integer> observeUnreadCount() {
        return notificationDao.observeUnreadCount();
    }

    public void markAsRead(String notificationId) {
        AppExecutors.DB.execute(() -> notificationDao.markAsRead(notificationId));
    }

    public void markAllAsRead() {
        AppExecutors.DB.execute(notificationDao::markAllAsRead);
    }

    public void deleteNotification(NotificationEntity notification) {
        AppExecutors.DB.execute(() -> notificationDao.delete(notification));
    }
}
