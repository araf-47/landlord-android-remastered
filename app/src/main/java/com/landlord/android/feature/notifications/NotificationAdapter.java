package com.landlord.android.feature.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.landlord.android.R;
import com.landlord.android.core.db.NotificationEntity;

public class NotificationAdapter extends ListAdapter<NotificationEntity, NotificationAdapter.ViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationEntity notification);
    }

    private final OnNotificationClickListener listener;

    public NotificationAdapter(OnNotificationClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<NotificationEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<NotificationEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull NotificationEntity oldItem, @NonNull NotificationEntity newItem) {
                    return oldItem.localId.equals(newItem.localId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull NotificationEntity oldItem, @NonNull NotificationEntity newItem) {
                    return oldItem.read == newItem.read
                            && oldItem.title.equals(newItem.title);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationEntity item = getItem(position);
        holder.title.setText(item.title);
        holder.body.setText(item.body);
        holder.unreadDot.setVisibility(item.read ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNotificationClick(item);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View unreadDot;
        final TextView title;
        final TextView body;

        ViewHolder(View itemView) {
            super(itemView);
            unreadDot = itemView.findViewById(R.id.unread_dot);
            title = itemView.findViewById(R.id.notification_title);
            body = itemView.findViewById(R.id.notification_body);
        }
    }
}
