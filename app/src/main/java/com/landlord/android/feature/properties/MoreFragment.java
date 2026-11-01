package com.landlord.android.feature.properties;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.landlord.android.R;

public class MoreFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindTile(view, R.id.more_marketplace, R.id.nav_marketplace);
        bindTile(view, R.id.more_expenses, R.id.nav_expenses);
        bindTile(view, R.id.more_ledger, R.id.nav_ledger);
        bindTile(view, R.id.more_payment_receipts, R.id.nav_payment_history);
        bindTile(view, R.id.more_maintenance, R.id.nav_maintenance);
        bindTile(view, R.id.more_messages, R.id.nav_messages);
        bindTile(view, R.id.more_notifications, R.id.nav_notifications);
        bindTile(view, R.id.more_reports, R.id.nav_reports);
        bindTile(view, R.id.more_insights, R.id.nav_insights);
        bindTile(view, R.id.more_settings, R.id.nav_settings);
    }

    private void bindTile(View parent, int viewId, int destinationId) {
        View target = parent.findViewById(viewId);
        if (target != null) {
            target.setOnClickListener(v -> Navigation.findNavController(v).navigate(destinationId));
        }
    }
}
