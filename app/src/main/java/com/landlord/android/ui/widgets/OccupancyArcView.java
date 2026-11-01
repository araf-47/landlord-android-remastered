package com.landlord.android.ui.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class OccupancyArcView extends View {

    private Paint trackPaint;
    private Paint progressPaint;
    private Paint centerTextPaint;
    private Paint subTextPaint;

    private float progress = 0f; // 0 to 1
    private int occupiedCount = 0;
    private int totalCount = 0;

    public OccupancyArcView(Context context) {
        super(context);
        init();
    }

    public OccupancyArcView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OccupancyArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(dpToPx(7f));
        trackPaint.setColor(Color.parseColor("#30FFFFFF"));
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(dpToPx(7f));
        progressPaint.setColor(Color.parseColor("#10B981")); // Emerald
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        centerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerTextPaint.setColor(Color.WHITE);
        centerTextPaint.setTextSize(dpToPx(16f));
        centerTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        centerTextPaint.setTextAlign(Paint.Align.CENTER);

        subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subTextPaint.setColor(Color.parseColor("#C7D2FE"));
        subTextPaint.setTextSize(dpToPx(9.5f));
        subTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        subTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setOccupancy(int occupied, int total) {
        this.occupiedCount = occupied;
        this.totalCount = total;
        float target = total > 0 ? (float) occupied / total : 0f;

        ValueAnimator animator = ValueAnimator.ofFloat(progress, target);
        animator.setDuration(950);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float stroke = dpToPx(7f);
        float radius = (Math.min(width, height) / 2f) - stroke;

        float cx = width / 2f;
        float cy = height / 2f;

        RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);

        // Draw background full circle track
        canvas.drawArc(oval, -90, 360, false, trackPaint);

        // Draw animated foreground sweep arc
        float sweepAngle = progress * 360f;
        if (sweepAngle > 0) {
            canvas.drawArc(oval, -90, sweepAngle, false, progressPaint);
        }

        // Draw center text: e.g. "10/12" and "Units"
        String mainText = occupiedCount + "/" + totalCount;
        canvas.drawText(mainText, cx, cy + dpToPx(3f), centerTextPaint);
        canvas.drawText("Units", cx, cy + dpToPx(14f), subTextPaint);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
