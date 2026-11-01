package com.landlord.android.feature.maintenance;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.landlord.android.R;
import com.landlord.android.core.db.MaintenanceTicketEntity;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceFragment extends Fragment {

    private MaintenanceViewModel viewModel;
    private MaintenanceAdapter adapter;
    private List<MaintenanceTicketEntity> allTickets = new ArrayList<>();
    private String currentFilter = "all";
    private String currentPriorityFilter = "all";
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maintenance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MaintenanceViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.tickets_list);
        View emptyView = view.findViewById(R.id.tickets_empty);
        View filterEmptyView = view.findViewById(R.id.maintenance_filter_empty);
        FloatingActionButton fab = view.findViewById(R.id.add_ticket_fab);
        MaterialButton btnEmptyAdd = view.findViewById(R.id.btn_empty_add_ticket);
        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.tickets_list_refresh);
        ChipGroup chipGroupStatus = view.findViewById(R.id.chip_group_status);
        ChipGroup chipGroupPriority = view.findViewById(R.id.chip_group_priority);
        MaterialButton btnReset = view.findViewById(R.id.btn_reset_maintenance_filters);

        EditText etSearch = view.findViewById(R.id.et_search_maintenance);
        View btnClearSearch = view.findViewById(R.id.btn_clear_search_maintenance);

        TextView tvPending = view.findViewById(R.id.tv_pending_count);
        TextView tvInProgress = view.findViewById(R.id.tv_in_progress_count);
        TextView tvResolved = view.findViewById(R.id.tv_resolved_count);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MaintenanceAdapter(this::showTicketActionDialog);
        recyclerView.setAdapter(adapter);

        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener(() -> {
                recyclerView.scheduleLayoutAnimation();
                refreshLayout.setRefreshing(false);
            });
        }

        viewModel.getTickets().observe(getViewLifecycleOwner(), tickets -> {
            allTickets = tickets != null ? tickets : new ArrayList<>();
            applyFilterAndMetrics(recyclerView, emptyView, filterEmptyView, tvPending, tvInProgress, tvResolved);
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
                    applyFilterAndMetrics(recyclerView, emptyView, filterEmptyView, tvPending, tvInProgress, tvResolved);
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

        if (chipGroupStatus != null) {
            chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.contains(R.id.chip_filter_pending)) {
                    currentFilter = "pending";
                } else if (checkedIds.contains(R.id.chip_filter_in_progress)) {
                    currentFilter = "in_progress";
                } else if (checkedIds.contains(R.id.chip_filter_resolved)) {
                    currentFilter = "resolved";
                } else {
                    currentFilter = "all";
                }
                applyFilterAndMetrics(recyclerView, emptyView, filterEmptyView, tvPending, tvInProgress, tvResolved);
                recyclerView.scheduleLayoutAnimation();
            });
        }

        if (chipGroupPriority != null) {
            chipGroupPriority.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.contains(R.id.chip_priority_urgent)) {
                    currentPriorityFilter = "urgent";
                } else if (checkedIds.contains(R.id.chip_priority_medium)) {
                    currentPriorityFilter = "medium";
                } else if (checkedIds.contains(R.id.chip_priority_low)) {
                    currentPriorityFilter = "low";
                } else {
                    currentPriorityFilter = "all";
                }
                applyFilterAndMetrics(recyclerView, emptyView, filterEmptyView, tvPending, tvInProgress, tvResolved);
                recyclerView.scheduleLayoutAnimation();
            });
        }

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (etSearch != null) etSearch.setText("");
                if (chipGroupStatus != null) chipGroupStatus.check(R.id.chip_filter_all);
                if (chipGroupPriority != null) chipGroupPriority.check(R.id.chip_priority_all);
                currentFilter = "all";
                currentPriorityFilter = "all";
                applyFilterAndMetrics(recyclerView, emptyView, filterEmptyView, tvPending, tvInProgress, tvResolved);
            });
        }

        if (btnEmptyAdd != null) {
            btnEmptyAdd.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showAddTicketDialog();
            });
        }

        if (fab != null) {
            fab.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showAddTicketDialog();
            });
        }
    }

    private void applyFilterAndMetrics(RecyclerView recyclerView, View emptyView, View filterEmptyView,
                                       TextView tvPending, TextView tvInProgress, TextView tvResolved) {
        if (allTickets == null || allTickets.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            if (tvPending != null) tvPending.setText("0");
            if (tvInProgress != null) tvInProgress.setText("0");
            if (tvResolved != null) tvResolved.setText("0");
            adapter.submitList(null);
            return;
        }

        if (emptyView != null) emptyView.setVisibility(View.GONE);

        int pendingCount = 0;
        int inProgressCount = 0;
        int resolvedCount = 0;

        List<MaintenanceTicketEntity> filteredList = new ArrayList<>();
        String q = searchQuery.toLowerCase(java.util.Locale.US);

        for (MaintenanceTicketEntity t : allTickets) {
            String status = t.status != null ? t.status.toLowerCase() : "pending";
            if ("resolved".equals(status)) {
                resolvedCount++;
            } else if ("in_progress".equals(status)) {
                inProgressCount++;
            } else {
                pendingCount++;
            }

            // Status filter
            if (!"all".equals(currentFilter) && !currentFilter.equalsIgnoreCase(status)) {
                continue;
            }

            // Priority filter
            String priority = t.priority != null ? t.priority.toLowerCase() : "medium";
            if ("urgent".equals(currentPriorityFilter) && !(priority.contains("high") || priority.contains("urgent"))) {
                continue;
            }
            if ("medium".equals(currentPriorityFilter) && !priority.contains("med")) {
                continue;
            }
            if ("low".equals(currentPriorityFilter) && !priority.contains("low")) {
                continue;
            }

            // Search query
            if (!q.isEmpty()) {
                boolean matchTitle = t.title != null && t.title.toLowerCase(java.util.Locale.US).contains(q);
                boolean matchDesc = t.description != null && t.description.toLowerCase(java.util.Locale.US).contains(q);
                boolean matchUnit = t.unitId != null && t.unitId.toLowerCase(java.util.Locale.US).contains(q);
                if (!matchTitle && !matchDesc && !matchUnit) {
                    continue;
                }
            }

            filteredList.add(t);
        }

        if (tvPending != null) tvPending.setText(String.valueOf(pendingCount));
        if (tvInProgress != null) tvInProgress.setText(String.valueOf(inProgressCount));
        if (tvResolved != null) tvResolved.setText(String.valueOf(resolvedCount));

        if (filteredList.isEmpty()) {
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            if (filterEmptyView != null) filterEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        adapter.submitList(filteredList);
    }

    private void showTicketActionDialog(MaintenanceTicketEntity ticket) {
        String[] options = new String[]{"Mark as In Progress", "Mark as Resolved", "Delete Ticket"};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(ticket.title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        viewModel.updateStatus(ticket, "in_progress");
                        Toast.makeText(requireContext(), "Marked as In Progress", Toast.LENGTH_SHORT).show();
                    } else if (which == 1) {
                        viewModel.updateStatus(ticket, "resolved");
                        Toast.makeText(requireContext(), "Marked as Resolved", Toast.LENGTH_SHORT).show();
                    } else if (which == 2) {
                        viewModel.deleteTicket(ticket);
                        Toast.makeText(requireContext(), "Ticket deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddTicketDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_ticket, null);
        EditText inputDesc = dialogView.findViewById(R.id.input_description);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Maintenance Ticket")
                .setView(dialogView)
                .setPositiveButton("Log Ticket", (dialog, which) -> {
                    String desc = inputDesc.getText().toString().trim();
                    if (!desc.isEmpty()) {
                        viewModel.createTicket("unit-1", "t-1", "Maintenance Request", desc, "medium");
                        Toast.makeText(requireContext(), "Ticket logged", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
