package com.landlord.android.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GlowingBezierChartView extends View {

    public static class ChartPoint {
        public final String label;
        public final float value;

        public ChartPoint(String label, float value) {
            this.label = label;
            this.value = value;
        }
    }

    private final List<ChartPoint> points = new ArrayList<>();
    private final Path curvePath = new Path();
    private final Path fillPath = new Path();

    private Paint linePaint;
    private Paint fillPaint;
    private Paint dotPaint;
    private Paint dotInnerPaint;
    private Paint textPaint;
    private Paint gridPaint;
    private Paint scrubberPaint;
    private Paint tooltipBgPaint;
    private Paint tooltipBorderPaint;
    private Paint tooltipTextPaint;

    private int selectedIndex = -1;
    private float touchX = -1;

    public GlowingBezierChartView(Context context) {
        super(context);
        init();
    }

    public GlowingBezierChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GlowingBezierChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null); // Allows glow shadow layer

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dpToPx(3f));
        linePaint.setColor(Color.parseColor("#4F46E5")); // Bright Indigo
        linePaint.setShadowLayer(dpToPx(6f), 0, dpToPx(3f), Color.parseColor("#804F46E5"));

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(Color.parseColor("#4F46E5"));

        dotInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotInnerPaint.setStyle(Paint.Style.FILL);
        dotInnerPaint.setColor(Color.WHITE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setTextSize(dpToPx(11f));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        textPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#E2E8F0"));
        gridPaint.setStrokeWidth(dpToPx(1f));
        gridPaint.setStyle(Paint.Style.STROKE);

        scrubberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scrubberPaint.setColor(Color.parseColor("#818CF8"));
        scrubberPaint.setStrokeWidth(dpToPx(1.5f));
        scrubberPaint.setStyle(Paint.Style.STROKE);
        scrubberPaint.setPathEffect(new DashPathEffect(new float[]{dpToPx(4f), dpToPx(4f)}, 0));

        tooltipBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipBgPaint.setColor(Color.parseColor("#0F172A")); // Obsidian Slate
        tooltipBgPaint.setStyle(Paint.Style.FILL);

        tooltipBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipBorderPaint.setColor(Color.parseColor("#38FFFFFF"));
        tooltipBorderPaint.setStyle(Paint.Style.STROKE);
        tooltipBorderPaint.setStrokeWidth(dpToPx(1f));

        tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipTextPaint.setColor(Color.WHITE);
        tooltipTextPaint.setTextSize(dpToPx(11f));
        tooltipTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);

        // Seed initial default 6-month points
        points.add(new ChartPoint("May", 78000));
        points.add(new ChartPoint("Jun", 95000));
        points.add(new ChartPoint("Jul", 88000));
        points.add(new ChartPoint("Aug", 112000));
        points.add(new ChartPoint("Sep", 108000));
        points.add(new ChartPoint("Oct", 124000));
    }

    public void setData(List<ChartPoint> newPoints) {
        points.clear();
        if (newPoints != null && !newPoints.isEmpty()) {
            points.addAll(newPoints);
        }
        selectedIndex = -1;
        touchX = -1;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (points.size() < 2) return;

        int width = getWidth();
        int height = getHeight();

        float paddingLeft = dpToPx(24f);
        float paddingRight = dpToPx(24f);
        float paddingTop = dpToPx(36f);
        float paddingBottom = dpToPx(32f);

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;
        float baselineY = height - paddingBottom;

        // Draw horizontal grid lines
        canvas.drawLine(paddingLeft, baselineY, width - paddingRight, baselineY, gridPaint);
        canvas.drawLine(paddingLeft, paddingTop + (chartHeight * 0.5f), width - paddingRight, paddingTop + (chartHeight * 0.5f), gridPaint);

        // Find max value for scaling
        float maxVal = 1000f;
        for (ChartPoint p : points) {
            if (p.value > maxVal) maxVal = p.value;
        }
        maxVal *= 1.15f; // Headroom

        int n = points.size();
        float stepX = chartWidth / (n - 1);
        float[] coordsX = new float[n];
        float[] coordsY = new float[n];

        for (int i = 0; i < n; i++) {
            coordsX[i] = paddingLeft + (i * stepX);
            float ratio = points.get(i).value / maxVal;
            coordsY[i] = baselineY - (ratio * chartHeight);
        }

        // Build smooth Cubic Bezier Curve
        curvePath.reset();
        fillPath.reset();

        curvePath.moveTo(coordsX[0], coordsY[0]);
        fillPath.moveTo(coordsX[0], baselineY);
        fillPath.lineTo(coordsX[0], coordsY[0]);

        for (int i = 0; i < n - 1; i++) {
            float p0x = coordsX[i];
            float p0y = coordsY[i];
            float p1x = coordsX[i + 1];
            float p1y = coordsY[i + 1];

            float controlX1 = p0x + (p1x - p0x) / 2f;
            float controlY1 = p0y;
            float controlX2 = p0x + (p1x - p0x) / 2f;
            float controlY2 = p1y;

            curvePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1x, p1y);
            fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1x, p1y);
        }

        fillPath.lineTo(coordsX[n - 1], baselineY);
        fillPath.close();

        // Draw gradient area fill
        fillPaint.setShader(new LinearGradient(
                0, paddingTop,
                0, baselineY,
                new int[]{Color.parseColor("#4D4F46E5"), Color.parseColor("#104F46E5"), Color.parseColor("#004F46E5")},
                new float[]{0f, 0.6f, 1f},
                Shader.TileMode.CLAMP
        ));
        canvas.drawPath(fillPath, fillPaint);

        // Draw glowing Bezier line
        canvas.drawPath(curvePath, linePaint);

        // Draw data points and month labels
        for (int i = 0; i < n; i++) {
            canvas.drawCircle(coordsX[i], coordsY[i], dpToPx(5f), dotPaint);
            canvas.drawCircle(coordsX[i], coordsY[i], dpToPx(2.5f), dotInnerPaint);
            canvas.drawText(points.get(i).label, coordsX[i], height - dpToPx(8f), textPaint);
        }

        // Draw interactive touch scrubber line & floating glass tooltip
        if (selectedIndex >= 0 && selectedIndex < n) {
            float selX = coordsX[selectedIndex];
            float selY = coordsY[selectedIndex];

            // Vertical dashed line
            canvas.drawLine(selX, paddingTop - dpToPx(8f), selX, baselineY, scrubberPaint);

            // Highlight circle
            canvas.drawCircle(selX, selY, dpToPx(7f), linePaint);

            // Floating Frosted Tooltip
            ChartPoint cp = points.get(selectedIndex);
            String tooltipText = cp.label + ": ৳" + String.format(Locale.US, "%,.0f", cp.value);

            float textW = tooltipTextPaint.measureText(tooltipText);
            float tooltipW = textW + dpToPx(20f);
            float tooltipH = dpToPx(26f);

            float ttX = Math.max(dpToPx(12f), Math.min(width - tooltipW - dpToPx(12f), selX - (tooltipW / 2f)));
            float ttY = Math.max(dpToPx(4f), selY - tooltipH - dpToPx(12f));

            RectF ttRect = new RectF(ttX, ttY, ttX + tooltipW, ttY + tooltipH);
            canvas.drawRoundRect(ttRect, dpToPx(8f), dpToPx(8f), tooltipBgPaint);
            canvas.drawRoundRect(ttRect, dpToPx(8f), dpToPx(8f), tooltipBorderPaint);

            canvas.drawText(tooltipText, ttX + (tooltipW / 2f), ttY + dpToPx(17f), tooltipTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (points.size() < 2) return super.onTouchEvent(event);

        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            touchX = event.getX();
            int closest = findClosestPointIndex(touchX);
            if (closest != selectedIndex) {
                selectedIndex = closest;
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                invalidate();
            }
            return true;
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            touchX = -1;
            postDelayed(() -> {
                selectedIndex = -1;
                invalidate();
            }, 2500);
            return true;
        }
        return super.onTouchEvent(event);
    }

    private int findClosestPointIndex(float x) {
        int width = getWidth();
        float paddingLeft = dpToPx(24f);
        float paddingRight = dpToPx(24f);
        float chartWidth = width - paddingLeft - paddingRight;

        int n = points.size();
        float stepX = chartWidth / (n - 1);

        int bestIndex = 0;
        float minDist = Float.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            float px = paddingLeft + (i * stepX);
            float dist = Math.abs(x - px);
            if (dist < minDist) {
                minDist = dist;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
