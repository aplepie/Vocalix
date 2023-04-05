package com.vocalix.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.vocalix.app.R;
import com.vocalix.app.database.model.ExerciseViewModel;

import java.util.Map;

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
        setupExpandableTextView();
        loadExerciseDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        startRecordingButton = findViewById(R.id.start_recording_button);

        ImageButton helpButton = findViewById(R.id.help_button);
        helpButton.setVisibility(View.GONE);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ImageButton backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());
    }

    private void setupButtonClickListener() {
        startRecordingButton.setOnClickListener(v -> {
            Intent intent = new Intent(DetailsActivity.this, RecordingActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void setupExpandableTextView() {
        ExpandableTextView expTv = findViewById(R.id.expand_text_view);
        expTv.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus mollis interdum auctor. In libero ante, placerat ut dolor vitae, porta dignissim augue. Curabitur ultricies sapien ligula, lacinia elementum metus tempus at. Sed cursus vestibulum dui, sed luctus orci lobortis et. Nullam vestibulum mattis mattis. Morbi id lectus et nisi dictum sollicitudin. Phasellus vestibulum scelerisque turpis sit amet ullamcorper. Vestibulum semper massa et mi tincidunt accumsan. Aliquam eu justo vel neque vehicula dignissim eget molestie ligula. Ut et pharetra nulla. Vivamus viverra mollis neque in malesuada. Vivamus tincidunt imperdiet nisl at congue. Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n" +
                "\n" +
                "In hac habitasse platea dictumst. Ut eget ante sit amet urna aliquet faucibus eget at erat. In hac habitasse platea dictumst. Fusce id suscipit eros, sit amet ultrices libero. Sed ut lacus id nisl consectetur viverra at in tortor. Sed erat ante, tempor a tellus a, commodo porta diam. Etiam pretium eleifend arcu id pellentesque.\n" +
                "\n" +
                "Etiam gravida arcu et nibh fermentum, quis congue eros mattis. Ut mollis non lectus nec condimentum. Curabitur magna dolor, luctus at felis sit amet, condimentum egestas augue. Vestibulum consectetur aliquam nisi. Phasellus sit amet odio sed dolor interdum finibus. Vestibulum ultricies consequat justo, quis gravida elit blandit quis. Donec dolor odio, elementum id ultrices a, efficitur ut enim. Proin vitae ipsum massa. Maecenas ultricies rutrum sem, ac rhoncus libero tempor nec. Nam laoreet vel quam cursus scelerisque. Nulla at neque vestibulum, gravida erat ut, maximus tortor. Quisque aliquet ac ex sit amet aliquet. Cras ut bibendum ligula. Nulla sed orci eu dolor maximus scelerisque. Aliquam ut felis sed justo aliquet sodales eget a eros.\n" +
                "\n" +
                "Phasellus eu aliquam magna. Integer massa libero, sagittis quis porttitor non, aliquam eget magna. In ullamcorper magna id ex pharetra, nec fermentum eros mattis. Morbi suscipit nulla a leo congue condimentum. Integer eu turpis facilisis, varius dui ac, maximus ante. Aliquam ullamcorper dignissim tempor. Cras lorem quam, porta non facilisis id, malesuada eu purus. Pellentesque accumsan imperdiet consectetur. Aenean volutpat ipsum quis metus pretium, a dictum augue fermentum. Integer a elit eu sem egestas euismod in at ligula. Praesent id facilisis ex. Aenean placerat, risus id faucibus aliquam, nisi ex placerat urna, quis fringilla justo turpis id diam. Duis lacinia, ligula non pulvinar ullamcorper, ante elit porttitor velit, nec semper est lorem at metus. Suspendisse rhoncus nisl gravida porta hendrerit. Fusce pharetra sapien urna, quis euismod nibh pellentesque at. Pellentesque feugiat, urna ac interdum venenatis, lacus justo facilisis sem, sit amet feugiat felis arcu ut arcu. ");
    }

    private void loadExerciseDetails() {
        exerciseViewModel.getVocalExercises().observe(this, exercises -> {
            for (Map<String, Object> exercise : exercises) {
                if (exercise.get("name").equals(exerciseName)) {
                    // Update TextView fields with exercise details
                    ((TextView) findViewById(R.id.category)).setText((String) exercise.get("type"));
                    ((TextView) findViewById(R.id.exercise_name_tittle)).setText((String) exercise.get("name"));
                    ((TextView) findViewById(R.id.exercise_name)).setText((String) exercise.get("name"));
                    ((TextView) findViewById(R.id.exercise_difficulty)).setText((String) exercise.get("difficulty"));
                    ((TextView) findViewById(R.id.exercise_duration)).setText((String) exercise.get("duration"));
                    break;
                }
            }
        });
    }

}

