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
public interface MarketplaceRequestDao {

    @Query("SELECT * FROM marketplace_requests ORDER BY createdAt DESC")
    LiveData<List<MarketplaceRequestEntity>> observeAll();

    @Query("SELECT * FROM marketplace_requests ORDER BY createdAt DESC")
    List<MarketplaceRequestEntity> getAll();

    @Query("SELECT * FROM marketplace_requests WHERE localId = :localId LIMIT 1")
    MarketplaceRequestEntity getByIdSync(String localId);

    @Query("SELECT COUNT(*) FROM marketplace_requests WHERE status = 'pending'")
    LiveData<Integer> observePendingCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MarketplaceRequestEntity request);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MarketplaceRequestEntity> requests);

    @Update
    void update(MarketplaceRequestEntity request);

    @Delete
    void delete(MarketplaceRequestEntity request);

    @Query("DELETE FROM marketplace_requests")
    void deleteAll();
}
