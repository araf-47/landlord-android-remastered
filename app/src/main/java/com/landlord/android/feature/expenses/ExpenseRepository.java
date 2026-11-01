package com.landlord.android.feature.expenses;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.common.AppExecutors;
import com.landlord.android.core.db.AppDatabase;
import com.landlord.android.core.db.ExpenseDao;
import com.landlord.android.core.db.ExpenseEntity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ExpenseRepository {

    private final ExpenseDao expenseDao;

    public ExpenseRepository(Context context) {
        this.expenseDao = AppDatabase.getInstance(context).expenseDao();
    }

    public LiveData<List<ExpenseEntity>> observeExpenses() {
        return expenseDao.observeAll();
    }

    public LiveData<Double> observeTotalExpenses() {
        return expenseDao.observeTotalExpenses();
    }

    public LiveData<Double> observePeriodExpenses(String period) {
        return expenseDao.observePeriodExpenses(period);
    }

    public void addExpense(String propertyId, String category, String description,
                           double amount, String bearer, String tenantId, String date) {
        AppExecutors.DB.execute(() -> {
            long now = System.currentTimeMillis();
            String dateStr = (date != null && !date.isEmpty())
                    ? date
                    : new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(now));

            ExpenseEntity entity = new ExpenseEntity();
            entity.localId = "exp-" + UUID.randomUUID().toString().substring(0, 8);
            entity.propertyId = propertyId;
            entity.category = category;
            entity.description = description;
            entity.amount = amount;
            entity.bearer = bearer != null ? bearer : "landlord";
            entity.tenantId = tenantId;
            entity.date = dateStr;
            entity.createdAt = now;
            expenseDao.insert(entity);
        });
    }

    public void deleteExpense(ExpenseEntity expense) {
        AppExecutors.DB.execute(() -> expenseDao.delete(expense));
    }
}
