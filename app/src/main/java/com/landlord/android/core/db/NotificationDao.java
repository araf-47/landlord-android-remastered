package com.landlord.android.core.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    LiveData<List<NotificationEntity>> observeAll();

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    List<NotificationEntity> getAll();

    @Query("SELECT COUNT(*) FROM notifications WHERE read = 0")
    LiveData<Integer> observeUnreadCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NotificationEntity notification);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<NotificationEntity> notifications);

    @Update
    void update(NotificationEntity notification);

    @Query("UPDATE notifications SET read = 1 WHERE localId = :localId")
    void markAsRead(String localId);

    @Query("UPDATE notifications SET read = 1")
    void markAllAsRead();

    @Delete
    void delete(NotificationEntity notification);

    @Query("DELETE FROM notifications")
    void deleteAll();
}
