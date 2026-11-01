package com.landlord.android.feature.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.landlord.android.R;
import com.landlord.android.core.common.CsvReportGenerator;
import com.landlord.android.core.common.PdfReportGenerator;
import com.landlord.android.core.db.ExpenseEntity;
import com.landlord.android.core.db.PaymentEntity;
import com.landlord.android.core.db.PropertyEntity;
import com.landlord.android.core.db.TenantEntity;
import com.landlord.android.feature.payments.TenantDueDto;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsFragment extends Fragment {

    private ReportsViewModel viewModel;

    private Spinner spinnerReportType;
    private Spinner spinnerProperty;
    private Spinner spinnerTenant;
    private View rowDateRange;
    private View rowProperty;
    private View rowTenant;
    private View rowCategory;
    private TextView reportOutput;
    private View reportCard;

    private double totalIncome = 0.0;
    private double totalExpenses = 0.0;
    private double totalDue = 0.0;
    private int occupiedUnits = 0;
    private int totalUnits = 0;

    private final List<PropertyEntity> cachedProperties = new ArrayList<>();
    private final List<TenantEntity> cachedTenants = new ArrayList<>();
    private final List<PaymentEntity> cachedPayments = new ArrayList<>();
    private final List<ExpenseEntity> cachedExpenses = new ArrayList<>();
    private final List<TenantDueDto> cachedDues = new ArrayList<>();
    private final Map<String, String> tenantNameMap = new HashMap<>();

    private static final String[] REPORT_TYPES = new String[]{
            "Portfolio Financial Statement",
            "Income & Rent Collections",
            "Operating Expenses Breakdown",
            "Property Occupancy Report",
            "Tenant Statement / Ledger"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ReportsViewModel.class);

        spinnerReportType = view.findViewById(R.id.input_report_type);
        spinnerProperty = view.findViewById(R.id.input_property);
        spinnerTenant = view.findViewById(R.id.input_tenant);
        rowDateRange = view.findViewById(R.id.date_range_row);
        rowProperty = view.findViewById(R.id.property_row);
        rowTenant = view.findViewById(R.id.tenant_row);
        rowCategory = view.findViewById(R.id.category_row);
        reportOutput = view.findViewById(R.id.report_json_output);
        reportCard = view.findViewById(R.id.report_json_card);

        com.google.android.material.textfield.TextInputEditText inputStartDate = view.findViewById(R.id.input_start_date);
        com.google.android.material.textfield.TextInputEditText inputEndDate = view.findViewById(R.id.input_end_date);

        if (inputStartDate != null) {
            inputStartDate.setOnClickListener(v -> showDatePicker(inputStartDate, "Select Start Date"));
        }
        if (inputEndDate != null) {
            inputEndDate.setOnClickListener(v -> showDatePicker(inputEndDate, "Select End Date"));
        }

        MaterialButton btnView = view.findViewById(R.id.view_report_button);
        MaterialButton btnPdf = view.findViewById(R.id.export_pdf_button);
        MaterialButton btnXlsx = view.findViewById(R.id.export_xlsx_button);

        setupSpinners();
        observeData();

        if (btnView != null) {
            btnView.setOnClickListener(v -> renderReportView());
        }

        if (btnPdf != null) {
            btnPdf.setOnClickListener(v -> exportPdf());
        }

        if (btnXlsx != null) {
            btnXlsx.setOnClickListener(v -> exportXlsx());
        }
    }

    private void showDatePicker(EditText targetField, String title) {
        com.google.android.material.datepicker.MaterialDatePicker<Long> picker =
                com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                        .setTitleText(title)
                        .setSelection(com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds())
                        .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                targetField.setText(sdf.format(new Date(selection)));
                renderReportView();
            }
        });

        picker.show(getParentFragmentManager(), "DATE_PICKER_" + title);
    }

    private void setupSpinners() {
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                REPORT_TYPES
        );
        spinnerReportType.setAdapter(typeAdapter);

        spinnerReportType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFilterVisibility(position);
                renderReportView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerProperty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerReportType.getSelectedItemPosition() == 3) {
                    renderReportView();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerTenant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerReportType.getSelectedItemPosition() == 4) {
                    renderReportView();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateFilterVisibility(int typeIndex) {
        switch (typeIndex) {
            case 0: // Portfolio Statement
            case 1: // Income & Collections
                if (rowProperty != null) rowProperty.setVisibility(View.GONE);
                if (rowTenant != null) rowTenant.setVisibility(View.GONE);
                if (rowCategory != null) rowCategory.setVisibility(View.GONE);
                break;
            case 2: // Expenses
                if (rowProperty != null) rowProperty.setVisibility(View.GONE);
                if (rowTenant != null) rowTenant.setVisibility(View.GONE);
                if (rowCategory != null) rowCategory.setVisibility(View.VISIBLE);
                break;
            case 3: // Occupancy
                if (rowProperty != null) rowProperty.setVisibility(View.VISIBLE);
                if (rowTenant != null) rowTenant.setVisibility(View.GONE);
                if (rowCategory != null) rowCategory.setVisibility(View.GONE);
                break;
            case 4: // Tenant Ledger
                if (rowProperty != null) rowProperty.setVisibility(View.GONE);
                if (rowTenant != null) rowTenant.setVisibility(View.VISIBLE);
                if (rowCategory != null) rowCategory.setVisibility(View.GONE);
                break;
        }
    }

    private void observeData() {
        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), inc -> {
            totalIncome = inc != null ? inc : 0.0;
        });

        viewModel.getTotalExpenses().observe(getViewLifecycleOwner(), exp -> {
            totalExpenses = exp != null ? exp : 0.0;
        });

        viewModel.getTotalOutstanding().observe(getViewLifecycleOwner(), due -> {
            totalDue = due != null ? due : 0.0;
        });

        viewModel.getOccupiedUnits().observe(getViewLifecycleOwner(), occ -> {
            occupiedUnits = occ != null ? occ : 0;
        });

        viewModel.getTotalUnits().observe(getViewLifecycleOwner(), tot -> {
            totalUnits = tot != null ? tot : 0;
        });

        viewModel.getProperties().observe(getViewLifecycleOwner(), props -> {
            if (props != null) {
                cachedProperties.clear();
                cachedProperties.addAll(props);
                List<String> names = new ArrayList<>();
                names.add("All Properties");
                for (PropertyEntity p : props) {
                    names.add(p.name);
                }
                ArrayAdapter<String> propAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        names
                );
                spinnerProperty.setAdapter(propAdapter);
            }
        });

        viewModel.getTenants().observe(getViewLifecycleOwner(), tenants -> {
            if (tenants != null) {
                cachedTenants.clear();
                cachedTenants.addAll(tenants);
                tenantNameMap.clear();
                List<String> names = new ArrayList<>();
                names.add("All Tenants");
                for (TenantEntity t : tenants) {
                    names.add(t.name);
                    tenantNameMap.put(t.localId, t.name);
                }
                ArrayAdapter<String> tenantAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        names
                );
                spinnerTenant.setAdapter(tenantAdapter);
            }
        });

        viewModel.getPayments().observe(getViewLifecycleOwner(), payments -> {
            if (payments != null) {
                cachedPayments.clear();
                cachedPayments.addAll(payments);
            }
        });

        viewModel.getExpenses().observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null) {
                cachedExpenses.clear();
                cachedExpenses.addAll(expenses);
            }
        });

        viewModel.getTenantDues().observe(getViewLifecycleOwner(), dues -> {
            if (dues != null) {
                cachedDues.clear();
                cachedDues.addAll(dues);
            }
        });
    }

    private void renderReportView() {
        if (reportOutput == null) return;
        int typeIndex = spinnerReportType.getSelectedItemPosition();

        StringBuilder sb = new StringBuilder();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());

        switch (typeIndex) {
            case 0: // Portfolio Financial Statement
                double net = totalIncome - totalExpenses;
                int occRate = totalUnits > 0 ? (int) Math.round(((double) occupiedUnits / totalUnits) * 100) : 0;

                sb.append("═══════════════════════════════════════════\n");
                sb.append("      PORTFOLIO FINANCIAL STATEMENT        \n");
                sb.append("═══════════════════════════════════════════\n");
                sb.append("Generated At: ").append(dateStr).append("\n\n");
                sb.append("1. INCOME SUMMARY\n");
                sb.append("   • Confirmed Collections:   ৳").append(String.format(Locale.US, "%,.2f", totalIncome)).append("\n");
                sb.append("   • Outstanding Receivables: ৳").append(String.format(Locale.US, "%,.2f", totalDue)).append("\n\n");
                sb.append("2. EXPENSE SUMMARY\n");
                sb.append("   • Operating Costs:         ৳").append(String.format(Locale.US, "%,.2f", totalExpenses)).append("\n\n");
                sb.append("3. NET OPERATIONAL CASH FLOW\n");
                sb.append("   • Net Operating Surplus:   ৳").append(String.format(Locale.US, "%,.2f", net)).append("\n\n");
                sb.append("4. OCCUPANCY METRICS\n");
                sb.append("   • Total Units:             ").append(totalUnits).append("\n");
                sb.append("   • Occupied Units:          ").append(occupiedUnits).append("\n");
                sb.append("   • Occupancy Rate:          ").append(occRate).append("%\n");
                sb.append("═══════════════════════════════════════════\n");
                break;

            case 1: // Income & Collections
                sb.append("═══════════════════════════════════════════\n");
                sb.append("       INCOME & RENT COLLECTIONS           \n");
                sb.append("═══════════════════════════════════════════\n");
                sb.append("Generated At: ").append(dateStr).append("\n");
                sb.append("Total Confirmed: ৳").append(String.format(Locale.US, "%,.2f", totalIncome)).append("\n\n");
                sb.append(String.format("%-10s %-16s %-8s %s\n", "ID", "TENANT", "METHOD", "AMOUNT"));
                sb.append("───────────────────────────────────────────\n");
                for (PaymentEntity p : cachedPayments) {
                    String tName = tenantNameMap.get(p.tenantId);
                    if (tName == null) tName = "Tenant";
                    if (tName.length() > 15) tName = tName.substring(0, 15);
                    sb.append(String.format(Locale.US, "%-10s %-16s %-8s ৳%,.0f\n",
                            p.localId, tName, p.method.toUpperCase(), p.amount));
                }
                sb.append("═══════════════════════════════════════════\n");
                break;

            case 2: // Operating Expenses
                sb.append("═══════════════════════════════════════════\n");
                sb.append("      OPERATING EXPENSES BREAKDOWN         \n");
                sb.append("═══════════════════════════════════════════\n");
                sb.append("Generated At: ").append(dateStr).append("\n");
                sb.append("Total Operating Costs: ৳").append(String.format(Locale.US, "%,.2f", totalExpenses)).append("\n\n");
                sb.append(String.format("%-12s %-18s %s\n", "CATEGORY", "DESCRIPTION", "AMOUNT"));
                sb.append("───────────────────────────────────────────\n");
                for (ExpenseEntity e : cachedExpenses) {
                    String desc = e.description != null ? e.description : "";
                    if (desc.length() > 17) desc = desc.substring(0, 17);
                    sb.append(String.format(Locale.US, "%-12s %-18s ৳%,.0f\n",
                            e.category, desc, e.amount));
                }
                sb.append("═══════════════════════════════════════════\n");
                break;

            case 3: // Property Occupancy
                sb.append("═══════════════════════════════════════════\n");
                sb.append("        PROPERTY OCCUPANCY REPORT          \n");
                sb.append("═══════════════════════════════════════════\n");
                sb.append("Generated At: ").append(dateStr).append("\n");
                sb.append("Portfolio Units: ").append(totalUnits).append(" | Occupied: ").append(occupiedUnits).append("\n\n");
                for (PropertyEntity prop : cachedProperties) {
                    sb.append("• ").append(prop.name).append(" (").append(prop.area).append(", ").append(prop.district).append(")\n");
                    sb.append("  Type: ").append(prop.propertyType).append(" • Status: Active Management\n");
                }
                sb.append("═══════════════════════════════════════════\n");
                break;

            case 4: // Tenant Statement / Ledger
                int tenantPos = spinnerTenant.getSelectedItemPosition();
                TenantEntity selectedTenant = null;
                if (tenantPos > 0 && tenantPos - 1 < cachedTenants.size()) {
                    selectedTenant = cachedTenants.get(tenantPos - 1);
                }

                sb.append("═══════════════════════════════════════════\n");
                sb.append("         TENANT STATEMENT & LEDGER         \n");
                sb.append("═══════════════════════════════════════════\n");
                if (selectedTenant != null) {
                    sb.append("Tenant Name: ").append(selectedTenant.name).append("\n");
                    sb.append("Phone: ").append(selectedTenant.phone).append("\n");
                    sb.append("National ID: ").append(selectedTenant.nationalId).append("\n\n");

                    sb.append("Recent Payment History:\n");
                    sb.append(String.format("%-12s %-12s %-10s %s\n", "DATE", "RECEIPT", "METHOD", "PAID"));
                    sb.append("───────────────────────────────────────────\n");
                    double tenantTotalPaid = 0.0;
                    for (PaymentEntity p : cachedPayments) {
                        if (selectedTenant.localId.equals(p.tenantId)) {
                            sb.append(String.format(Locale.US, "%-12s %-12s %-10s ৳%,.0f\n",
                                    p.date, p.localId, p.method.toUpperCase(), p.amount));
                            tenantTotalPaid += p.amount;
                        }
                    }
                    sb.append("\nTotal Rent Paid: ৳").append(String.format(Locale.US, "%,.2f", tenantTotalPaid)).append("\n");
                } else {
                    sb.append("Active Tenants Overview (All Tenants):\n\n");
                    for (TenantDueDto due : cachedDues) {
                        sb.append(String.format(Locale.US, "• %-18s (%s) Due: ৳%,.0f [%s]\n",
                                due.tenantName, due.unitNumber, due.totalDue, due.paymentStatus));
                    }
                }
                sb.append("═══════════════════════════════════════════\n");
                break;
        }

        reportOutput.setText(sb.toString());
        if (reportCard != null) reportCard.setVisibility(View.VISIBLE);
    }

    private void exportPdf() {
        int typeIndex = spinnerReportType.getSelectedItemPosition();
        String title = REPORT_TYPES[typeIndex];
        String subtitle = "Official certified statement for owner & accounts audit";

        List<String> summary = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        List<String[]> rows = new ArrayList<>();

        switch (typeIndex) {
            case 0: // Portfolio Financial Statement
                double net = totalIncome - totalExpenses;
                int occRate = totalUnits > 0 ? (int) Math.round(((double) occupiedUnits / totalUnits) * 100) : 0;
                summary.add("• Confirmed Collections: ৳" + String.format(Locale.US, "%,.2f", totalIncome));
                summary.add("• Outstanding Receivables: ৳" + String.format(Locale.US, "%,.2f", totalDue));
                summary.add("• Total Operating Costs: ৳" + String.format(Locale.US, "%,.2f", totalExpenses));
                summary.add("• Net Operating Cash Flow: ৳" + String.format(Locale.US, "%,.2f", net));
                summary.add("• Portfolio Occupancy Rate: " + occRate + "% (" + occupiedUnits + "/" + totalUnits + " units)");

                headers.add("METRIC / ACCOUNT");
                headers.add("STATUS");
                headers.add("AMOUNT (BDT)");

                rows.add(new String[]{"Total Rent Collected", "Confirmed", String.format(Locale.US, "৳%,.2f", totalIncome)});
                rows.add(new String[]{"Total Operating Expenses", "Paid Out", String.format(Locale.US, "৳%,.2f", totalExpenses)});
                rows.add(new String[]{"Net Operating Surplus", "Retained", String.format(Locale.US, "৳%,.2f", net)});
                rows.add(new String[]{"Pending Rent Receivables", "Overdue", String.format(Locale.US, "৳%,.2f", totalDue)});
                break;

            case 1: // Income & Collections
                summary.add("• Total Income Collections: ৳" + String.format(Locale.US, "%,.2f", totalIncome));
                summary.add("• Total Transactions Recorded: " + cachedPayments.size());

                headers.add("PAYMENT ID");
                headers.add("TENANT");
                headers.add("METHOD");
                headers.add("DATE");
                headers.add("AMOUNT");

                for (PaymentEntity p : cachedPayments) {
                    String tName = tenantNameMap.get(p.tenantId);
                    if (tName == null) tName = "Tenant (" + p.tenantId + ")";
                    rows.add(new String[]{p.localId, tName, p.method.toUpperCase(), p.date, String.format(Locale.US, "৳%,.2f", p.amount)});
                }
                break;

            case 2: // Operating Expenses
                summary.add("• Total Portfolio Operating Costs: ৳" + String.format(Locale.US, "%,.2f", totalExpenses));
                summary.add("• Recorded Maintenance & Vendor Entries: " + cachedExpenses.size());

                headers.add("EXPENSE ID");
                headers.add("CATEGORY");
                headers.add("DESCRIPTION");
                headers.add("DATE");
                headers.add("AMOUNT");

                for (ExpenseEntity e : cachedExpenses) {
                    rows.add(new String[]{e.localId, e.category, e.description != null ? e.description : "", e.date, String.format(Locale.US, "৳%,.2f", e.amount)});
                }
                break;

            case 3: // Property Occupancy
                summary.add("• Total Portfolio Units: " + totalUnits);
                summary.add("• Occupied Units: " + occupiedUnits + " | Vacant Units: " + (totalUnits - occupiedUnits));

                headers.add("PROPERTY NAME");
                headers.add("LOCATION");
                headers.add("PROPERTY TYPE");
                headers.add("STATUS");

                for (PropertyEntity p : cachedProperties) {
                    rows.add(new String[]{p.name, p.area + ", " + p.district, p.propertyType, "Managed"});
                }
                break;

            case 4: // Tenant Statement / Ledger
                int tenantPos = spinnerTenant.getSelectedItemPosition();
                TenantEntity selectedTenant = null;
                if (tenantPos > 0 && tenantPos - 1 < cachedTenants.size()) {
                    selectedTenant = cachedTenants.get(tenantPos - 1);
                }

                if (selectedTenant != null) {
                    summary.add("• Tenant Name: " + selectedTenant.name);
                    summary.add("• Contact: " + selectedTenant.phone + " | National ID: " + selectedTenant.nationalId);

                    headers.add("DATE");
                    headers.add("RECEIPT ID");
                    headers.add("METHOD");
                    headers.add("STATUS");
                    headers.add("PAID AMOUNT");

                    for (PaymentEntity p : cachedPayments) {
                        if (selectedTenant.localId.equals(p.tenantId)) {
                            rows.add(new String[]{p.date, p.localId, p.method.toUpperCase(), "Confirmed", String.format(Locale.US, "৳%,.2f", p.amount)});
                        }
                    }
                } else {
                    summary.add("• Portfolio Active Tenants Dues Summary");

                    headers.add("TENANT NAME");
                    headers.add("UNIT / PROPERTY");
                    headers.add("MONTHLY RENT");
                    headers.add("TOTAL DUE");
                    headers.add("STATUS");

                    for (TenantDueDto due : cachedDues) {
                        rows.add(new String[]{
                                due.tenantName,
                                due.unitNumber + " • " + due.propertyName,
                                String.format(Locale.US, "৳%,.2f", due.monthlyRent),
                                String.format(Locale.US, "৳%,.2f", due.totalDue),
                                due.paymentStatus
                        });
                    }
                }
                break;
        }

        File pdfFile = PdfReportGenerator.generateReportPdf(
                requireContext(),
                title,
                subtitle,
                summary,
                headers,
                rows
        );
        PdfReportGenerator.openOrShareReport(requireContext(), pdfFile, title);
        Toast.makeText(requireContext(), "PDF report generated successfully", Toast.LENGTH_SHORT).show();
    }

    private void exportXlsx() {
        int typeIndex = spinnerReportType.getSelectedItemPosition();
        String title = REPORT_TYPES[typeIndex];

        List<String> summary = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        List<String[]> rows = new ArrayList<>();

        switch (typeIndex) {
            case 0:
                double net = totalIncome - totalExpenses;
                summary.add("Confirmed Collections: BDT " + totalIncome);
                summary.add("Total Operating Expenses: BDT " + totalExpenses);
                summary.add("Net Cash Flow: BDT " + net);
                summary.add("Total Outstanding Dues: BDT " + totalDue);

                headers.add("Account Metric");
                headers.add("Status");
                headers.add("Amount (BDT)");

                rows.add(new String[]{"Total Collections", "Confirmed", String.valueOf(totalIncome)});
                rows.add(new String[]{"Operating Expenses", "Paid", String.valueOf(totalExpenses)});
                rows.add(new String[]{"Net Operating Surplus", "Retained", String.valueOf(net)});
                rows.add(new String[]{"Pending Receivables", "Due", String.valueOf(totalDue)});
                break;

            case 1:
                summary.add("Total Income Collections: BDT " + totalIncome);
                headers.add("Payment ID");
                headers.add("Tenant");
                headers.add("Method");
                headers.add("Date");
                headers.add("Amount (BDT)");

                for (PaymentEntity p : cachedPayments) {
                    String tName = tenantNameMap.get(p.tenantId);
                    if (tName == null) tName = "Tenant (" + p.tenantId + ")";
                    rows.add(new String[]{p.localId, tName, p.method, p.date, String.valueOf(p.amount)});
                }
                break;

            case 2:
                summary.add("Total Operating Expenses: BDT " + totalExpenses);
                headers.add("Expense ID");
                headers.add("Category");
                headers.add("Description");
                headers.add("Date");
                headers.add("Amount (BDT)");

                for (ExpenseEntity e : cachedExpenses) {
                    rows.add(new String[]{e.localId, e.category, e.description, e.date, String.valueOf(e.amount)});
                }
                break;

            case 3:
                summary.add("Total Portfolio Units: " + totalUnits);
                summary.add("Occupied Units: " + occupiedUnits);
                headers.add("Property Name");
                headers.add("Address");
                headers.add("Area");
                headers.add("Type");

                for (PropertyEntity p : cachedProperties) {
                    rows.add(new String[]{p.name, p.address, p.area, p.propertyType});
                }
                break;

            case 4:
                headers.add("Tenant Name");
                headers.add("Unit");
                headers.add("Property");
                headers.add("Monthly Rent");
                headers.add("Total Due");
                headers.add("Status");

                for (TenantDueDto due : cachedDues) {
                    rows.add(new String[]{
                            due.tenantName,
                            due.unitNumber,
                            due.propertyName,
                            String.valueOf(due.monthlyRent),
                            String.valueOf(due.totalDue),
                            due.paymentStatus
                    });
                }
                break;
        }

        File csvFile = CsvReportGenerator.generateReportCsv(requireContext(), title, summary, headers, rows);
        CsvReportGenerator.openOrShareCsv(requireContext(), csvFile, title);
        Toast.makeText(requireContext(), "Spreadsheet generated successfully", Toast.LENGTH_SHORT).show();
    }
}
