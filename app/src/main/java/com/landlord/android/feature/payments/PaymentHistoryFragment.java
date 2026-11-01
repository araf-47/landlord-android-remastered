package com.landlord.android.feature.payments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.landlord.android.R;
import com.landlord.android.core.common.PdfReceiptGenerator;
import com.landlord.android.core.db.PaymentEntity;
import com.landlord.android.core.db.TenantEntity;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentHistoryFragment extends Fragment {

    private PaymentsViewModel viewModel;
    private PaymentHistoryAdapter adapter;
    private final Map<String, String> tenantNameMap = new HashMap<>();

    private List<PaymentEntity> allPayments = new ArrayList<>();
    private String searchQuery = "";
    private String filterMode = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PaymentsViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.payment_history_list);
        View emptyView = view.findViewById(R.id.payment_history_empty);
        View filterEmptyView = view.findViewById(R.id.payment_history_filter_empty);
        androidx.swiperefreshlayout.widget.SwipeRefreshLayout refreshLayout = view.findViewById(R.id.payment_history_list_refresh);

        EditText etSearch = view.findViewById(R.id.et_search_payments);
        View btnClearSearch = view.findViewById(R.id.btn_clear_search_payments);
        com.google.android.material.chip.ChipGroup chipGroup = view.findViewById(R.id.chip_group_payment_period);
        com.google.android.material.button.MaterialButton btnReset = view.findViewById(R.id.btn_reset_payment_filters);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PaymentHistoryAdapter(this::showReceiptDialog);
        recyclerView.setAdapter(adapter);

        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener(() -> {
                recyclerView.scheduleLayoutAnimation();
                refreshLayout.setRefreshing(false);
            });
        }

        viewModel.getTenants().observe(getViewLifecycleOwner(), tenants -> {
            if (tenants != null) {
                tenantNameMap.clear();
                for (TenantEntity t : tenants) {
                    tenantNameMap.put(t.localId, t.name);
                }
                applyFilter(recyclerView, emptyView, filterEmptyView);
            }
        });

        viewModel.getPayments().observe(getViewLifecycleOwner(), payments -> {
            allPayments = payments != null ? payments : new ArrayList<>();
            applyFilter(recyclerView, emptyView, filterEmptyView);
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
                    applyFilter(recyclerView, emptyView, filterEmptyView);
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
                if (checkedIds.contains(R.id.chip_payment_this_month)) {
                    filterMode = "this_month";
                } else if (checkedIds.contains(R.id.chip_payment_last_month)) {
                    filterMode = "last_month";
                } else if (checkedIds.contains(R.id.chip_payment_bank)) {
                    filterMode = "bank";
                } else if (checkedIds.contains(R.id.chip_payment_cash)) {
                    filterMode = "cash";
                } else {
                    filterMode = "all";
                }
                applyFilter(recyclerView, emptyView, filterEmptyView);
                recyclerView.scheduleLayoutAnimation();
            });
        }

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                if (etSearch != null) etSearch.setText("");
                if (chipGroup != null) chipGroup.check(R.id.chip_payment_all);
                filterMode = "all";
                applyFilter(recyclerView, emptyView, filterEmptyView);
            });
        }
    }

    private void applyFilter(RecyclerView recyclerView, View emptyView, View filterEmptyView) {
        if (allPayments == null || allPayments.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            adapter.submitList(null);
            return;
        }

        if (emptyView != null) emptyView.setVisibility(View.GONE);

        java.text.SimpleDateFormat ymFormat = new java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String thisMonthPrefix = ymFormat.format(cal.getTime());
        cal.add(java.util.Calendar.MONTH, -1);
        String lastMonthPrefix = ymFormat.format(cal.getTime());

        List<PaymentEntity> filtered = new ArrayList<>();
        String q = searchQuery.toLowerCase(java.util.Locale.US);

        for (PaymentEntity p : allPayments) {
            if ("this_month".equals(filterMode) && (p.date == null || !p.date.startsWith(thisMonthPrefix))) {
                continue;
            }
            if ("last_month".equals(filterMode) && (p.date == null || !p.date.startsWith(lastMonthPrefix))) {
                continue;
            }
            if ("cash".equals(filterMode) && (p.method == null || !p.method.equalsIgnoreCase("cash"))) {
                continue;
            }
            if ("bank".equals(filterMode) && (p.method == null || p.method.equalsIgnoreCase("cash"))) {
                continue;
            }

            if (!q.isEmpty()) {
                String tenantName = tenantNameMap.get(p.tenantId);
                boolean matchTenant = tenantName != null && tenantName.toLowerCase(java.util.Locale.US).contains(q);
                boolean matchRef = p.transactionRef != null && p.transactionRef.toLowerCase(java.util.Locale.US).contains(q);
                boolean matchId = p.localId != null && p.localId.toLowerCase(java.util.Locale.US).contains(q);
                boolean matchDate = p.date != null && p.date.contains(q);
                if (!matchTenant && !matchRef && !matchId && !matchDate) {
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
    }

    private void showReceiptDialog(PaymentEntity payment) {
        String tenantName = tenantNameMap.get(payment.tenantId);
        if (tenantName == null) tenantName = "Tenant (" + payment.tenantId + ")";

        File pdfFile = PdfReceiptGenerator.generateReceiptPdf(
                requireContext(),
                payment,
                tenantName,
                "Assigned Unit"
        );

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Official Rent Receipt")
                .setMessage("PDF Receipt generated successfully for " + tenantName + "\n\nReceipt ID: " + payment.localId +
                        "\nAmount: ৳" + payment.amount +
                        "\nMethod: " + payment.method.toUpperCase() +
                        "\nDate: " + payment.date)
                .setPositiveButton("Open PDF", (dialog, which) -> {
                    PdfReceiptGenerator.openOrShareReceipt(requireContext(), pdfFile);
                })
                .setNegativeButton("Close", null)
                .show();
    }
}
