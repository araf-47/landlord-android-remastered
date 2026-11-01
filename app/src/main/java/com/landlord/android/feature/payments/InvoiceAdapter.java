package com.landlord.android.feature.payments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.landlord.android.R;
import com.landlord.android.core.db.InvoiceEntity;
import com.landlord.android.ui.widgets.StatusPillHelper;
import java.util.Locale;

public class InvoiceAdapter extends ListAdapter<InvoiceEntity, InvoiceAdapter.ViewHolder> {

    public interface OnInvoiceClickListener {
        void onInvoiceClick(InvoiceEntity invoice);
    }

    private final OnInvoiceClickListener listener;

    public InvoiceAdapter(OnInvoiceClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<InvoiceEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<InvoiceEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull InvoiceEntity oldItem, @NonNull InvoiceEntity newItem) {
                    return oldItem.localId.equals(newItem.localId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull InvoiceEntity oldItem, @NonNull InvoiceEntity newItem) {
                    return oldItem.balance == newItem.balance
                            && oldItem.status.equals(newItem.status);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InvoiceEntity item = getItem(position);
        holder.period.setText("Invoice • " + item.period);
        holder.total.setText("Total: ৳" + String.format(Locale.US, "%,.0f", item.amount));
        holder.dueDate.setText("Due: " + item.dueDate);
        holder.balance.setText("Balance: ৳" + String.format(Locale.US, "%,.0f", item.balance));

        String status = item.status != null ? item.status.toLowerCase() : "unpaid";
        if ("paid".equals(status)) {
            StatusPillHelper.setSuccess(holder.statusBadge, "PAID");
            holder.balance.setTextColor(holder.itemView.getContext().getColor(R.color.color_success));
        } else if ("partial".equals(status)) {
            StatusPillHelper.setWarning(holder.statusBadge, "PARTIAL");
            holder.balance.setTextColor(holder.itemView.getContext().getColor(R.color.color_warning));
        } else {
            StatusPillHelper.setDanger(holder.statusBadge, "UNPAID");
            holder.balance.setTextColor(holder.itemView.getContext().getColor(R.color.color_danger));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onInvoiceClick(item);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView period;
        final TextView total;
        final TextView dueDate;
        final TextView balance;
        final TextView statusBadge;

        ViewHolder(View itemView) {
            super(itemView);
            period = itemView.findViewById(R.id.invoice_period);
            total = itemView.findViewById(R.id.invoice_total);
            dueDate = itemView.findViewById(R.id.invoice_due_date);
            balance = itemView.findViewById(R.id.invoice_balance);
            statusBadge = itemView.findViewById(R.id.invoice_status_badge);
        }
    }
}
