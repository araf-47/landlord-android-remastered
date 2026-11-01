package com.landlord.android.feature.messages;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.landlord.android.R;
import com.landlord.android.core.db.MessageEntity;

public class MessageAdapter extends ListAdapter<MessageEntity, MessageAdapter.ViewHolder> {

    public MessageAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<MessageEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<MessageEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull MessageEntity oldItem, @NonNull MessageEntity newItem) {
                    return oldItem.localId.equals(newItem.localId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull MessageEntity oldItem, @NonNull MessageEntity newItem) {
                    return oldItem.content.equals(newItem.content)
                            && oldItem.sentAt == newItem.sentAt;
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_bubble, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageEntity item = getItem(position);
        holder.messageText.setText(item.content);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.messageText.getLayoutParams();
        boolean isMine = "landlord".equalsIgnoreCase(item.senderType);

        if (isMine) {
            params.gravity = Gravity.END;
            holder.messageText.setBackgroundResource(R.drawable.bg_message_bubble_mine);
            holder.messageText.setTextColor(holder.itemView.getContext().getColor(R.color.md_on_primary));
        } else {
            params.gravity = Gravity.START;
            holder.messageText.setBackgroundResource(R.drawable.bg_message_bubble_theirs);
            holder.messageText.setTextColor(holder.itemView.getContext().getColor(R.color.md_on_surface));
        }
        holder.messageText.setLayoutParams(params);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView messageText;

        ViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
        }
    }
}
