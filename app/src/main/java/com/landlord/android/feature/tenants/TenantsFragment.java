package com.landlord.android.feature.tenants;

import android.content.Intent;
import android.net.Uri;
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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.landlord.android.R;
import com.landlord.android.core.db.RentalAgreementEntity;
import com.landlord.android.core.db.TenantEntity;
import com.landlord.android.core.db.UnitEntity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TenantsFragment extends Fragment implements TenantAdapter.OnTenantActionListener {

    private TenantsViewModel viewModel;
    private TenantAdapter adapter;
    private List<TenantEntity> currentTenants;
    private List<UnitEntity> cachedUnits = new ArrayList<>();
    private List<RentalAgreementEntity> cachedAgreements = new ArrayList<>();
    private final Map<String, UnitEntity> unitMap = new HashMap<>();
    private final java.util.Set<String> tenantsWithDues = new java.util.HashSet<>();

    private String searchQuery = "";
    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tenants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TenantsViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.tenants_list);
        View emptyView = view.findViewById(R.id.tenants_empty);
        View filterEmptyView = view.findViewById(R.id.tenants_filter_empty);
        FloatingActionButton fab = view.findViewById(R.id.add_tenant_fab);
        MaterialButton btnEmptyRegister = view.findViewById(R.id.btn_empty_register_tenant);
        MaterialButton btnResetFilters = view.findViewById(R.id.btn_reset_tenant_filters);
        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.tenants_list_refresh);

        EditText etSearch = view.findViewById(R.id.et_search_tenants);
        View btnClearSearch = view.findViewById(R.id.btn_clear_search_tenants);
        com.google.android.material.chip.ChipGroup chipGroup = view.findViewById(R.id.chip_group_tenant_status);

        TextView tvActiveTenants = view.findViewById(R.id.tv_active_tenants);
        TextView tvRentRoll = view.findViewById(R.id.tv_monthly_rent_roll);
        TextView tvDeposits = view.findViewById(R.id.tv_deposits_held);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TenantAdapter(this);
        recyclerView.setAdapter(adapter);

        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener(() -> {
                recyclerView.scheduleLayoutAnimation();
                refreshLayout.setRefreshing(false);
            });
        }

        viewModel.getTenants().observe(getViewLifecycleOwner(), list -> {
            currentTenants = list;
            updateUI(recyclerView, emptyView, filterEmptyView, tvActiveTenants, tvRentRoll, tvDeposits);
        });

        viewModel.getAllUnits().observe(getViewLifecycleOwner(), units -> {
            if (units != null) {
                cachedUnits = units;
                unitMap.clear();
                for (UnitEntity u : units) {
                    unitMap.put(u.localId, u);
                }
                adapter.setUnits(unitMap);
                updateUI(recyclerView, emptyView, filterEmptyView, tvActiveTenants, tvRentRoll, tvDeposits);
            }
        });

        viewModel.getAllAgreements().observe(getViewLifecycleOwner(), agreements -> {
            if (agreements != null) {
                cachedAgreements = agreements;
                updateUI(recyclerView, emptyView, filterEmptyView, tvActiveTenants, tvRentRoll, tvDeposits);
            }
        });

        viewModel.getTenantMonthlyDues().observe(getViewLifecycleOwner(), dues -> {
            tenantsWithDues.clear();
            if (dues != null) {
                for (com.landlord.android.feature.payments.TenantDueDto due : dues) {
                    if (due.totalDue > due.totalPaid) {
                        tenantsWithDues.add(due.tenantId);
                    }
                }
            }
            updateUI(recyclerView, emptyView, filterEmptyView, tvActiveTenants, tvRentRoll, tvDeposits);
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
                    updateUI(recyclerView, emptyView, filterEmptyView, tvActiveTenants, tvRentRoll, tvDeposits);
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

        // Status chip filter
        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.contains(R.id.chip_tenant_active)) {
                    currentFilter = "active";
                } else if (checkedIds.contains(R.id.chip_tenant_vacated)) {
                    currentFilter = "vacated";
                } else if (checkedIds.contains(R.id.chip_tenant_with_dues)) {
                    currentFilter = "with_dues";
                } else {
                    currentFilter = "all";
                }
                updateUI(recyclerView, emptyView, filterEmptyView, tvActiveTenants, tvRentRoll, tvDeposits);
                recyclerView.scheduleLayoutAnimation();
            });
        }

        if (btnResetFilters != null) {
            btnResetFilters.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (etSearch != null) etSearch.setText("");
                if (chipGroup != null) chipGroup.check(R.id.chip_tenant_all);
                currentFilter = "all";
                updateUI(recyclerView, emptyView, filterEmptyView, tvActiveTenants, tvRentRoll, tvDeposits);
            });
        }

        if (btnEmptyRegister != null) {
            btnEmptyRegister.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showRegisterTenantDialog();
            });
        }

        if (fab != null) {
            fab.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showRegisterTenantDialog();
            });
        }
    }

    private void updateUI(RecyclerView recyclerView, View emptyView, View filterEmptyView,
                          TextView tvActiveTenants, TextView tvRentRoll, TextView tvDeposits) {
        if (currentTenants == null || currentTenants.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            if (tvActiveTenants != null) tvActiveTenants.setText("0");
            if (tvRentRoll != null) tvRentRoll.setText("৳0");
            if (tvDeposits != null) tvDeposits.setText("৳0");
            adapter.submitList(null);
            return;
        }

        // Base data is present
        if (emptyView != null) emptyView.setVisibility(View.GONE);

        // Filter current tenants
        List<TenantEntity> filtered = new ArrayList<>();
        String queryLower = searchQuery.toLowerCase(Locale.US);

        for (TenantEntity t : currentTenants) {
            // Apply Status Filter
            boolean matchesStatus = true;
            if ("active".equalsIgnoreCase(currentFilter)) {
                matchesStatus = "active".equalsIgnoreCase(t.status);
            } else if ("vacated".equalsIgnoreCase(currentFilter)) {
                matchesStatus = !"active".equalsIgnoreCase(t.status);
            } else if ("with_dues".equalsIgnoreCase(currentFilter)) {
                matchesStatus = tenantsWithDues.contains(t.localId);
            }

            if (!matchesStatus) continue;

            // Apply Search Query
            if (!queryLower.isEmpty()) {
                boolean matchesName = t.name != null && t.name.toLowerCase(Locale.US).contains(queryLower);
                boolean matchesPhone = t.phone != null && t.phone.toLowerCase(Locale.US).contains(queryLower);
                boolean matchesEmail = t.email != null && t.email.toLowerCase(Locale.US).contains(queryLower);
                boolean matchesUnit = false;
                if (t.unitId != null && unitMap.containsKey(t.unitId)) {
                    UnitEntity u = unitMap.get(t.unitId);
                    if (u != null && u.unitNumber != null && u.unitNumber.toLowerCase(Locale.US).contains(queryLower)) {
                        matchesUnit = true;
                    }
                }
                if (!matchesName && !matchesPhone && !matchesEmail && !matchesUnit) {
                    continue;
                }
            }

            filtered.add(t);
        }

        if (filtered.isEmpty()) {
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        adapter.submitList(filtered);

        // Recalculate metrics based on filtered results
        int activeCount = 0;
        double monthlyRoll = 0.0;
        java.util.Set<String> filteredTenantIds = new java.util.HashSet<>();

        for (TenantEntity t : filtered) {
            filteredTenantIds.add(t.localId);
            if ("active".equalsIgnoreCase(t.status)) {
                activeCount++;
                if (t.unitId != null && unitMap.containsKey(t.unitId)) {
                    UnitEntity u = unitMap.get(t.unitId);
                    if (u != null) {
                        monthlyRoll += u.rent;
                    }
                }
            }
        }

        double totalDeposits = 0.0;
        if (cachedAgreements != null) {
            for (RentalAgreementEntity a : cachedAgreements) {
                if (filteredTenantIds.contains(a.tenantId)) {
                    totalDeposits += a.deposit;
                }
            }
        }

        if (tvActiveTenants != null) {
            tvActiveTenants.setText(String.valueOf(activeCount));
        }
        if (tvRentRoll != null) {
            tvRentRoll.setText(String.format(Locale.US, "৳%,.0f", monthlyRoll));
        }
        if (tvDeposits != null) {
            tvDeposits.setText(String.format(Locale.US, "৳%,.0f", totalDeposits));
        }
    }

    @Override
    public void onTenantClick(TenantEntity tenant) {
        Bundle args = new Bundle();
        args.putString("tenantLocalId", tenant.localId);
        View view = getView();
        if (view != null) {
            Navigation.findNavController(view).navigate(R.id.action_tenants_to_detail, args);
        }
    }

    @Override
    public void onCallClick(TenantEntity tenant) {
        if (tenant.phone != null && !tenant.phone.isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tenant.phone));
            startActivity(callIntent);
        } else {
            Toast.makeText(requireContext(), "No phone number registered for " + tenant.name, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onChatClick(TenantEntity tenant) {
        View view = getView();
        if (view != null) {
            Navigation.findNavController(view).navigate(R.id.action_tenants_to_messages);
        }
    }

    private void showRegisterTenantDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_register_tenant, null);
        Spinner unitSpinner = dialogView.findViewById(R.id.input_unit);
        EditText inputName = dialogView.findViewById(R.id.input_name);
        EditText inputPhone = dialogView.findViewById(R.id.input_phone);
        EditText inputEmail = dialogView.findViewById(R.id.input_email);
        EditText inputNid = dialogView.findViewById(R.id.input_nid);
        EditText inputDeposit = dialogView.findViewById(R.id.input_deposit);

        List<String> unitLabels = new ArrayList<>();
        List<String> unitIds = new ArrayList<>();

        for (UnitEntity u : cachedUnits) {
            unitLabels.add("Unit " + u.unitNumber + " (" + u.status + ") - ৳" + String.format(Locale.US, "%,.0f", u.rent));
            unitIds.add(u.localId);
        }

        if (unitLabels.isEmpty()) {
            unitLabels.add("No units created yet");
            unitIds.add("");
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, unitLabels);
        unitSpinner.setAdapter(spinnerAdapter);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Register New Tenant")
                .setView(dialogView)
                .setPositiveButton("Register", (dialog, which) -> {
                    String name = inputName.getText().toString().trim();
                    String phone = inputPhone.getText().toString().trim();
                    String email = inputEmail.getText().toString().trim();
                    String nid = inputNid.getText().toString().trim();
                    String depositStr = inputDeposit.getText().toString().trim();

                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(requireContext(), "Name and phone required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int selectedPos = unitSpinner.getSelectedItemPosition();
                    String selectedUnitId = (selectedPos >= 0 && selectedPos < unitIds.size()) ? unitIds.get(selectedPos) : "";
                    double deposit = 0;
                    try {
                        deposit = Double.parseDouble(depositStr);
                    } catch (NumberFormatException ignored) {}

                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                    String terms = "Standard lease agreement for 12 months with 2 months notice.";

                    viewModel.registerTenant(name, phone, email, nid, selectedUnitId, today, terms, deposit);
                    Toast.makeText(requireContext(), "Tenant " + name + " registered", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
