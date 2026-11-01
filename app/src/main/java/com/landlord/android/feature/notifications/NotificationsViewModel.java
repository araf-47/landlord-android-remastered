package com.landlord.android.feature.notifications;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.db.NotificationEntity;
import java.util.List;

public class NotificationsViewModel extends AndroidViewModel {

    private final NotificationRepository repository;

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new NotificationRepository(application);
    }

    public LiveData<List<NotificationEntity>> getNotifications() {
        return repository.observeNotifications();
    }

    public LiveData<Integer> getUnreadCount() {
        return repository.observeUnreadCount();
    }

    public void markAsRead(String id) {
        repository.markAsRead(id);
    }

    public void markAllAsRead() {
        repository.markAllAsRead();
    }
}
