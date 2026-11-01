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
import com.landlord.android.core.db.PropertyEntity;
import com.landlord.android.ui.widgets.StatusPillHelper;
import java.util.HashMap;
import java.util.Map;

public class PropertyAdapter extends ListAdapter<PropertyEntity, PropertyAdapter.ViewHolder> {

    public interface OnPropertyClickListener {
        void onPropertyClick(PropertyEntity property);
    }

    private final OnPropertyClickListener listener;
    private final Map<String, Integer> unitCounts = new HashMap<>();

    public PropertyAdapter(OnPropertyClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setUnitCounts(Map<String, Integer> counts) {
        this.unitCounts.clear();
        if (counts != null) {
            this.unitCounts.putAll(counts);
        }
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<PropertyEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PropertyEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull PropertyEntity oldItem, @NonNull PropertyEntity newItem) {
                    return oldItem.localId.equals(newItem.localId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull PropertyEntity oldItem, @NonNull PropertyEntity newItem) {
                    return oldItem.name.equals(newItem.name)
                            && oldItem.address.equals(newItem.address);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_property, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PropertyEntity item = getItem(position);
        holder.name.setText(item.name);
        holder.address.setText(item.address + (item.area != null ? ", " + item.area : ""));
        if (holder.typeTag != null) {
            String type = item.propertyType != null ? item.propertyType.toUpperCase() : "APARTMENT";
            StatusPillHelper.setInfo(holder.typeTag, type);
        }
        if (holder.unitsBadge != null) {
            Integer count = unitCounts.get(item.localId);
            if (count != null && count > 0) {
                holder.unitsBadge.setText(count + (count == 1 ? " Unit" : " Units"));
                holder.unitsBadge.setVisibility(View.VISIBLE);
            } else {
                holder.unitsBadge.setVisibility(View.GONE);
            }
        }
        holder.itemView.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            if (listener != null) listener.onPropertyClick(item);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView address;
        final TextView typeTag;
        final TextView unitsBadge;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.property_name);
            address = itemView.findViewById(R.id.property_address);
            typeTag = itemView.findViewById(R.id.property_type_tag);
            unitsBadge = itemView.findViewById(R.id.property_units_badge);
        }
    }
}
