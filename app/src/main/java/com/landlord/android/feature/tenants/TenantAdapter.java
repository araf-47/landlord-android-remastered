package com.landlord.android.feature.tenants;

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
import com.landlord.android.core.db.TenantEntity;
import com.landlord.android.core.db.UnitEntity;
import com.landlord.android.ui.widgets.StatusPillHelper;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TenantAdapter extends ListAdapter<TenantEntity, TenantAdapter.ViewHolder> {

    public interface OnTenantActionListener {
        void onTenantClick(TenantEntity tenant);
        void onCallClick(TenantEntity tenant);
        void onChatClick(TenantEntity tenant);
    }

    private final OnTenantActionListener listener;
    private final Map<String, UnitEntity> unitMap = new HashMap<>();

    public TenantAdapter(OnTenantActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setUnits(Map<String, UnitEntity> units) {
        this.unitMap.clear();
        if (units != null) {
            this.unitMap.putAll(units);
        }
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<TenantEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TenantEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull TenantEntity oldItem, @NonNull TenantEntity newItem) {
                    return oldItem.localId.equals(newItem.localId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull TenantEntity oldItem, @NonNull TenantEntity newItem) {
                    return oldItem.name.equals(newItem.name)
                            && oldItem.phone.equals(newItem.phone)
                            && oldItem.status.equals(newItem.status)
                            && (oldItem.unitId != null ? oldItem.unitId.equals(newItem.unitId) : newItem.unitId == null);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tenant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TenantEntity item = getItem(position);
        holder.name.setText(item.name);
        holder.contact.setText(item.phone + (item.email != null && !item.email.isEmpty() ? " • " + item.email : ""));

        boolean isActive = "active".equalsIgnoreCase(item.status);
        if (isActive) {
            StatusPillHelper.setSuccess(holder.statusBadge, "ACTIVE");
        } else {
            StatusPillHelper.setDanger(holder.statusBadge, "INACTIVE");
        }

        UnitEntity unit = item.unitId != null ? unitMap.get(item.unitId) : null;
        if (unit != null) {
            if (holder.unitBadge != null) {
                holder.unitBadge.setText("Unit " + unit.unitNumber);
                holder.unitBadge.setVisibility(View.VISIBLE);
            }
            if (holder.rentBadge != null) {
                holder.rentBadge.setText("৳" + String.format(Locale.US, "%,.0f", unit.rent) + " / mo");
                holder.rentBadge.setVisibility(View.VISIBLE);
            }
        } else {
            if (holder.unitBadge != null) {
                holder.unitBadge.setText(item.unitId != null && !item.unitId.isEmpty() ? item.unitId : "No Unit");
                holder.unitBadge.setVisibility(View.VISIBLE);
            }
            if (holder.rentBadge != null) {
                holder.rentBadge.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if (listener != null) listener.onTenantClick(item);
        });

        if (holder.btnCall != null) {
            holder.btnCall.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (listener != null) listener.onCallClick(item);
            });
        }

        if (holder.btnChat != null) {
            holder.btnChat.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (listener != null) listener.onChatClick(item);
            });
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView contact;
        final TextView statusBadge;
        final TextView unitBadge;
        final TextView rentBadge;
        final View btnCall;
        final View btnChat;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tenant_name);
            contact = itemView.findViewById(R.id.tenant_contact);
            statusBadge = itemView.findViewById(R.id.tenant_status_badge);
            unitBadge = itemView.findViewById(R.id.tenant_unit);
            rentBadge = itemView.findViewById(R.id.tenant_rent);
            btnCall = itemView.findViewById(R.id.btn_tenant_call_shortcut);
            btnChat = itemView.findViewById(R.id.btn_tenant_chat_shortcut);
        }
    }
}
