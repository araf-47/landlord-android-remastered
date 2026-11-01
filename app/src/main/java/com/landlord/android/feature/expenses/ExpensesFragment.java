package com.landlord.android.feature.expenses;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.landlord.android.R;
import com.landlord.android.core.db.ExpenseEntity;
import com.landlord.android.core.db.PropertyEntity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpensesFragment extends Fragment {

    private ExpensesViewModel viewModel;
    private ExpenseAdapter adapter;
    private List<ExpenseEntity> allExpenses = new ArrayList<>();
    private List<PropertyEntity> cachedProperties = new ArrayList<>();
    private String currentCategoryFilter = "all";
    private String currentPeriodFilter = "all";
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expenses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ExpensesViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.expenses_list);
        View emptyView = view.findViewById(R.id.expenses_empty);
        View filterEmptyView = view.findViewById(R.id.expenses_filter_empty);
        FloatingActionButton fab = view.findViewById(R.id.add_expense_fab);
        MaterialButton btnEmptyAdd = view.findViewById(R.id.btn_empty_add_expense);
        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.expenses_list_refresh);
        ChipGroup chipGroupCategory = view.findViewById(R.id.chip_group_expense_category);
        ChipGroup chipGroupPeriod = view.findViewById(R.id.chip_group_expense_period);
        MaterialButton btnReset = view.findViewById(R.id.btn_reset_expense_filters);

        EditText etSearch = view.findViewById(R.id.et_search_expenses);
        View btnClearSearch = view.findViewById(R.id.btn_clear_search_expenses);

        TextView tvTotalExpenses = view.findViewById(R.id.tv_total_expenses);
        TextView tvEntriesCount = view.findViewById(R.id.tv_expense_entries_count);
        TextView tvTopCategory = view.findViewById(R.id.tv_top_category);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ExpenseAdapter(expense -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Expense: " + expense.category)
                    .setMessage(expense.description + "\nAmount: ৳" + String.format(Locale.US, "%,.0f", expense.amount) + "\nDate: " + expense.date)
                    .setNegativeButton("Close", null)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteExpense(expense);
                        Toast.makeText(requireContext(), "Expense deleted", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });
        recyclerView.setAdapter(adapter);

        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener(() -> {
                recyclerView.scheduleLayoutAnimation();
                refreshLayout.setRefreshing(false);
            });
        }

        viewModel.getExpenses().observe(getViewLifecycleOwner(), list -> {
            allExpenses = list != null ? list : new ArrayList<>();
            applyFilterAndMetrics(recyclerView, emptyView, filterEmptyView, tvTotalExpenses, tvEntriesCount, tvTopCategory);
        });

        viewModel.getProperties().observe(getViewLifecycleOwner(), properties -> {
            if (properties != null) cachedProperties = properties;
        });

        // Search text watcher
        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s != null ? s.toString().trim() : "";
                    if (btnClearSearch != null) {
                        btnClearSearch.setVisibility(searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    applyFilterAndMetrics(recyclerView, emptyView, filterEmptyView, tvTotalExpenses, tvEntriesCount, tvTopCategory);
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        if (btnClearSearch != null && etSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                etSearch.setText("");
            });
        }

        if (chipGroupPeriod != null) {
            chipGroupPeriod.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.contains(R.id.chip_expense_period_this_month)) {
                    currentPeriodFilter = "this_month";
                } else if (checkedIds.contains(R.id.chip_expense_period_last_month)) {
                    currentPeriodFilter = "last_month";
                } else {
                    currentPeriodFilter = "all";
                }
                applyFilterAndMetrics(recyclerView, emptyView, filterEmptyView, tvTotalExpenses, tvEntriesCount, tvTopCategory);
                recyclerView.scheduleLayoutAnimation();
            });
        }

        if (chipGroupCategory != null) {
            chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.contains(R.id.chip_expense_utility)) {
                    currentCategoryFilter = "utility";
                } else if (checkedIds.contains(R.id.chip_expense_repair)) {
                    currentCategoryFilter = "repair";
                } else if (checkedIds.contains(R.id.chip_expense_tax)) {
                    currentCategoryFilter = "tax";
                } else {
                    currentCategoryFilter = "all";
                }
                applyFilterAndMetrics(recyclerView, emptyView, filterEmptyView, tvTotalExpenses, tvEntriesCount, tvTopCategory);
                recyclerView.scheduleLayoutAnimation();
            });
        }

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (etSearch != null) etSearch.setText("");
                if (chipGroupPeriod != null) chipGroupPeriod.check(R.id.chip_expense_period_all);
                if (chipGroupCategory != null) chipGroupCategory.check(R.id.chip_expense_all);
                currentPeriodFilter = "all";
                currentCategoryFilter = "all";
                applyFilterAndMetrics(recyclerView, emptyView, filterEmptyView, tvTotalExpenses, tvEntriesCount, tvTopCategory);
            });
        }

        if (btnEmptyAdd != null) {
            btnEmptyAdd.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showAddExpenseDialog();
            });
        }

        if (fab != null) {
            fab.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showAddExpenseDialog();
            });
        }
    }

    private void applyFilterAndMetrics(RecyclerView recyclerView, View emptyView, View filterEmptyView,
                                       TextView tvTotalExpenses, TextView tvEntriesCount, TextView tvTopCategory) {
        if (allExpenses == null || allExpenses.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            if (tvTotalExpenses != null) tvTotalExpenses.setText("৳0");
            if (tvEntriesCount != null) tvEntriesCount.setText("0");
            if (tvTopCategory != null) tvTopCategory.setText("None");
            adapter.submitList(null);
            return;
        }

        if (emptyView != null) emptyView.setVisibility(View.GONE);

        java.text.SimpleDateFormat ymFormat = new java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String thisMonthPrefix = ymFormat.format(cal.getTime());
        cal.add(java.util.Calendar.MONTH, -1);
        String lastMonthPrefix = ymFormat.format(cal.getTime());

        double filteredSum = 0.0;
        Map<String, Double> categorySums = new HashMap<>();
        List<ExpenseEntity> filteredList = new ArrayList<>();
        String q = searchQuery.toLowerCase(Locale.US);

        for (ExpenseEntity e : allExpenses) {
            // Period filter
            if ("this_month".equals(currentPeriodFilter) && (e.date == null || !e.date.startsWith(thisMonthPrefix))) {
                continue;
            }
            if ("last_month".equals(currentPeriodFilter) && (e.date == null || !e.date.startsWith(lastMonthPrefix))) {
                continue;
            }

            // Category filter
            String cat = e.category != null ? e.category : "General";
            String catLower = cat.toLowerCase(Locale.US);
            if ("utility".equals(currentCategoryFilter) && !(catLower.contains("util") || catLower.contains("elect") || catLower.contains("water") || catLower.contains("gas"))) {
                continue;
            }
            if ("repair".equals(currentCategoryFilter) && !(catLower.contains("repair") || catLower.contains("maint") || catLower.contains("plumb"))) {
                continue;
            }
            if ("tax".equals(currentCategoryFilter) && !(catLower.contains("tax") || catLower.contains("fee") || catLower.contains("govt"))) {
                continue;
            }

            // Search query
            if (!q.isEmpty()) {
                boolean matchDesc = e.description != null && e.description.toLowerCase(Locale.US).contains(q);
                boolean matchCat = catLower.contains(q);
                boolean matchAmount = String.format(Locale.US, "%.0f", e.amount).contains(q);
                boolean matchDate = e.date != null && e.date.contains(q);
                if (!matchDesc && !matchCat && !matchAmount && !matchDate) {
                    continue;
                }
            }

            filteredList.add(e);
            filteredSum += e.amount;
            categorySums.put(cat, categorySums.getOrDefault(cat, 0.0) + e.amount);
        }

        if (filteredList.isEmpty()) {
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        adapter.submitList(filteredList);

        if (tvTotalExpenses != null) {
            tvTotalExpenses.setText(String.format(Locale.US, "৳%,.0f", filteredSum));
        }
        if (tvEntriesCount != null) {
            tvEntriesCount.setText(String.valueOf(filteredList.size()));
        }

        String topCatName = "None";
        double maxCatSum = 0;
        for (Map.Entry<String, Double> entry : categorySums.entrySet()) {
            if (entry.getValue() > maxCatSum) {
                maxCatSum = entry.getValue();
                topCatName = entry.getKey();
            }
        }
        if (tvTopCategory != null) {
            tvTopCategory.setText(topCatName);
        }
    }

    private void showAddExpenseDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_expense, null);
        Spinner propertySpinner = dialogView.findViewById(R.id.input_property);
        EditText inputCategory = dialogView.findViewById(R.id.input_category);
        EditText inputDescription = dialogView.findViewById(R.id.input_description);
        EditText inputAmount = dialogView.findViewById(R.id.input_amount);

        List<String> propertyNames = new ArrayList<>();
        List<String> propertyIds = new ArrayList<>();
        for (PropertyEntity p : cachedProperties) {
            propertyNames.add(p.name);
            propertyIds.add(p.localId);
        }
        if (propertyNames.isEmpty()) {
            propertyNames.add("Default Property");
            propertyIds.add("");
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, propertyNames);
        propertySpinner.setAdapter(spinnerAdapter);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log New Expense")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String category = inputCategory.getText().toString().trim();
                    String desc = inputDescription.getText().toString().trim();
                    String amtStr = inputAmount.getText().toString().trim();

                    if (!category.isEmpty() && !amtStr.isEmpty()) {
                        int pos = propertySpinner.getSelectedItemPosition();
                        String propId = pos >= 0 && pos < propertyIds.size() ? propertyIds.get(pos) : "";
                        double amt = 0.0;
                        try {
                            amt = Double.parseDouble(amtStr);
                        } catch (NumberFormatException ignored) {}

                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

                        viewModel.addExpense(propId, category, desc.isEmpty() ? category : desc, amt, "landlord", date);
                        Toast.makeText(requireContext(), "Expense logged successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Category and amount are required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
