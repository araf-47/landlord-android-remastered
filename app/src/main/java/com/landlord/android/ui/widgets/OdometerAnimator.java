package com.landlord.android.ui.widgets;

import android.animation.ValueAnimator;
import android.widget.TextView;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import java.util.Locale;

public final class OdometerAnimator {

    private OdometerAnimator() {}

    public static void animateCurrency(TextView textView, double startValue, double targetValue) {
        if (textView == null) return;
        ValueAnimator animator = ValueAnimator.ofFloat((float) startValue, (float) targetValue);
        animator.setDuration(900);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            textView.setText(String.format(Locale.US, "৳%,.0f", val));
        });
        animator.start();
    }

    public static void animateInteger(TextView textView, int startValue, int targetValue) {
        if (textView == null) return;
        ValueAnimator animator = ValueAnimator.ofInt(startValue, targetValue);
        animator.setDuration(800);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            textView.setText(String.valueOf(val));
        });
        animator.start();
    }

    public static void animatePercentage(TextView textView, int startPercent, int targetPercent) {
        if (textView == null) return;
        ValueAnimator animator = ValueAnimator.ofInt(startPercent, targetPercent);
        animator.setDuration(850);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            textView.setText(val + "%");
        });
        animator.start();
    }
}
