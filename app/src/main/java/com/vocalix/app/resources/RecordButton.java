package com.vocalix.app.resources;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class RecordButton extends View {

    private Paint framePaint;
    private Paint buttonPaint;
    private RectF buttonRect;
    private Paint pressedButtonPaint;

    private float isRecordingScale;
    private boolean isPressed;
    private boolean isRecording;

    public RecordButton(Context context) {
        super(context);
        init();
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setColor(Color.WHITE);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(14);

        buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonPaint.setColor(Color.RED);

        pressedButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pressedButtonPaint.setColor(Color.parseColor("#C80000")); // Darker shade of red

        buttonRect = new RectF();

        isRecordingScale = 1.0f;
        isPressed = false;
        isRecording = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float halfStrokeWidth = framePaint.getStrokeWidth() / 2;

        // Draw frame
        canvas.drawOval(halfStrokeWidth, halfStrokeWidth, width - halfStrokeWidth, height - halfStrokeWidth, framePaint);

        // Draw button
        float scalingFactor = 2.3f;

        float radius = (10 + 37 * Math.min(isRecordingScale * 1.88f, 1)) * scalingFactor;
        float buttonScale = 1 - (1 - isRecordingScale) * 0.45f;
        float buttonSize = 78 * buttonScale * scalingFactor;

        buttonRect.set((width - buttonSize) / 2, (height - buttonSize) / 2, (width + buttonSize) / 2, (height + buttonSize) / 2);

        // Change the button color based on the pressed state
        Paint currentButtonPaint = isPressed ? pressedButtonPaint : buttonPaint;

        // Draw button with currentButtonPaint instead of buttonPaint
        canvas.drawRoundRect(buttonRect, radius, radius, currentButtonPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPressed = true;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: // Handle the cancel event to reset the pressed state
                isPressed = false;
                toggleRecording();
                invalidate();
                performClick();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void toggleRecording() {
        isRecording = !isRecording;
        float targetScale = isRecording ? 0.0f : 1.0f;

        ValueAnimator animator = ValueAnimator.ofFloat(isRecordingScale, targetScale);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                isRecordingScale = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }
}
