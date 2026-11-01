package com.landlord.android.feature.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.landlord.android.R;

public class MessageThreadFragment extends Fragment {

    private MessagesViewModel viewModel;
    private MessageAdapter adapter;
    private String conversationId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message_thread, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        conversationId = getArguments() != null ? getArguments().getString("conversationLocalId") : "";
        viewModel = new ViewModelProvider(this).get(MessagesViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.messages_list);
        EditText inputMessage = view.findViewById(R.id.message_input);
        View btnSend = view.findViewById(R.id.send_button);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MessageAdapter();
        recyclerView.setAdapter(adapter);

        viewModel.getMessages(conversationId).observe(getViewLifecycleOwner(), messages -> {
            adapter.submitList(messages);
            if (messages != null && !messages.isEmpty()) {
                recyclerView.scrollToPosition(messages.size() - 1);
            }
        });

        if (inputMessage != null) {
            inputMessage.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && adapter.getItemCount() > 0) {
                    recyclerView.postDelayed(() -> recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1), 250);
                }
            });

            inputMessage.setOnClickListener(v -> {
                if (adapter.getItemCount() > 0) {
                    recyclerView.postDelayed(() -> recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1), 250);
                }
            });
        }

        if (btnSend != null && inputMessage != null) {
            btnSend.setOnClickListener(v -> {
                String text = inputMessage.getText().toString().trim();
                if (!text.isEmpty()) {
                    viewModel.sendMessage(conversationId, text);
                    inputMessage.setText("");
                }
            });
        }
    }
}
