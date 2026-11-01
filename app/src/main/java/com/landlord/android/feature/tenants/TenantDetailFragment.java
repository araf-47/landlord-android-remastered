package com.landlord.android.feature.tenants;

import android.content.Intent;
import android.net.Uri;
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
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.landlord.android.R;
import com.landlord.android.core.db.RentalAgreementEntity;
import com.landlord.android.ui.widgets.StatusPillHelper;
import java.util.Locale;

public class TenantDetailFragment extends Fragment {

    private TenantDetailViewModel viewModel;
    private String tenantId;
    private RentalAgreementEntity currentAgreement;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tenant_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tenantId = getArguments() != null ? getArguments().getString("tenantLocalId") : "";
        viewModel = new ViewModelProvider(this).get(TenantDetailViewModel.class);

        TextView tvName = view.findViewById(R.id.detail_tenant_name);
        TextView tvPhone = view.findViewById(R.id.detail_tenant_phone);
        TextView tvEmail = view.findViewById(R.id.detail_tenant_email);
        TextView tvNid = view.findViewById(R.id.detail_tenant_nid);
        TextView tvStatus = view.findViewById(R.id.detail_tenant_status);
        TextView tvStartDate = view.findViewById(R.id.detail_agreement_start);
        TextView tvDeposit = view.findViewById(R.id.detail_agreement_deposit);
        TextView tvTerms = view.findViewById(R.id.detail_agreement_terms);

        MaterialButton btnCall = view.findViewById(R.id.btn_call_tenant);
        MaterialButton btnEditTerms = view.findViewById(R.id.btn_edit_terms);
        MaterialButton btnMoveOut = view.findViewById(R.id.btn_move_out);

        viewModel.getTenant(tenantId).observe(getViewLifecycleOwner(), tenant -> {
            if (tenant != null) {
                if (tvName != null) tvName.setText(tenant.name);
                if (tvPhone != null) tvPhone.setText(tenant.phone);
                if (tvEmail != null) tvEmail.setText(tenant.email != null && !tenant.email.isEmpty() ? tenant.email : "Not provided");
                if (tvNid != null) tvNid.setText("NID: " + tenant.nationalId);
                if (tvStatus != null) {
                    if ("active".equalsIgnoreCase(tenant.status)) {
                        StatusPillHelper.setSuccess(tvStatus, "ACTIVE");
                    } else {
                        StatusPillHelper.setDanger(tvStatus, "INACTIVE");
                    }
                }

                if (btnCall != null) {
                    btnCall.setOnClickListener(v -> {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tenant.phone));
                        startActivity(callIntent);
                    });
                }
            }
        });

        viewModel.getAgreement(tenantId).observe(getViewLifecycleOwner(), agreement -> {
            this.currentAgreement = agreement;
            if (agreement != null) {
                if (tvStartDate != null) tvStartDate.setText("Start Date: " + agreement.startDate);
                if (tvDeposit != null) tvDeposit.setText("Security Deposit: ৳" + String.format(Locale.US, "%,.0f", agreement.deposit));
                if (tvTerms != null) tvTerms.setText(agreement.terms);
            }
        });

        if (btnEditTerms != null) {
            btnEditTerms.setOnClickListener(v -> {
                if (currentAgreement == null) return;
                View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_terms, null);
                EditText inputTerms = dialogView.findViewById(R.id.input_terms);
                inputTerms.setText(currentAgreement.terms);

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Edit Lease Terms")
                        .setView(dialogView)
                        .setPositiveButton("Save", (dialog, which) -> {
                            currentAgreement.terms = inputTerms.getText().toString().trim();
                            viewModel.updateAgreement(currentAgreement);
                            Toast.makeText(requireContext(), "Agreement updated", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        if (btnMoveOut != null) {
            btnMoveOut.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Confirm Tenant Move-Out")
                        .setMessage("This will mark the tenant as inactive and restore their assigned unit back to vacant status. Proceed?")
                        .setPositiveButton("Move Out", (dialog, which) -> {
                            viewModel.moveOutTenant(tenantId);
                            Toast.makeText(requireContext(), "Tenant moved out. Unit is now vacant.", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(view).popBackStack();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }
}
