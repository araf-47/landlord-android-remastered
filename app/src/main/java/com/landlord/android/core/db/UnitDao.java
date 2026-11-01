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
public interface UnitDao {

    @Query("SELECT * FROM units WHERE propertyId = :propertyId ORDER BY unitNumber ASC")
    LiveData<List<UnitEntity>> observeByProperty(String propertyId);

    @Query("SELECT * FROM units WHERE propertyId = :propertyId ORDER BY unitNumber ASC")
    List<UnitEntity> getByPropertySync(String propertyId);

    @Query("SELECT * FROM units ORDER BY propertyId, unitNumber ASC")
    LiveData<List<UnitEntity>> observeAll();

    @Query("SELECT * FROM units ORDER BY propertyId, unitNumber ASC")
    List<UnitEntity> getAll();

    @Query("SELECT * FROM units WHERE localId = :localId LIMIT 1")
    LiveData<UnitEntity> getById(String localId);

    @Query("SELECT * FROM units WHERE localId = :localId LIMIT 1")
    UnitEntity getByIdSync(String localId);

    @Query("SELECT COUNT(*) FROM units WHERE status = 'occupied'")
    LiveData<Integer> countOccupied();

    @Query("SELECT COUNT(*) FROM units")
    LiveData<Integer> countTotal();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UnitEntity unit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UnitEntity> units);

    @Update
    void update(UnitEntity unit);

    @Delete
    void delete(UnitEntity unit);

    @Query("DELETE FROM units")
    void deleteAll();
}
