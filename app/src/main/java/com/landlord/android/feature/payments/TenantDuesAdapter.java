package com.landlord.android.feature.payments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.landlord.android.R;
import com.landlord.android.ui.widgets.StatusPillHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TenantDuesAdapter extends RecyclerView.Adapter<TenantDuesAdapter.ViewHolder> {

    public interface OnCollectPaymentListener {
        void onCollectPayment(TenantDueDto due);
    }

    private final List<TenantDueDto> items = new ArrayList<>();
    private final OnCollectPaymentListener listener;

    public TenantDuesAdapter(OnCollectPaymentListener listener) {
        this.listener = listener;
    }

    public void submitList(List<TenantDueDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tenant_due, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TenantDueDto item = items.get(position);

        holder.tenantName.setText(item.tenantName != null ? item.tenantName : "Tenant");
        holder.propertyUnit.setText((item.unitNumber != null ? item.unitNumber : "Unit") + " • " +
                (item.propertyName != null ? item.propertyName : "Property"));

        holder.dueAmount.setText(String.format(Locale.US, "৳%,.2f", item.totalDue));
        holder.monthlyRent.setText(String.format(Locale.US, "Monthly Rent: ৳%,.2f", item.monthlyRent));

        if (item.totalDue <= 0.01) {
            StatusPillHelper.setSuccess(holder.statusPill, "PAID");
            holder.dueAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.color_success));
            holder.btnCollect.setText("Advance");
        } else if ("PARTIAL".equalsIgnoreCase(item.paymentStatus) || item.totalPaid > 0) {
            StatusPillHelper.setWarning(holder.statusPill, "PARTIAL");
            holder.dueAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.color_warning));
            holder.btnCollect.setText("Collect");
        } else {
            StatusPillHelper.setDanger(holder.statusPill, "DUE");
            holder.dueAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.color_danger));
            holder.btnCollect.setText("Collect");
        }

        holder.btnCollect.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCollectPayment(item);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCollectPayment(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tenantName;
        final TextView propertyUnit;
        final TextView statusPill;
        final TextView dueAmount;
        final TextView monthlyRent;
        final MaterialButton btnCollect;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tenantName = itemView.findViewById(R.id.due_tenant_name);
            propertyUnit = itemView.findViewById(R.id.due_property_unit);
            statusPill = itemView.findViewById(R.id.due_status_pill);
            dueAmount = itemView.findViewById(R.id.due_amount);
            monthlyRent = itemView.findViewById(R.id.due_monthly_rent);
            btnCollect = itemView.findViewById(R.id.btn_collect_payment);
        }
    }
}
