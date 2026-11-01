package com.landlord.android.feature.marketplace;

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
import com.landlord.android.R;
import com.landlord.android.core.db.MarketplaceRequestEntity;

public class MarketplaceFragment extends Fragment {

    private MarketplaceViewModel viewModel;
    private MarketplaceAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_marketplace, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MarketplaceViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.requests_list);
        View emptyView = view.findViewById(R.id.requests_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MarketplaceAdapter(new MarketplaceAdapter.OnActionListener() {
            @Override
            public void onApprove(MarketplaceRequestEntity request) {
                viewModel.approveRequest(request);
                Toast.makeText(requireContext(), "Application approved for " + request.applicantName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReject(MarketplaceRequestEntity request) {
                viewModel.rejectRequest(request);
                Toast.makeText(requireContext(), "Application declined", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);

        viewModel.getRequests().observe(getViewLifecycleOwner(), list -> {
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
