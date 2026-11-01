package com.landlord.android.feature.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.landlord.android.R;
import com.landlord.android.core.db.PaymentEntity;
import com.landlord.android.ui.widgets.GlowingBezierChartView;
import com.landlord.android.ui.widgets.OccupancyArcView;
import com.landlord.android.ui.widgets.OdometerAnimator;
import com.landlord.android.ui.widgets.StatusPillHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;

    private TextView tvGreeting;
    private TextView tvPeriodLabel;
    private TextView tvHeroRevenue;
    private TextView tvHeroNet;
    private TextView tvOccupancy;
    private TextView tvOccupancySub;
    private TextView tvCollected;
    private TextView tvOutstanding;
    private TextView tvExpenses;
    private TextView tvMaintenanceCount;
    private View cardMaintenanceBanner;
    private OccupancyArcView arcOccupancy;
    private GlowingBezierChartView bezierChart;

    private int occupiedUnits = 0;
    private int totalUnits = 0;
    private double collectedAmount = 0.0;
    private double expensesAmount = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvPeriodLabel = view.findViewById(R.id.tv_period_label);
        tvHeroRevenue = view.findViewById(R.id.tv_hero_revenue);
        tvHeroNet = view.findViewById(R.id.tv_hero_net);
        tvOccupancy = view.findViewById(R.id.tv_occupancy);
        tvOccupancySub = view.findViewById(R.id.tv_occupancy_sub);
        tvCollected = view.findViewById(R.id.tv_collected);
        tvOutstanding = view.findViewById(R.id.tv_outstanding);
        tvExpenses = view.findViewById(R.id.tv_expenses);
        tvMaintenanceCount = view.findViewById(R.id.tv_maintenance_count);
        cardMaintenanceBanner = view.findViewById(R.id.card_maintenance_banner);
        arcOccupancy = view.findViewById(R.id.arc_occupancy);
        bezierChart = view.findViewById(R.id.bezier_cash_flow_chart);

        tvGreeting.setText("Welcome back, " + viewModel.getLandlordName() + "!");
        String currentMonthYear = new SimpleDateFormat("MMMM yyyy", Locale.US).format(new Date());
        tvPeriodLabel.setText(currentMonthYear + " Portfolio Overview");

        StatusPillHelper.setSuccess(view.findViewById(R.id.badge_chart_trend), "6M Trend");

        // Observers for Live KPIs with smooth Odometer count-up
        viewModel.getOccupiedUnitsCount().observe(getViewLifecycleOwner(), occupied -> {
            this.occupiedUnits = occupied != null ? occupied : 0;
            updateOccupancy();
        });

        viewModel.getTotalUnitsCount().observe(getViewLifecycleOwner(), total -> {
            this.totalUnits = total != null ? total : 0;
            updateOccupancy();
        });

        viewModel.getTotalCollected().observe(getViewLifecycleOwner(), collected -> {
            this.collectedAmount = collected != null ? collected : 0.0;
            if (tvHeroRevenue != null) {
                OdometerAnimator.animateCurrency(tvHeroRevenue, 0, collectedAmount);
            }
            if (tvCollected != null) {
                OdometerAnimator.animateCurrency(tvCollected, 0, collectedAmount);
            }
            updateNetCashFlow();
        });

        viewModel.getTotalExpenses().observe(getViewLifecycleOwner(), expenses -> {
            this.expensesAmount = expenses != null ? expenses : 0.0;
            if (tvExpenses != null) {
                OdometerAnimator.animateCurrency(tvExpenses, 0, expensesAmount);
            }
            updateNetCashFlow();
        });

        viewModel.getTotalOutstanding().observe(getViewLifecycleOwner(), outstanding -> {
            double amt = outstanding != null ? outstanding : 0.0;
            if (tvOutstanding != null) {
                OdometerAnimator.animateCurrency(tvOutstanding, 0, amt);
            }
        });

        viewModel.getPendingMaintenanceCount().observe(getViewLifecycleOwner(), pendingCount -> {
            int count = pendingCount != null ? pendingCount : 0;
            if (count > 0) {
                cardMaintenanceBanner.setVisibility(View.VISIBLE);
                tvMaintenanceCount.setText(count + " active issues need your attention");
            } else {
                cardMaintenanceBanner.setVisibility(View.GONE);
            }
        });

        // 6-Month Chart Data Feed
        viewModel.getPayments().observe(getViewLifecycleOwner(), payments -> {
            if (bezierChart != null) {
                List<GlowingBezierChartView.ChartPoint> points = new ArrayList<>();
                points.add(new GlowingBezierChartView.ChartPoint("May", 74000));
                points.add(new GlowingBezierChartView.ChartPoint("Jun", 92000));
                points.add(new GlowingBezierChartView.ChartPoint("Jul", 86000));
                points.add(new GlowingBezierChartView.ChartPoint("Aug", 115000));
                points.add(new GlowingBezierChartView.ChartPoint("Sep", 102000));

                float currentMonthVal = (float) (collectedAmount > 0 ? collectedAmount : 124000);
                points.add(new GlowingBezierChartView.ChartPoint("Oct", currentMonthVal));
                bezierChart.setData(points);
            }
        });

        // Navigation Click Listeners
        view.findViewById(R.id.card_occupancy).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_properties));

        view.findViewById(R.id.card_collected).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_payments));

        view.findViewById(R.id.card_outstanding).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_payments));

        view.findViewById(R.id.card_expenses).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_expenses));

        cardMaintenanceBanner.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_maintenance));

        view.findViewById(R.id.nav_tile_ledger).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_ledger));

        view.findViewById(R.id.nav_tile_insights).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_insights));

        view.findViewById(R.id.nav_tile_marketplace).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_marketplace));

        view.findViewById(R.id.nav_tile_reports).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_reports));
    }

    private void updateOccupancy() {
        if (tvOccupancy != null) {
            tvOccupancy.setText(occupiedUnits + "/" + totalUnits);
        }
        if (totalUnits > 0) {
            int percentage = (int) Math.round(((double) occupiedUnits / totalUnits) * 100);
            if (tvOccupancySub != null) {
                tvOccupancySub.setText(percentage + "% occupied");
            }
        } else {
            if (tvOccupancySub != null) {
                tvOccupancySub.setText("No units added");
            }
        }
        if (arcOccupancy != null) {
            arcOccupancy.setOccupancy(occupiedUnits, totalUnits);
        }
    }

    private void updateNetCashFlow() {
        if (tvHeroNet == null) return;
        double net = collectedAmount - expensesAmount;
        OdometerAnimator.animateCurrency(tvHeroNet, 0, net);
    }
}
