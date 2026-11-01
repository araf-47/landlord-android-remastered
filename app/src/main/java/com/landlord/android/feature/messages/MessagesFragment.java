package com.landlord.android.feature.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.landlord.android.R;

public class MessagesFragment extends Fragment {

    private MessagesViewModel viewModel;
    private ConversationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MessagesViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.conversations_list);
        View emptyView = view.findViewById(R.id.conversations_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ConversationAdapter(conv -> {
            Bundle args = new Bundle();
            args.putString("conversationLocalId", conv.localId);
            args.putString("withName", conv.tenantName);
            Navigation.findNavController(view).navigate(R.id.action_messages_to_thread, args);
        });
        recyclerView.setAdapter(adapter);

        viewModel.getConversations().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                if (emptyView != null) emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.submitList(list);
            }
        });
    }
}
