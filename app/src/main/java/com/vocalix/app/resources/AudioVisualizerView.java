package com.vocalix.app.resources;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.vocalix.app.R;

public class AudioVisualizerView extends View {

    private float barWidth = 4.0f;
    private boolean active = false;
    private int color = ContextCompat.getColor(getContext(), R.color.blue_gray_300);
    private int[] waveforms = new int[100];
    private Paint paint;

    public AudioVisualizerView(Context context) {
        super(context);
        init();
    }

    public AudioVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AudioVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(0x00000000);
        paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
    }

    public void setActive(boolean active) {
        this.active = active;
        color = active ? ContextCompat.getColor(getContext(),
                R.color.color_accent) : ContextCompat.getColor(getContext(), R.color.blue_gray_300);
        paint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(0x00000000);
        float w = getWidth();
        float h = getHeight();
        int t = (int) (w / barWidth);
        int s = Math.max(0, waveforms.length - t);
        float m = h / 2;
        float r = barWidth / 2;
        float x = m - r;
        float bar = 0;

        for (int i = s; i < waveforms.length; i++) {
            float v = h * waveforms[i] / 50.0f;

            if (v > x) {
                v = x;
            } else if (v < 3) {
                v = 3;
            }

            float oneX = bar * barWidth;
            float oneY;
            float twoX = oneX + r;
            float twoY;
            float twoS;
            float twoE;
            boolean twoC;
            float threeX = twoX + r;
            float threeY = m;

            if (i % 2 == 1) {
                oneY = m - v;
                twoY = m - v;
                twoS = (float) Math.toRadians(-180);
                twoE = 0;
                twoC = false;
            } else {
                oneY = m + v;
                twoY = m + v;
                twoS = (float) Math.toRadians(180);
                twoE = 0;
                twoC = true;
            }

            Path path = new Path();
            path.moveTo(oneX, m);
            path.lineTo(oneX, oneY);
            path.arcTo(new RectF(twoX - r, twoY - r, twoX + r, twoY + r), twoS, twoE - twoS, false);
            path.lineTo(threeX, threeY);

            canvas.drawPath(path, paint);
            bar += 1;
        }
    }
}
