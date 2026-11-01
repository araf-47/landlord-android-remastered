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
public interface RentalAgreementDao {

    @Query("SELECT * FROM rental_agreements WHERE tenantId = :tenantId LIMIT 1")
    LiveData<RentalAgreementEntity> getByTenantId(String tenantId);

    @Query("SELECT * FROM rental_agreements")
    LiveData<List<RentalAgreementEntity>> observeAll();

    @Query("SELECT * FROM rental_agreements WHERE tenantId = :tenantId LIMIT 1")
    RentalAgreementEntity getByTenantIdSync(String tenantId);

    @Query("SELECT * FROM rental_agreements WHERE unitId = :unitId LIMIT 1")
    RentalAgreementEntity getByUnitIdSync(String unitId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RentalAgreementEntity agreement);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RentalAgreementEntity> agreements);

    @Update
    void update(RentalAgreementEntity agreement);

    @Delete
    void delete(RentalAgreementEntity agreement);

    @Query("DELETE FROM rental_agreements")
    void deleteAll();
}
