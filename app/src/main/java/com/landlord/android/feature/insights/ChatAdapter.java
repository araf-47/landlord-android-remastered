package com.landlord.android.feature.insights;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.landlord.android.R;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final List<ChatMessage> messages = new ArrayList<>();

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_bubble, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage item = messages.get(position);
        holder.text.setText(item.text);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.text.getLayoutParams();
        if (item.isUser) {
            params.gravity = Gravity.END;
            holder.text.setBackgroundResource(R.drawable.bg_message_bubble_mine);
            holder.text.setTextColor(holder.itemView.getContext().getColor(R.color.md_on_primary));
        } else {
            params.gravity = Gravity.START;
            holder.text.setBackgroundResource(R.drawable.bg_message_bubble_theirs);
            holder.text.setTextColor(holder.itemView.getContext().getColor(R.color.md_on_surface));
        }
        holder.text.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView text;

        ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.chat_message_text);
        }
    }
}
