package com.landlord.android.feature.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.landlord.android.R;
import com.landlord.android.core.db.ConversationEntity;

public class ConversationAdapter extends ListAdapter<ConversationEntity, ConversationAdapter.ViewHolder> {

    public interface OnConversationClickListener {
        void onConversationClick(ConversationEntity conversation);
    }

    private final OnConversationClickListener listener;

    public ConversationAdapter(OnConversationClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ConversationEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ConversationEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull ConversationEntity oldItem, @NonNull ConversationEntity newItem) {
                    return oldItem.localId.equals(newItem.localId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull ConversationEntity oldItem, @NonNull ConversationEntity newItem) {
                    return oldItem.lastMessageTime == newItem.lastMessageTime
                            && oldItem.lastMessage.equals(newItem.lastMessage);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConversationEntity item = getItem(position);
        holder.name.setText(item.tenantName != null ? item.tenantName : "Tenant");
        if (holder.lastMessage != null) {
            holder.lastMessage.setText(item.lastMessage != null ? item.lastMessage : "");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onConversationClick(item);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView lastMessage;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.conversation_name);
            lastMessage = itemView.findViewById(R.id.conversation_last_message);
        }
    }
}
