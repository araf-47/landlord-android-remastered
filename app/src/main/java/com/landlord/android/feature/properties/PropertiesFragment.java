package com.landlord.android.feature.properties;

import android.os.Bundle;
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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.landlord.android.R;
import com.landlord.android.core.db.PropertyEntity;
import com.landlord.android.core.db.UnitEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PropertiesFragment extends Fragment {

    private PropertiesViewModel viewModel;
    private PropertyAdapter adapter;
    private List<PropertyEntity> currentProperties;
    private List<UnitEntity> currentUnits;
    private String searchQuery = "";
    private String occupancyFilter = "all"; // "all", "vacancies", "occupied"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_properties, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PropertiesViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.properties_list);
        View emptyView = view.findViewById(R.id.properties_empty);
        View filterEmptyView = view.findViewById(R.id.properties_filter_empty);
        FloatingActionButton fab = view.findViewById(R.id.add_property_fab);
        MaterialButton btnEmptyAdd = view.findViewById(R.id.btn_empty_add_property);
        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.properties_refresh);
        com.google.android.material.chip.ChipGroup chipGroup = view.findViewById(R.id.chip_group_property_occupancy);
        MaterialButton btnReset = view.findViewById(R.id.btn_reset_property_filters);

        EditText etSearch = view.findViewById(R.id.et_search_properties);
        View btnClearSearch = view.findViewById(R.id.btn_clear_search_properties);

        TextView tvTotalProperties = view.findViewById(R.id.tv_total_properties);
        TextView tvTotalUnits = view.findViewById(R.id.tv_total_units);
        TextView tvPortfolioOccupancy = view.findViewById(R.id.tv_portfolio_occupancy);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PropertyAdapter(property -> {
            Bundle args = new Bundle();
            args.putString("propertyLocalId", property.localId);
            Navigation.findNavController(view).navigate(R.id.action_properties_to_detail, args);
        });
        recyclerView.setAdapter(adapter);

        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener(() -> {
                recyclerView.scheduleLayoutAnimation();
                refreshLayout.setRefreshing(false);
            });
        }

        viewModel.getProperties().observe(getViewLifecycleOwner(), list -> {
            currentProperties = list;
            updateUI(recyclerView, emptyView, filterEmptyView, tvTotalProperties, tvTotalUnits, tvPortfolioOccupancy);
        });

        viewModel.getAllUnits().observe(getViewLifecycleOwner(), units -> {
            currentUnits = units;
            updateUI(recyclerView, emptyView, filterEmptyView, tvTotalProperties, tvTotalUnits, tvPortfolioOccupancy);
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
                    updateUI(recyclerView, emptyView, filterEmptyView, tvTotalProperties, tvTotalUnits, tvPortfolioOccupancy);
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

        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.contains(R.id.chip_property_vacancies)) {
                    occupancyFilter = "vacancies";
                } else if (checkedIds.contains(R.id.chip_property_occupied)) {
                    occupancyFilter = "occupied";
                } else {
                    occupancyFilter = "all";
                }
                updateUI(recyclerView, emptyView, filterEmptyView, tvTotalProperties, tvTotalUnits, tvPortfolioOccupancy);
                recyclerView.scheduleLayoutAnimation();
            });
        }

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (etSearch != null) etSearch.setText("");
                if (chipGroup != null) chipGroup.check(R.id.chip_property_all);
                occupancyFilter = "all";
                updateUI(recyclerView, emptyView, filterEmptyView, tvTotalProperties, tvTotalUnits, tvPortfolioOccupancy);
            });
        }

        if (btnEmptyAdd != null) {
            btnEmptyAdd.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showAddPropertyDialog();
            });
        }

        if (fab != null) {
            fab.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showAddPropertyDialog();
            });
        }
    }

    private void updateUI(RecyclerView recyclerView, View emptyView, View filterEmptyView,
                          TextView tvTotalProperties, TextView tvTotalUnits, TextView tvPortfolioOccupancy) {
        if (currentProperties == null || currentProperties.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            if (tvTotalProperties != null) tvTotalProperties.setText("0");
            if (tvTotalUnits != null) tvTotalUnits.setText("0 Units");
            if (tvPortfolioOccupancy != null) tvPortfolioOccupancy.setText("● 0% Occupied");
            adapter.submitList(null);
            return;
        }

        if (emptyView != null) emptyView.setVisibility(View.GONE);

        // Precompute property unit counts and occupancy
        Map<String, Integer> propertyTotalUnits = new HashMap<>();
        Map<String, Integer> propertyOccupiedUnits = new HashMap<>();
        if (currentUnits != null) {
            for (UnitEntity u : currentUnits) {
                if (u.propertyId != null) {
                    propertyTotalUnits.put(u.propertyId, propertyTotalUnits.getOrDefault(u.propertyId, 0) + 1);
                    if ("occupied".equalsIgnoreCase(u.status)) {
                        propertyOccupiedUnits.put(u.propertyId, propertyOccupiedUnits.getOrDefault(u.propertyId, 0) + 1);
                    }
                }
            }
        }
        adapter.setUnitCounts(propertyTotalUnits);

        List<PropertyEntity> filtered = new ArrayList<>();
        String q = searchQuery.toLowerCase(Locale.US);

        for (PropertyEntity p : currentProperties) {
            int totalU = propertyTotalUnits.getOrDefault(p.localId, 0);
            int occU = propertyOccupiedUnits.getOrDefault(p.localId, 0);

            // Occupancy filter
            if ("vacancies".equals(occupancyFilter) && (totalU > 0 && occU >= totalU)) {
                continue;
            }
            if ("occupied".equals(occupancyFilter) && (totalU == 0 || occU < totalU)) {
                continue;
            }

            // Search query
            if (!q.isEmpty()) {
                boolean matchName = p.name != null && p.name.toLowerCase(Locale.US).contains(q);
                boolean matchAddress = p.address != null && p.address.toLowerCase(Locale.US).contains(q);
                boolean matchLocation = (p.district != null && p.district.toLowerCase(Locale.US).contains(q))
                        || (p.area != null && p.area.toLowerCase(Locale.US).contains(q));
                if (!matchName && !matchAddress && !matchLocation) {
                    continue;
                }
            }

            filtered.add(p);
        }

        if (filtered.isEmpty()) {
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        adapter.submitList(filtered);

        // Recalculate metrics for displayed properties
        int filteredUnitsCount = 0;
        int filteredOccupiedCount = 0;
        for (PropertyEntity p : filtered) {
            filteredUnitsCount += propertyTotalUnits.getOrDefault(p.localId, 0);
            filteredOccupiedCount += propertyOccupiedUnits.getOrDefault(p.localId, 0);
        }

        if (tvTotalProperties != null) {
            tvTotalProperties.setText(String.valueOf(filtered.size()));
        }
        if (tvTotalUnits != null) {
            tvTotalUnits.setText(filteredUnitsCount + " Units");
        }
        if (tvPortfolioOccupancy != null) {
            int pct = filteredUnitsCount > 0 ? Math.round((filteredOccupiedCount * 100.0f) / filteredUnitsCount) : 0;
            tvPortfolioOccupancy.setText("● " + pct + "% Occupied");
        }
    }

    private void showAddPropertyDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_property, null);
        EditText inputName = dialogView.findViewById(R.id.input_name);
        EditText inputAddress = dialogView.findViewById(R.id.input_address);
        EditText inputDistrict = dialogView.findViewById(R.id.input_district);
        EditText inputArea = dialogView.findViewById(R.id.input_area);
        EditText inputType = dialogView.findViewById(R.id.input_type);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add New Property")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = inputName.getText().toString().trim();
                    String address = inputAddress.getText().toString().trim();
                    String district = inputDistrict.getText().toString().trim();
                    String area = inputArea.getText().toString().trim();
                    String type = inputType.getText().toString().trim();

                    if (!name.isEmpty() && !address.isEmpty()) {
                        viewModel.addProperty(name, address,
                                district.isEmpty() ? "Dhaka" : district,
                                area.isEmpty() ? "Dhanmondi" : area,
                                type.isEmpty() ? "apartment" : type);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
