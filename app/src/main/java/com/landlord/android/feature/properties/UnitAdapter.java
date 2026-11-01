package com.landlord.android.feature.properties;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.landlord.android.R;
import com.landlord.android.core.db.UnitEntity;
import com.landlord.android.ui.widgets.StatusPillHelper;
import java.util.Locale;

public class UnitAdapter extends ListAdapter<UnitEntity, UnitAdapter.ViewHolder> {

    public interface OnUnitClickListener {
        void onUnitClick(UnitEntity unit);
    }

    private final OnUnitClickListener listener;

    public UnitAdapter(OnUnitClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<UnitEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<UnitEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull UnitEntity oldItem, @NonNull UnitEntity newItem) {
                    return oldItem.localId.equals(newItem.localId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull UnitEntity oldItem, @NonNull UnitEntity newItem) {
                    return oldItem.unitNumber.equals(newItem.unitNumber)
                            && oldItem.rent == newItem.rent
                            && oldItem.status.equals(newItem.status);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UnitEntity item = getItem(position);
        holder.unitNumber.setText("Unit " + item.unitNumber);
        holder.unitRent.setText("৳" + String.format(Locale.US, "%,.0f", item.rent) + " / month");

        boolean isOccupied = "occupied".equalsIgnoreCase(item.status);
        if (isOccupied) {
            StatusPillHelper.setSuccess(holder.statusBadge, "OCCUPIED");
        } else {
            StatusPillHelper.setInfo(holder.statusBadge, "VACANT");
        }

        holder.itemView.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            if (listener != null) listener.onUnitClick(item);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView unitNumber;
        final TextView unitRent;
        final TextView statusBadge;

        ViewHolder(View itemView) {
            super(itemView);
            unitNumber = itemView.findViewById(R.id.unit_number);
            unitRent = itemView.findViewById(R.id.unit_rent);
            statusBadge = itemView.findViewById(R.id.unit_status_badge);
        }
    }
}
