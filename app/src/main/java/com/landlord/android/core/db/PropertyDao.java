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
public interface PropertyDao {

    @Query("SELECT * FROM properties ORDER BY createdAt DESC")
    LiveData<List<PropertyEntity>> observeAll();

    @Query("SELECT * FROM properties ORDER BY createdAt DESC")
    List<PropertyEntity> getAll();

    @Query("SELECT * FROM properties WHERE localId = :localId LIMIT 1")
    LiveData<PropertyEntity> getById(String localId);

    @Query("SELECT * FROM properties WHERE localId = :localId LIMIT 1")
    PropertyEntity getByIdSync(String localId);

    @Query("SELECT COUNT(*) FROM properties")
    LiveData<Integer> count();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PropertyEntity property);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PropertyEntity> properties);

    @Update
    void update(PropertyEntity property);

    @Delete
    void delete(PropertyEntity property);

    @Query("DELETE FROM properties")
    void deleteAll();
}
