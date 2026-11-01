package com.landlord.android.feature.properties;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.landlord.android.R;

public class PropertyDetailFragment extends Fragment {

    private PropertyDetailViewModel viewModel;
    private UnitAdapter unitAdapter;
    private String propertyId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_property_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        propertyId = getArguments() != null ? getArguments().getString("propertyLocalId") : "";

        viewModel = new ViewModelProvider(this).get(PropertyDetailViewModel.class);

        TextView tvName = view.findViewById(R.id.detail_property_name);
        TextView tvAddress = view.findViewById(R.id.detail_property_address);
        TextView tvLocation = view.findViewById(R.id.detail_property_location);
        RecyclerView recyclerView = view.findViewById(R.id.units_list);
        FloatingActionButton fabAddUnit = view.findViewById(R.id.add_unit_fab);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        unitAdapter = new UnitAdapter(unit -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Unit " + unit.unitNumber)
                    .setMessage("Current status: " + unit.status.toUpperCase() + "\nRent: ৳" + unit.rent)
                    .setPositiveButton("Toggle Status", (dialog, which) -> {
                        viewModel.toggleUnitStatus(unit);
                        Toast.makeText(requireContext(), "Unit status updated", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Close", null)
                    .show();
        });
        recyclerView.setAdapter(unitAdapter);

        viewModel.getProperty(propertyId).observe(getViewLifecycleOwner(), property -> {
            if (property != null) {
                if (tvName != null) tvName.setText(property.name);
                if (tvAddress != null) tvAddress.setText(property.address);
                if (tvLocation != null) tvLocation.setText(property.area + ", " + property.district);
            }
        });

        viewModel.getUnits(propertyId).observe(getViewLifecycleOwner(), units -> {
            unitAdapter.submitList(units);
        });

        if (fabAddUnit != null) {
            fabAddUnit.setOnClickListener(v -> showAddUnitDialog());
        }
    }

    private void showAddUnitDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_unit, null);
        EditText inputNumber = dialogView.findViewById(R.id.input_unit_number);
        EditText inputRent = dialogView.findViewById(R.id.input_rent);
        EditText inputType = dialogView.findViewById(R.id.input_unit_type);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Unit to Property")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String number = inputNumber.getText().toString().trim();
                    String rentStr = inputRent.getText().toString().trim();
                    String type = inputType != null ? inputType.getText().toString().trim() : "apartment";

                    if (!number.isEmpty() && !rentStr.isEmpty()) {
                        double rent = Double.parseDouble(rentStr);
                        viewModel.addUnit(propertyId, number, rent, type.isEmpty() ? "apartment" : type, "[]");
                        Toast.makeText(requireContext(), "Unit added successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
