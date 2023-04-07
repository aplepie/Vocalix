package com.vocalix.app.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.vocalix.app.R;
import com.vocalix.app.database.model.ExerciseViewModel;

import java.util.Map;
import java.util.Objects;

public class DetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FrameLayout startRecordingButton;
    private ExerciseViewModel exerciseViewModel;
    private String exerciseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);
        exerciseName = getIntent().getStringExtra("exerciseName");

        initViews();
        setupToolbar();
        setupButtonClickListener();
        loadExerciseDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        startRecordingButton = findViewById(R.id.start_recording_button);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ImageButton backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());
    }

    private void setupButtonClickListener() {
        startRecordingButton.setOnClickListener(v -> {
            Intent intent = new Intent(DetailsActivity.this, RecordingActivity.class);
            intent.putExtra("exerciseName", exerciseName);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void loadExerciseDetails() {
        exerciseViewModel.getVocalExercises().observe(this, exercises -> {
            for (Map<String, Object> exercise : exercises) {
                if (Objects.equals(exercise.get("name"), exerciseName)) {
                    // Update TextView fields with exercise details
                    TextView categoryTextView = findViewById(R.id.category);
                    updateLabelWidth(exercise, categoryTextView);

                    ((TextView) findViewById(R.id.exercise_name_tittle)).setText((String) exercise.get("name"));
                    ((TextView) findViewById(R.id.exercise_name)).setText((String) exercise.get("name"));
                    ((TextView) findViewById(R.id.exercise_difficulty)).setText((String) exercise.get("difficulty"));
                    ((TextView) findViewById(R.id.exercise_duration)).setText((String) exercise.get("duration"));

                    // Update ExpandableTextView with the exercise description
                    ExpandableTextView expTv = findViewById(R.id.expand_text_view);
                    String exerciseDescription = (String) exercise.get("description");
                    expTv.setText(exerciseDescription);

                    break;
                }
            }
        });
    }

    private void updateLabelWidth(Map<String, Object> exercise, TextView categoryTextView) {
        String categoryText = ((String) Objects.requireNonNull(exercise.get("type"))).toUpperCase();
        categoryTextView.setText(categoryText);

        // Set the arrow background drawable
        Drawable arrowDrawable = ContextCompat.getDrawable(this, R.drawable.arrow_background);
        categoryTextView.setBackground(arrowDrawable);

        // Add padding to the TextView to position the text within the arrow background
        int paddingHorizontal = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        int paddingVertical = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        categoryTextView.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
    }
}

