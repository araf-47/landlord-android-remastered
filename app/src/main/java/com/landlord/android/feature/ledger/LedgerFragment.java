package com.landlord.android.feature.ledger;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.landlord.android.R;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LedgerFragment extends Fragment {

    private LedgerViewModel viewModel;
    private LedgerAdapter adapter;
    private List<LedgerEntry> allEntries = new ArrayList<>();

    private String searchQuery = "";
    private String periodMode = "all"; // "all", "this_month", "last_month", or "custom"
    private String customMonthPrefix = ""; // e.g. "2026-10"
    private String customMonthDisplay = "";
    private String typeFilter = "all"; // "all", "income", "expense"

    private RecyclerView recyclerView;
    private View emptyView;
    private View filterEmptyView;
    private TextView tvInflow;
    private TextView tvOutflow;
    private TextView tvNet;
    private TextView tvPeriodLabel;
    private Chip chipSelectMonth;

    private final SimpleDateFormat isoMonthFormat = new SimpleDateFormat("yyyy-MM", Locale.US);
    private final SimpleDateFormat displayMonthFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ledger, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LedgerViewModel.class);

        recyclerView = view.findViewById(R.id.ledger_list);
        emptyView = view.findViewById(R.id.ledger_empty);
        filterEmptyView = view.findViewById(R.id.ledger_filter_empty);
        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.ledger_list_refresh);

        tvInflow = view.findViewById(R.id.tv_ledger_inflow);
        tvOutflow = view.findViewById(R.id.tv_ledger_outflow);
        tvNet = view.findViewById(R.id.tv_ledger_net);
        tvPeriodLabel = view.findViewById(R.id.tv_ledger_period_label);

        EditText etSearch = view.findViewById(R.id.et_search_ledger);
        View btnClearSearch = view.findViewById(R.id.btn_clear_search_ledger);
        ChipGroup chipGroupPeriod = view.findViewById(R.id.chip_group_ledger_period);
        ChipGroup chipGroupType = view.findViewById(R.id.chip_group_ledger_type);
        chipSelectMonth = view.findViewById(R.id.chip_ledger_period_select);
        MaterialButton btnResetFilters = view.findViewById(R.id.btn_reset_ledger_filters);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new LedgerAdapter();
        recyclerView.setAdapter(adapter);

        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener(() -> {
                recyclerView.scheduleLayoutAnimation();
                refreshLayout.setRefreshing(false);
            });
        }

        viewModel.getLedgerEntries().observe(getViewLifecycleOwner(), entries -> {
            allEntries = entries != null ? entries : new ArrayList<>();
            applyFilterAndMetrics();
        });

        // Search text watcher
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s != null ? s.toString().trim() : "";
                    if (btnClearSearch != null) {
                        btnClearSearch.setVisibility(searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    applyFilterAndMetrics();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnClearSearch != null && etSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                etSearch.setText("");
            });
        }

        // Period chip filter
        if (chipGroupPeriod != null) {
            chipGroupPeriod.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.contains(R.id.chip_ledger_period_this_month)) {
                    periodMode = "this_month";
                } else if (checkedIds.contains(R.id.chip_ledger_period_last_month)) {
                    periodMode = "last_month";
                } else if (checkedIds.contains(R.id.chip_ledger_period_select)) {
                    periodMode = "custom";
                    showMonthPickerDialog();
                } else {
                    periodMode = "all";
                    if (chipSelectMonth != null) chipSelectMonth.setText("Select Month 📅");
                }
                applyFilterAndMetrics();
                recyclerView.scheduleLayoutAnimation();
            });
        }

        // Type chip filter
        if (chipGroupType != null) {
            chipGroupType.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.contains(R.id.chip_ledger_type_income)) {
                    typeFilter = "income";
                } else if (checkedIds.contains(R.id.chip_ledger_type_expense)) {
                    typeFilter = "expense";
                } else {
                    typeFilter = "all";
                }
                applyFilterAndMetrics();
                recyclerView.scheduleLayoutAnimation();
            });
        }

        if (btnResetFilters != null) {
            btnResetFilters.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (etSearch != null) etSearch.setText("");
                if (chipGroupPeriod != null) chipGroupPeriod.check(R.id.chip_ledger_period_all);
                if (chipGroupType != null) chipGroupType.check(R.id.chip_ledger_type_all);
                if (chipSelectMonth != null) chipSelectMonth.setText("Select Month 📅");
                periodMode = "all";
                typeFilter = "all";
                applyFilterAndMetrics();
            });
        }
    }

    private void showMonthPickerDialog() {
        // Collect available months from dataset, plus recent 3 calendar months
        Set<String> monthPrefixes = new LinkedHashSet<>();
        Calendar cal = Calendar.getInstance();
        monthPrefixes.add(isoMonthFormat.format(cal.getTime()));
        cal.add(Calendar.MONTH, -1);
        monthPrefixes.add(isoMonthFormat.format(cal.getTime()));
        cal.add(Calendar.MONTH, -1);
        monthPrefixes.add(isoMonthFormat.format(cal.getTime()));

        for (LedgerEntry e : allEntries) {
            if (e.date != null && e.date.length() >= 7) {
                monthPrefixes.add(e.date.substring(0, 7));
            }
        }

        List<String> sortedPrefixes = new ArrayList<>(monthPrefixes);
        Collections.sort(sortedPrefixes, Collections.reverseOrder());

        String[] displayNames = new String[sortedPrefixes.size()];
        for (int i = 0; i < sortedPrefixes.size(); i++) {
            String p = sortedPrefixes.get(i);
            try {
                Date d = isoMonthFormat.parse(p);
                displayNames[i] = d != null ? displayMonthFormat.format(d) : p;
            } catch (ParseException ex) {
                displayNames[i] = p;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Transaction Month")
                .setItems(displayNames, (dialog, which) -> {
                    customMonthPrefix = sortedPrefixes.get(which);
                    customMonthDisplay = displayNames[which];
                    if (chipSelectMonth != null) {
                        chipSelectMonth.setText("📅 " + customMonthDisplay);
                    }
                    applyFilterAndMetrics();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (customMonthPrefix.isEmpty() && chipSelectMonth != null) {
                        ChipGroup group = requireView().findViewById(R.id.chip_group_ledger_period);
                        if (group != null) group.check(R.id.chip_ledger_period_all);
                    }
                })
                .show();
    }

    private void applyFilterAndMetrics() {
        if (allEntries == null || allEntries.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.GONE);
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            if (tvInflow != null) tvInflow.setText("+৳0");
            if (tvOutflow != null) tvOutflow.setText("-৳0");
            if (tvNet != null) tvNet.setText("৳0");
            if (tvPeriodLabel != null) tvPeriodLabel.setText("All Time");
            adapter.submitList(null);
            return;
        }

        if (emptyView != null) emptyView.setVisibility(View.GONE);

        // Determine month prefix filter
        String activeMonthPrefix = null;
        String activePeriodTitle = "All Time";

        Calendar cal = Calendar.getInstance();
        if ("this_month".equals(periodMode)) {
            activeMonthPrefix = isoMonthFormat.format(cal.getTime());
            activePeriodTitle = displayMonthFormat.format(cal.getTime());
        } else if ("last_month".equals(periodMode)) {
            cal.add(Calendar.MONTH, -1);
            activeMonthPrefix = isoMonthFormat.format(cal.getTime());
            activePeriodTitle = displayMonthFormat.format(cal.getTime());
        } else if ("custom".equals(periodMode) && !customMonthPrefix.isEmpty()) {
            activeMonthPrefix = customMonthPrefix;
            activePeriodTitle = !customMonthDisplay.isEmpty() ? customMonthDisplay : customMonthPrefix;
        }

        if (tvPeriodLabel != null) {
            tvPeriodLabel.setText(activePeriodTitle);
        }

        List<LedgerEntry> filtered = new ArrayList<>();
        String queryLower = searchQuery.toLowerCase(Locale.US);

        double totalInflow = 0.0;
        double totalOutflow = 0.0;

        for (LedgerEntry entry : allEntries) {
            // Check month period filter
            if (activeMonthPrefix != null) {
                if (entry.date == null || !entry.date.startsWith(activeMonthPrefix)) {
                    continue;
                }
            }

            // Check transaction type filter
            if ("income".equals(typeFilter) && !entry.isIncome) {
                continue;
            }
            if ("expense".equals(typeFilter) && entry.isIncome) {
                continue;
            }

            // Check search query
            if (!queryLower.isEmpty()) {
                boolean matchesDesc = entry.description != null && entry.description.toLowerCase(Locale.US).contains(queryLower);
                boolean matchesAmount = String.format(Locale.US, "%.0f", entry.amount).contains(queryLower);
                boolean matchesDate = entry.date != null && entry.date.contains(queryLower);
                if (!matchesDesc && !matchesAmount && !matchesDate) {
                    continue;
                }
            }

            filtered.add(entry);

            if (entry.isIncome) {
                totalInflow += entry.amount;
            } else {
                totalOutflow += entry.amount;
            }
        }

        if (filtered.isEmpty()) {
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.VISIBLE);
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        } else {
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.GONE);
            if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
        }

        adapter.submitList(filtered);

        // Update financial ledger metrics
        double netSurplus = totalInflow - totalOutflow;
        if (tvInflow != null) {
            tvInflow.setText(String.format(Locale.US, "+৳%,.0f", totalInflow));
        }
        if (tvOutflow != null) {
            tvOutflow.setText(String.format(Locale.US, "-৳%,.0f", totalOutflow));
        }
        if (tvNet != null) {
            String prefix = netSurplus >= 0 ? "+৳" : "-৳";
            tvNet.setText(String.format(Locale.US, prefix + "%,.0f", Math.abs(netSurplus)));
            tvNet.setTextColor(requireContext().getColor(netSurplus >= 0 ? R.color.color_success : R.color.color_danger));
        }
    }
}
