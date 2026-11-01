package com.landlord.android.feature.insights;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.landlord.android.R;
import com.landlord.android.core.ai.OfflineAiAssistant;

public class InsightsFragment extends Fragment {

    private ChatAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_insights, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView chatList = view.findViewById(R.id.chat_list);
        ProgressBar progress = view.findViewById(R.id.chat_progress);
        TextInputEditText questionInput = view.findViewById(R.id.question_input);
        MaterialButton askButton = view.findViewById(R.id.ask_button);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        chatList.setLayoutManager(layoutManager);

        adapter = new ChatAdapter();
        chatList.setAdapter(adapter);

        questionInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && adapter.getItemCount() > 0) {
                chatList.postDelayed(() -> chatList.smoothScrollToPosition(adapter.getItemCount() - 1), 250);
            }
        });

        questionInput.setOnClickListener(v -> {
            if (adapter.getItemCount() > 0) {
                chatList.postDelayed(() -> chatList.smoothScrollToPosition(adapter.getItemCount() - 1), 250);
            }
        });

        // Seed welcome greeting message
        adapter.addMessage(new ChatMessage(
                "Hello! I am your LandLord AI Assistant. Ask me about calculating rental yield, lease agreements, occupancy tips, or maintenance management.",
                false
        ));

        askButton.setOnClickListener(v -> {
            String query = String.valueOf(questionInput.getText()).trim();
            if (query.isEmpty()) return;

            adapter.addMessage(new ChatMessage(query, true));
            chatList.smoothScrollToPosition(adapter.getItemCount() - 1);
            questionInput.setText("");

            progress.setVisibility(View.VISIBLE);
            askButton.setEnabled(false);

            // Simulate fast intelligent processing
            handler.postDelayed(() -> {
                progress.setVisibility(View.GONE);
                askButton.setEnabled(true);
                String response = OfflineAiAssistant.getResponse(query);
                adapter.addMessage(new ChatMessage(response, false));
                chatList.smoothScrollToPosition(adapter.getItemCount() - 1);
            }, 500);
        });
    }
}
