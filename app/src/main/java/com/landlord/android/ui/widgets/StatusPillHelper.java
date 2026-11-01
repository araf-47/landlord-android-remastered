package com.landlord.android.ui.widgets;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.landlord.android.R;

public final class StatusPillHelper {

    private StatusPillHelper() {}

    public static void setStatusPill(TextView textView, String statusText, int bgDrawableRes, int dotColorRes, int textColorRes) {
        if (textView == null || statusText == null) return;
        Context context = textView.getContext();
        textView.setBackgroundResource(bgDrawableRes);

        int dotColor = ContextCompat.getColor(context, dotColorRes);
        int textColor = ContextCompat.getColor(context, textColorRes);

        String fullText = "●  " + statusText.trim().toUpperCase();
        SpannableString spannable = new SpannableString(fullText);
        // Slightly enlarged dot for prominent illuminated beacon look
        spannable.setSpan(new RelativeSizeSpan(1.2f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(dotColor), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(textColor), 1, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannable);
    }

    public static void setSuccess(TextView textView, String statusText) {
        setStatusPill(textView, statusText, R.drawable.bg_pill_pulse_success, R.color.color_success, R.color.color_on_success_container);
    }

    public static void setDanger(TextView textView, String statusText) {
        setStatusPill(textView, statusText, R.drawable.bg_pill_pulse_danger, R.color.color_danger, R.color.color_on_danger_container);
    }

    public static void setWarning(TextView textView, String statusText) {
        setStatusPill(textView, statusText, R.drawable.bg_pill_pulse_warning, R.color.color_warning, R.color.color_on_warning_container);
    }

    public static void setInfo(TextView textView, String statusText) {
        setStatusPill(textView, statusText, R.drawable.bg_pill_pulse_info, R.color.color_info, R.color.color_on_info_container);
    }
}
