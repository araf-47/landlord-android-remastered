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
public interface PaymentDao {

    @Query("SELECT * FROM payments ORDER BY date DESC, createdAt DESC")
    LiveData<List<PaymentEntity>> observeAll();

    @Query("SELECT * FROM payments ORDER BY date DESC, createdAt DESC")
    List<PaymentEntity> getAll();

    @Query("SELECT * FROM payments WHERE tenantId = :tenantId ORDER BY date DESC")
    LiveData<List<PaymentEntity>> observeByTenant(String tenantId);

    @Query("SELECT * FROM payments WHERE localId = :localId LIMIT 1")
    LiveData<PaymentEntity> getById(String localId);

    @Query("SELECT * FROM payments WHERE localId = :localId LIMIT 1")
    PaymentEntity getByIdSync(String localId);

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE status = 'confirmed'")
    LiveData<Double> observeTotalCollected();

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE status = 'confirmed' AND date LIKE :periodPrefix || '%'")
    LiveData<Double> observePeriodCollected(String periodPrefix);

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE status = 'confirmed' AND date LIKE :periodPrefix || '%'")
    double getPeriodCollectedSync(String periodPrefix);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PaymentEntity payment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PaymentEntity> payments);

    @Update
    void update(PaymentEntity payment);

    @Delete
    void delete(PaymentEntity payment);

    @Query("DELETE FROM payments")
    void deleteAll();
}
