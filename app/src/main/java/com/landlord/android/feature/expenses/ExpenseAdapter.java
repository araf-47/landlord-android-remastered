package com.landlord.android.feature.expenses;

import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.landlord.android.R;
import com.landlord.android.core.db.ExpenseEntity;
import java.util.Locale;

public class ExpenseAdapter extends ListAdapter<ExpenseEntity, ExpenseAdapter.ViewHolder> {

    public interface OnExpenseClickListener {
        void onExpenseClick(ExpenseEntity expense);
    }

    private final OnExpenseClickListener listener;

    public ExpenseAdapter(OnExpenseClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ExpenseEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ExpenseEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull ExpenseEntity oldItem, @NonNull ExpenseEntity newItem) {
                    return oldItem.localId.equals(newItem.localId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull ExpenseEntity oldItem, @NonNull ExpenseEntity newItem) {
                    return oldItem.amount == newItem.amount
                            && oldItem.description.equals(newItem.description)
                            && oldItem.date.equals(newItem.date)
                            && (oldItem.category != null ? oldItem.category.equals(newItem.category) : newItem.category == null);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseEntity item = getItem(position);
        holder.description.setText(item.description);
        String bearerStr = "landlord".equalsIgnoreCase(item.bearer) ? "Landlord" : "Tenant";
        holder.subtitle.setText(item.category + " • " + item.date + " • Paid by " + bearerStr);
        holder.amount.setText("-৳" + String.format(Locale.US, "%,.0f", item.amount));

        if (holder.categoryIcon != null) {
            String cat = item.category != null ? item.category.toLowerCase() : "";
            if (cat.contains("util") || cat.contains("electric") || cat.contains("water") || cat.contains("gas")) {
                holder.categoryIcon.setImageResource(R.drawable.ic_cat_utility);
            } else if (cat.contains("repair") || cat.contains("maint") || cat.contains("plumb")) {
                holder.categoryIcon.setImageResource(R.drawable.ic_cat_repair);
            } else if (cat.contains("tax") || cat.contains("govt") || cat.contains("fee")) {
                holder.categoryIcon.setImageResource(R.drawable.ic_cat_tax);
            } else {
                holder.categoryIcon.setImageResource(R.drawable.ic_cat_other);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if (listener != null) listener.onExpenseClick(item);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView description;
        final TextView subtitle;
        final TextView amount;
        final ImageView categoryIcon;

        ViewHolder(View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.expense_description);
            subtitle = itemView.findViewById(R.id.expense_subtitle);
            amount = itemView.findViewById(R.id.expense_amount);
            categoryIcon = itemView.findViewById(R.id.expense_category_icon);
        }
    }
}
