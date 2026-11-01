package com.landlord.android.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.lifecycle.LifecycleOwner;

public class RequiresInternetBanner extends FrameLayout {

    public RequiresInternetBanner(Context context) {
        super(context);
        setVisibility(GONE);
    }

    public RequiresInternetBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVisibility(GONE);
    }

    public void observe(LifecycleOwner owner) {
        // App is 100% standalone offline - banner remains hidden
        setVisibility(GONE);
    }
}
