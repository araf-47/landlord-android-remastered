package com.landlord.android.feature.payments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.landlord.android.R;
import com.landlord.android.core.db.TenantEntity;
import com.landlord.android.ui.widgets.OdometerAnimator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentsFragment extends Fragment {

    private PaymentsViewModel viewModel;
    private TenantDuesAdapter duesAdapter;
    private List<TenantEntity> cachedTenants = new ArrayList<>();
    private List<TenantDueDto> allDues = new ArrayList<>();
    private String duesSearchQuery = "";
    private String duesFilter = "all"; // "all", "overdue", "settled"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PaymentsViewModel.class);

        TextView statTotalDue = view.findViewById(R.id.stat_total_due);
        TextView statTotalCollected = view.findViewById(R.id.stat_total_collected);
        MaterialButton btnPaymentHistory = view.findViewById(R.id.btn_payment_history);
        RecyclerView recyclerView = view.findViewById(R.id.tenant_dues_list);
        View emptyView = view.findViewById(R.id.tenant_dues_empty);
        View filterEmptyView = view.findViewById(R.id.dues_filter_empty);
        ExtendedFloatingActionButton fab = view.findViewById(R.id.receive_payment_fab);

        EditText etSearch = view.findViewById(R.id.et_search_dues);
        View btnClearSearch = view.findViewById(R.id.btn_clear_search_dues);
        com.google.android.material.chip.ChipGroup chipGroup = view.findViewById(R.id.chip_group_dues_status);
        MaterialButton btnResetFilters = view.findViewById(R.id.btn_reset_dues_filters);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        duesAdapter = new TenantDuesAdapter(this::showRecordPaymentForTenantDue);
        recyclerView.setAdapter(duesAdapter);

        // Live observation of tenant dues at a single glance
        viewModel.getTenantMonthlyDues().observe(getViewLifecycleOwner(), dues -> {
            allDues = dues != null ? dues : new ArrayList<>();
            applyDuesFilter(recyclerView, emptyView, filterEmptyView);
        });

        // Search text watcher
        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    duesSearchQuery = s != null ? s.toString().trim() : "";
                    if (btnClearSearch != null) {
                        btnClearSearch.setVisibility(duesSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    applyDuesFilter(recyclerView, emptyView, filterEmptyView);
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        if (btnClearSearch != null && etSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                etSearch.setText("");
            });
        }

        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.contains(R.id.chip_dues_overdue)) {
                    duesFilter = "overdue";
                } else if (checkedIds.contains(R.id.chip_dues_settled)) {
                    duesFilter = "settled";
                } else {
                    duesFilter = "all";
                }
                applyDuesFilter(recyclerView, emptyView, filterEmptyView);
                recyclerView.scheduleLayoutAnimation();
            });
        }

        if (btnResetFilters != null) {
            btnResetFilters.setOnClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                if (etSearch != null) etSearch.setText("");
                if (chipGroup != null) chipGroup.check(R.id.chip_dues_all);
                duesFilter = "all";
                applyDuesFilter(recyclerView, emptyView, filterEmptyView);
            });
        }

        // Live observation of financial totals with smooth odometer animations
        viewModel.getTotalOutstanding().observe(getViewLifecycleOwner(), outstanding -> {
            if (statTotalDue != null) {
                double val = outstanding != null ? outstanding : 0.0;
                OdometerAnimator.animateCurrency(statTotalDue, 0, val);
            }
        });

        viewModel.getTotalCollected().observe(getViewLifecycleOwner(), collected -> {
            if (statTotalCollected != null) {
                double val = collected != null ? collected : 0.0;
                OdometerAnimator.animateCurrency(statTotalCollected, 0, val);
            }
        });

        viewModel.getTenants().observe(getViewLifecycleOwner(), tenants -> {
            if (tenants != null) cachedTenants = tenants;
        });

        if (btnPaymentHistory != null) {
            btnPaymentHistory.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(R.id.nav_payment_history));
        }

        fab.setOnClickListener(v -> showGeneralRecordPaymentDialog());
    }

    private void applyDuesFilter(RecyclerView recyclerView, View emptyView, View filterEmptyView) {
        if (allDues == null || allDues.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            duesAdapter.submitList(null);
            return;
        }

        if (emptyView != null) emptyView.setVisibility(View.GONE);

        List<TenantDueDto> filtered = new ArrayList<>();
        String q = duesSearchQuery.toLowerCase(Locale.US);

        for (TenantDueDto due : allDues) {
            if ("overdue".equals(duesFilter) && due.totalDue <= due.totalPaid) {
                continue;
            }
            if ("settled".equals(duesFilter) && due.totalDue > due.totalPaid) {
                continue;
            }

            if (!q.isEmpty()) {
                boolean matchName = due.tenantName != null && due.tenantName.toLowerCase(Locale.US).contains(q);
                boolean matchUnit = due.unitNumber != null && due.unitNumber.toLowerCase(Locale.US).contains(q);
                boolean matchProp = due.propertyName != null && due.propertyName.toLowerCase(Locale.US).contains(q);
                if (!matchName && !matchUnit && !matchProp) {
                    continue;
                }
            }

            filtered.add(due);
        }

        if (filtered.isEmpty()) {
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        duesAdapter.submitList(filtered);
    }

    private void showRecordPaymentForTenantDue(TenantDueDto due) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_receive_payment, null);
        AutoCompleteTextView inputTenant = dialogView.findViewById(R.id.input_tenant);
        AutoCompleteTextView inputMethod = dialogView.findViewById(R.id.input_method);
        EditText inputAmount = dialogView.findViewById(R.id.input_amount);
        TextView totalDueText = dialogView.findViewById(R.id.receive_total_due);

        double suggestAmount = due.totalDue > 0 ? due.totalDue : due.monthlyRent;
        inputAmount.setText(String.format(Locale.US, "%.0f", suggestAmount));
        totalDueText.setVisibility(View.VISIBLE);
        totalDueText.setText("Current Outstanding Due: ৳" + String.format(Locale.US, "%,.2f", due.totalDue));

        inputTenant.setText(due.tenantName + " (" + due.unitNumber + ")");
        inputTenant.setEnabled(false);

        String[] methods = new String[]{"bKash", "Nagad", "Bank Transfer", "Cash"};
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, methods);
        inputMethod.setAdapter(methodAdapter);
        inputMethod.setText(methods[0], false);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Collect Rent — " + due.tenantName)
                .setView(dialogView)
                .setPositiveButton("Confirm Payment", (dialog, which) -> {
                    String amtStr = inputAmount.getText().toString().trim();
                    String method = inputMethod.getText().toString().trim();
                    if (!amtStr.isEmpty()) {
                        double amt = Double.parseDouble(amtStr);
                        viewModel.recordPayment(due.tenantId, "", amt, method, method + " Payment");
                        Toast.makeText(requireContext(), "Payment of ৳" + amt + " recorded for " + due.tenantName, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showGeneralRecordPaymentDialog() {
        if (cachedTenants.isEmpty()) {
            Toast.makeText(requireContext(), "No tenants found to record payment for", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_receive_payment, null);
        AutoCompleteTextView inputTenant = dialogView.findViewById(R.id.input_tenant);
        AutoCompleteTextView inputMethod = dialogView.findViewById(R.id.input_method);
        EditText inputAmount = dialogView.findViewById(R.id.input_amount);

        List<String> names = new ArrayList<>();
        for (TenantEntity t : cachedTenants) {
            names.add(t.name);
        }
        ArrayAdapter<String> tenantAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names);
        inputTenant.setAdapter(tenantAdapter);
        inputTenant.setText(names.get(0), false);

        String[] methods = new String[]{"bKash", "Nagad", "Bank Transfer", "Cash"};
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, methods);
        inputMethod.setAdapter(methodAdapter);
        inputMethod.setText(methods[0], false);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Record Rent Payment")
                .setView(dialogView)
                .setPositiveButton("Save Payment", (dialog, which) -> {
                    String amtStr = inputAmount.getText().toString().trim();
                    String method = inputMethod.getText().toString().trim();
                    int pos = names.indexOf(inputTenant.getText().toString());
                    String tenantId = pos >= 0 ? cachedTenants.get(pos).localId : "";

                    if (!amtStr.isEmpty() && !tenantId.isEmpty()) {
                        double amt = Double.parseDouble(amtStr);
                        viewModel.recordPayment(tenantId, "", amt, method, method + " Payment");
                        Toast.makeText(requireContext(), "Payment recorded successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
