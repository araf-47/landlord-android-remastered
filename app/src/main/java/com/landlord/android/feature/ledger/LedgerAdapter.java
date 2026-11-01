package com.landlord.android.feature.ledger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.landlord.android.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LedgerAdapter extends RecyclerView.Adapter<LedgerAdapter.ViewHolder> {

    private final List<LedgerEntry> items = new ArrayList<>();

    public void submitList(List<LedgerEntry> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ledger_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LedgerEntry item = items.get(position);
        holder.description.setText(item.description);
        holder.type.setText((item.isIncome ? "Income" : "Expense") + " • " + item.date);

        if (item.isIncome) {
            holder.icon.setText("📥");
            holder.amount.setText("+৳" + String.format(Locale.US, "%,.0f", item.amount));
            holder.amount.setTextColor(holder.itemView.getContext().getColor(R.color.color_success));
        } else {
            holder.icon.setText("📤");
            holder.amount.setText("-৳" + String.format(Locale.US, "%,.0f", item.amount));
            holder.amount.setTextColor(holder.itemView.getContext().getColor(R.color.color_danger));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView icon;
        final TextView description;
        final TextView type;
        final TextView amount;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ledger_icon);
            description = itemView.findViewById(R.id.ledger_description);
            type = itemView.findViewById(R.id.ledger_type);
            amount = itemView.findViewById(R.id.ledger_amount);
        }
    }
}
