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
public interface InvoiceDao {

    @Query("SELECT * FROM invoices ORDER BY period DESC, dueDate DESC")
    LiveData<List<InvoiceEntity>> observeAll();

    @Query("SELECT * FROM invoices ORDER BY period DESC, dueDate DESC")
    List<InvoiceEntity> getAll();

    @Query("SELECT * FROM invoices WHERE tenantId = :tenantId ORDER BY period DESC")
    LiveData<List<InvoiceEntity>> observeByTenant(String tenantId);

    @Query("SELECT * FROM invoices WHERE period = :period ORDER BY dueDate DESC")
    LiveData<List<InvoiceEntity>> observeByPeriod(String period);

    @Query("SELECT * FROM invoices WHERE localId = :localId LIMIT 1")
    LiveData<InvoiceEntity> getById(String localId);

    @Query("SELECT * FROM invoices WHERE localId = :localId LIMIT 1")
    InvoiceEntity getByIdSync(String localId);

    @Query("SELECT COALESCE(SUM(balance), 0.0) FROM invoices WHERE status != 'paid'")
    LiveData<Double> observeTotalOutstanding();

    @Query("SELECT COALESCE(SUM(balance), 0.0) FROM invoices WHERE status != 'paid' AND period = :period")
    LiveData<Double> observePeriodOutstanding(String period);

    @Query("SELECT COALESCE(SUM(balance), 0.0) FROM invoices WHERE status != 'paid'")
    double getTotalOutstandingSync();

    @Query("SELECT " +
           "t.localId AS tenantId, " +
           "t.name AS tenantName, " +
           "t.phone AS tenantPhone, " +
           "COALESCE(u.unitNumber, 'Unit') AS unitNumber, " +
           "COALESCE(p.name, 'Property') AS propertyName, " +
           "COALESCE(u.rent, 0.0) AS monthlyRent, " +
           "COALESCE(SUM(i.balance), 0.0) AS totalDue, " +
           "COALESCE(SUM(CASE WHEN i.status = 'paid' THEN i.amount ELSE (i.amount - i.balance) END), 0.0) AS totalPaid, " +
           "COALESCE(MAX(i.period), '') AS latestPeriod, " +
           "CASE " +
           "    WHEN COALESCE(SUM(i.balance), 0.0) <= 0.01 THEN 'PAID' " +
           "    WHEN COALESCE(SUM(i.balance), 0.0) < COALESCE(SUM(i.amount), 0.0) THEN 'PARTIAL' " +
           "    ELSE 'UNPAID' " +
           "END AS paymentStatus " +
           "FROM tenants t " +
           "LEFT JOIN units u ON t.unitId = u.localId " +
           "LEFT JOIN properties p ON u.propertyId = p.localId " +
           "LEFT JOIN invoices i ON t.localId = i.tenantId " +
           "WHERE t.status = 'active' " +
           "GROUP BY t.localId " +
           "ORDER BY totalDue DESC, t.name ASC")
    LiveData<List<com.landlord.android.feature.payments.TenantDueDto>> observeTenantMonthlyDues();

    @Query("SELECT * FROM invoices WHERE tenantId = :tenantId AND balance > 0 ORDER BY period ASC, dueDate ASC")
    List<InvoiceEntity> getUnpaidInvoicesByTenantSync(String tenantId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(InvoiceEntity invoice);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<InvoiceEntity> invoices);

    @Update
    void update(InvoiceEntity invoice);

    @Delete
    void delete(InvoiceEntity invoice);

    @Query("DELETE FROM invoices")
    void deleteAll();
}
