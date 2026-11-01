package com.landlord.android.feature.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.landlord.android.R;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel viewModel;
    private NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.notifications_list);
        View emptyView = view.findViewById(R.id.notifications_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter(item -> {
            viewModel.markAsRead(item.localId);
            Toast.makeText(requireContext(), "Marked as read", Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        viewModel.getNotifications().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                if (emptyView != null) emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.submitList(list);
            }
        });

        // notifications setup complete
    }
}
