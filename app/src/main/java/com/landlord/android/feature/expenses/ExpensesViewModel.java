package com.landlord.android.feature.expenses;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.db.ExpenseEntity;
import com.landlord.android.core.db.PropertyEntity;
import com.landlord.android.feature.properties.PropertyRepository;
import java.util.List;

public class ExpensesViewModel extends AndroidViewModel {

    private final ExpenseRepository expenseRepo;
    private final PropertyRepository propertyRepo;

    public ExpensesViewModel(@NonNull Application application) {
        super(application);
        this.expenseRepo = new ExpenseRepository(application);
        this.propertyRepo = new PropertyRepository(application);
    }

    public LiveData<List<ExpenseEntity>> getExpenses() {
        return expenseRepo.observeExpenses();
    }

    public LiveData<Double> getTotalExpenses() {
        return expenseRepo.observeTotalExpenses();
    }

    public LiveData<List<PropertyEntity>> getProperties() {
        return propertyRepo.observeProperties();
    }

    public void addExpense(String propertyId, String category, String description, double amount, String bearer, String date) {
        expenseRepo.addExpense(propertyId, category, description, amount, bearer, null, date);
    }

    public void deleteExpense(ExpenseEntity expense) {
        expenseRepo.deleteExpense(expense);
    }
}
