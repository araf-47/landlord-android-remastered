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
public interface TenantDao {

    @Query("SELECT * FROM tenants ORDER BY name ASC")
    LiveData<List<TenantEntity>> observeAll();

    @Query("SELECT * FROM tenants ORDER BY name ASC")
    List<TenantEntity> getAll();

    @Query("SELECT * FROM tenants WHERE localId = :localId LIMIT 1")
    LiveData<TenantEntity> getById(String localId);

    @Query("SELECT * FROM tenants WHERE localId = :localId LIMIT 1")
    TenantEntity getByIdSync(String localId);

    @Query("SELECT * FROM tenants WHERE unitId = :unitId AND status = 'active' LIMIT 1")
    LiveData<TenantEntity> getByUnitId(String unitId);

    @Query("SELECT * FROM tenants WHERE unitId = :unitId AND status = 'active' LIMIT 1")
    TenantEntity getByUnitIdSync(String unitId);

    @Query("SELECT COUNT(*) FROM tenants WHERE status = 'active'")
    LiveData<Integer> countActive();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TenantEntity tenant);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TenantEntity> tenants);

    @Update
    void update(TenantEntity tenant);

    @Delete
    void delete(TenantEntity tenant);

    @Query("DELETE FROM tenants")
    void deleteAll();
}
