package com.vocalix.app.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vocalix.app.R;

public class InstructionView extends LinearLayout {
    private TextView stepNumberTextView;
    private TextView instructionTextView;

    public InstructionView(Context context) {
        super(context);
        init(context);
    }

    public InstructionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public InstructionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater.from(context).inflate(R.layout.instruction_view, this, true);
        stepNumberTextView = findViewById(R.id.step_number);
        instructionTextView = findViewById(R.id.instruction);
    }

    public void setStepNumber(int stepNumber) {
        stepNumberTextView.setText(String.format("%02d ", stepNumber));
    }

    public void setInstructionText(String instructionText) {
        instructionTextView.setText(instructionText);
    }
}
