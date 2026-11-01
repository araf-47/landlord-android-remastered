package com.landlord.android.core.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY date DESC, createdAt DESC")
    LiveData<List<ExpenseEntity>> observeAll();

    @Query("SELECT * FROM expenses ORDER BY date DESC, createdAt DESC")
    List<ExpenseEntity> getAll();

    @Query("SELECT * FROM expenses WHERE propertyId = :propertyId ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> observeByProperty(String propertyId);

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses")
    LiveData<Double> observeTotalExpenses();

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE date LIKE :periodPrefix || '%'")
    LiveData<Double> observePeriodExpenses(String periodPrefix);

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE date LIKE :periodPrefix || '%'")
    double getPeriodExpensesSync(String periodPrefix);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExpenseEntity expense);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ExpenseEntity> expenses);

    @Delete
    void delete(ExpenseEntity expense);

    @Query("DELETE FROM expenses")
    void deleteAll();
}
