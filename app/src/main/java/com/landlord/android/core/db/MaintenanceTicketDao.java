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
public interface MaintenanceTicketDao {

    @Query("SELECT * FROM maintenance_tickets ORDER BY createdAt DESC")
    LiveData<List<MaintenanceTicketEntity>> observeAll();

    @Query("SELECT * FROM maintenance_tickets ORDER BY createdAt DESC")
    List<MaintenanceTicketEntity> getAll();

    @Query("SELECT COUNT(*) FROM maintenance_tickets WHERE status = 'pending' OR status = 'in_progress'")
    LiveData<Integer> observePendingCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MaintenanceTicketEntity ticket);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MaintenanceTicketEntity> tickets);

    @Update
    void update(MaintenanceTicketEntity ticket);

    @Delete
    void delete(MaintenanceTicketEntity ticket);

    @Query("DELETE FROM maintenance_tickets")
    void deleteAll();
}
