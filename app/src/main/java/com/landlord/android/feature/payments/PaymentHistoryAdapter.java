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
import com.landlord.android.core.db.PaymentEntity;
import java.util.Locale;

public class PaymentHistoryAdapter extends ListAdapter<PaymentEntity, PaymentHistoryAdapter.ViewHolder> {

    public interface OnPaymentClickListener {
        void onPaymentClick(PaymentEntity payment);
    }

    private final OnPaymentClickListener listener;

    public PaymentHistoryAdapter(OnPaymentClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<PaymentEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PaymentEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull PaymentEntity oldItem, @NonNull PaymentEntity newItem) {
                    return oldItem.localId.equals(newItem.localId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull PaymentEntity oldItem, @NonNull PaymentEntity newItem) {
                    return oldItem.amount == newItem.amount
                            && oldItem.date.equals(newItem.date)
                            && oldItem.status.equals(newItem.status);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentEntity item = getItem(position);
        if (holder.title != null) holder.title.setText("৳" + String.format(Locale.US, "%,.0f", item.amount));
        if (holder.subtitle != null) holder.subtitle.setText("Date: " + item.date + " • Method: " + item.method.toUpperCase());
        if (holder.ref != null) holder.ref.setText(item.transactionRef != null ? item.transactionRef : "Confirmed Payment");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPaymentClick(item);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;
        final TextView ref;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.payment_amount);
            subtitle = itemView.findViewById(R.id.payment_date_method);
            ref = itemView.findViewById(R.id.payment_ref);
        }
    }
}
