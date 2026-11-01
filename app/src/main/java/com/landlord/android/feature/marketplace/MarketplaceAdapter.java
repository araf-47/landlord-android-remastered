package com.landlord.android.feature.marketplace;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.landlord.android.R;
import com.landlord.android.core.db.MarketplaceRequestEntity;
import com.landlord.android.ui.widgets.StatusPillHelper;

public class MarketplaceAdapter extends ListAdapter<MarketplaceRequestEntity, MarketplaceAdapter.ViewHolder> {

    public interface OnActionListener {
        void onApprove(MarketplaceRequestEntity request);
        void onReject(MarketplaceRequestEntity request);
    }

    private final OnActionListener listener;

    public MarketplaceAdapter(OnActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<MarketplaceRequestEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<MarketplaceRequestEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull MarketplaceRequestEntity oldItem, @NonNull MarketplaceRequestEntity newItem) {
                    return oldItem.localId.equals(newItem.localId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull MarketplaceRequestEntity oldItem, @NonNull MarketplaceRequestEntity newItem) {
                    return oldItem.status.equals(newItem.status);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_marketplace_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MarketplaceRequestEntity item = getItem(position);
        holder.applicant.setText(item.applicantName);
        if (holder.phone != null) holder.phone.setText(item.applicantPhone != null ? item.applicantPhone : "");
        if (holder.message != null) holder.message.setText(item.message != null ? item.message : "Inquiry for unit lease");

        boolean isPending = "pending".equalsIgnoreCase(item.status);
        if ("approved".equalsIgnoreCase(item.status)) {
            StatusPillHelper.setSuccess(holder.statusBadge, "APPROVED");
            holder.actions.setVisibility(View.GONE);
        } else if ("rejected".equalsIgnoreCase(item.status)) {
            StatusPillHelper.setDanger(holder.statusBadge, "DECLINED");
            holder.actions.setVisibility(View.GONE);
        } else {
            StatusPillHelper.setWarning(holder.statusBadge, "PENDING");
            holder.actions.setVisibility(View.VISIBLE);
        }

        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(item);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(item);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView applicant;
        final TextView phone;
        final TextView message;
        final TextView statusBadge;
        final View actions;
        final MaterialButton btnApprove;
        final MaterialButton btnReject;

        ViewHolder(View itemView) {
            super(itemView);
            applicant = itemView.findViewById(R.id.request_applicant);
            phone = itemView.findViewById(R.id.request_phone);
            message = itemView.findViewById(R.id.request_message);
            statusBadge = itemView.findViewById(R.id.request_status);
            actions = itemView.findViewById(R.id.request_actions);
            btnApprove = itemView.findViewById(R.id.approve_button);
            btnReject = itemView.findViewById(R.id.reject_button);
        }
    }
}
