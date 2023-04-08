package com.vocalix.app.ui.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class WaveformView extends View {

    private final Paint paint;
    private final ArrayList<Float> amplitudes;
    private final ArrayList<RectF> spikes;

    private final float w = 9f;
    private final float d = 6f;

    private final float sw;

    private final int maxSpikes;

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        amplitudes = new ArrayList<>();
        spikes = new ArrayList<>();

        // Old color = 244, 81, 30
        paint.setColor(Color.rgb(200, 0, 0));

        sw = getResources().getDisplayMetrics().widthPixels;

        maxSpikes = (int) (sw / (w + d));
    }

    public synchronized void addAmplitude(float amp) {
        float sh = 400f;
        float scaledAmplitude = Math.min(amp * sh, sh);
        amplitudes.add(scaledAmplitude);

        spikes.clear();
        List<Float> amps = amplitudes.subList(Math.max(0, amplitudes.size() - maxSpikes), amplitudes.size());
        for (int i = 0; i < amps.size(); i++) {
            float left = sw - i * (w + d);
            float top = sh / 2 - amps.get(i) / 2;
            float right = left + w;
            float bottom = top + amps.get(i);
            spikes.add(new RectF(left, top, right, bottom));
        }

        postInvalidate();
    }

    public synchronized void clear() {
        amplitudes.clear();
        spikes.clear();
        postInvalidate();

    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (RectF spike : spikes) {
            float radius = 6f;
            canvas.drawRoundRect(spike, radius, radius, paint);
        }
    }
}
