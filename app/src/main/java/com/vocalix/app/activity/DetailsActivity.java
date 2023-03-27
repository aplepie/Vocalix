package com.vocalix.app.activity;

import android.media.AudioRecord;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.vocalix.app.R;
import com.vocalix.app.resources.RecordButton;

import java.util.Map;
import java.util.Objects;

public class DetailsActivity extends AppCompatActivity {

    private ConstraintLayout bottomSheetContainer;
    private RecordButton recordButton;
    private TextView timeLabel;
    private View audioView;
    private AudioRecord audioRecorder;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ImageView placeImage = findViewById(R.id.place_image);
        TextView placeName = findViewById(R.id.place_name);
        TextView placeLocation = findViewById(R.id.place_location);
        TextView placePrice = findViewById(R.id.place_price);
        TextView placeDetails = findViewById(R.id.place_details);

        // Assuming you pass a Map<String, Object> representing the place as an extra in the Intent
        @SuppressWarnings("unchecked")
        Map<String, Object> place = (Map<String, Object>) getIntent().getSerializableExtra("place");

        placeName.setText((String) place.get("name"));
        placeLocation.setText((String) place.get("location"));
        placePrice.setText((String) place.get("price"));
        placeDetails.setText((String) place.get("details"));

        Glide.with(this).load((String) place.get("img")).into(placeImage);

        recordButton = findViewById(R.id.recordButton);
        timeLabel = new TextView(this);
        audioView = new View(this);

        timeLabel.setX(recordButton.getX());
        timeLabel.setY(recordButton.getY() - 32);
        timeLabel.setText("00.00");
        timeLabel.setTextColor(getResources().getColor(R.color.blue_gray_300));
        timeLabel.setVisibility(View.GONE);

        // Initialize and configure the audioView
        // The exact implementation will depend on the AudioVisualizerView class
        audioView.setVisibility(View.GONE);

        bottomSheetContainer = findViewById(R.id.bottom_sheet_container);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRecording();
            }
        });
    }

    private void toggleRecording() {
        isRecording = !isRecording;

        if (isRecording) {
            // Start recording, show time label and audio view
            timeLabel.setVisibility(View.VISIBLE);
            audioView.setVisibility(View.VISIBLE);
            startRecording();
        } else {
            // Stop recording, hide time label and audio view
            timeLabel.setVisibility(View.GONE);
            audioView.setVisibility(View.GONE);
            stopRecording();
        }
    }

    private void startRecording() {
        // Implement the logic for starting the recording
    }

    private void stopRecording() {
        // Implement the logic for stopping the recording
    }
}

