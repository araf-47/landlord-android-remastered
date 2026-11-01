package com.landlord.android.feature.maintenance;

import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.landlord.android.R;
import com.landlord.android.core.db.MaintenanceTicketEntity;
import com.landlord.android.ui.widgets.StatusPillHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MaintenanceAdapter extends ListAdapter<MaintenanceTicketEntity, MaintenanceAdapter.ViewHolder> {

    public interface OnTicketClickListener {
        void onTicketClick(MaintenanceTicketEntity ticket);
    }

    private final OnTicketClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    public MaintenanceAdapter(OnTicketClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<MaintenanceTicketEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<MaintenanceTicketEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull MaintenanceTicketEntity oldItem, @NonNull MaintenanceTicketEntity newItem) {
                    return oldItem.localId.equals(newItem.localId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull MaintenanceTicketEntity oldItem, @NonNull MaintenanceTicketEntity newItem) {
                    return oldItem.status.equals(newItem.status)
                            && oldItem.title.equals(newItem.title)
                            && (oldItem.priority != null ? oldItem.priority.equals(newItem.priority) : newItem.priority == null);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MaintenanceTicketEntity item = getItem(position);
        holder.title.setText(item.title != null ? item.title : "Maintenance Issue");
        holder.description.setText(item.description);

        String status = item.status != null ? item.status.toLowerCase() : "pending";
        if ("resolved".equals(status)) {
            StatusPillHelper.setSuccess(holder.statusBadge, "RESOLVED");
        } else if ("in_progress".equals(status)) {
            StatusPillHelper.setInfo(holder.statusBadge, "IN PROGRESS");
        } else {
            StatusPillHelper.setWarning(holder.statusBadge, "PENDING");
        }

        String priority = item.priority != null ? item.priority.toLowerCase() : "medium";
        if (holder.priorityBadge != null) {
            if ("urgent".equals(priority) || "high".equals(priority)) {
                StatusPillHelper.setDanger(holder.priorityBadge, "URGENT");
            } else if ("low".equals(priority)) {
                StatusPillHelper.setInfo(holder.priorityBadge, "LOW");
            } else {
                StatusPillHelper.setWarning(holder.priorityBadge, "MEDIUM");
            }
        }

        if (holder.unitBadge != null) {
            if (item.unitId != null && !item.unitId.isEmpty()) {
                holder.unitBadge.setText("Unit " + item.unitId.replace("unit-", ""));
                holder.unitBadge.setVisibility(View.VISIBLE);
            } else {
                holder.unitBadge.setVisibility(View.GONE);
            }
        }

        if (holder.date != null) {
            if (item.createdAt > 0) {
                holder.date.setText(dateFormat.format(new Date(item.createdAt)));
            } else {
                holder.date.setText("Recent");
            }
        }

        holder.itemView.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if (listener != null) listener.onTicketClick(item);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView description;
        final TextView statusBadge;
        final TextView priorityBadge;
        final TextView unitBadge;
        final TextView date;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.ticket_title);
            description = itemView.findViewById(R.id.ticket_description);
            statusBadge = itemView.findViewById(R.id.ticket_status_badge);
            priorityBadge = itemView.findViewById(R.id.ticket_priority_badge);
            unitBadge = itemView.findViewById(R.id.ticket_unit_badge);
            date = itemView.findViewById(R.id.ticket_date);
        }
    }
}
