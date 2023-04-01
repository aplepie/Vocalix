package com.vocalix.app.activity;

import android.animation.ValueAnimator;
import android.media.AudioRecord;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.vocalix.app.R;
import com.vocalix.app.resources.RecordButton;

public class DetailsActivity extends AppCompatActivity {

    private ConstraintLayout bottomSheet;
    private RecordButton recordButton;
    private AudioRecord audioRecorder;
    private boolean isBottomSheetExpanded = false;
    private boolean isRecording = false;
    private int bottomSheetHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageButton backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());

        /*
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
         */

        ExpandableTextView expTv = findViewById(R.id.expand_text_view);
        expTv.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus mollis interdum auctor. In libero ante, placerat ut dolor vitae, porta dignissim augue. Curabitur ultricies sapien ligula, lacinia elementum metus tempus at. Sed cursus vestibulum dui, sed luctus orci lobortis et. Nullam vestibulum mattis mattis. Morbi id lectus et nisi dictum sollicitudin. Phasellus vestibulum scelerisque turpis sit amet ullamcorper. Vestibulum semper massa et mi tincidunt accumsan. Aliquam eu justo vel neque vehicula dignissim eget molestie ligula. Ut et pharetra nulla. Vivamus viverra mollis neque in malesuada. Vivamus tincidunt imperdiet nisl at congue. Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n" +
                "\n" +
                "In hac habitasse platea dictumst. Ut eget ante sit amet urna aliquet faucibus eget at erat. In hac habitasse platea dictumst. Fusce id suscipit eros, sit amet ultrices libero. Sed ut lacus id nisl consectetur viverra at in tortor. Sed erat ante, tempor a tellus a, commodo porta diam. Etiam pretium eleifend arcu id pellentesque.\n" +
                "\n" +
                "Etiam gravida arcu et nibh fermentum, quis congue eros mattis. Ut mollis non lectus nec condimentum. Curabitur magna dolor, luctus at felis sit amet, condimentum egestas augue. Vestibulum consectetur aliquam nisi. Phasellus sit amet odio sed dolor interdum finibus. Vestibulum ultricies consequat justo, quis gravida elit blandit quis. Donec dolor odio, elementum id ultrices a, efficitur ut enim. Proin vitae ipsum massa. Maecenas ultricies rutrum sem, ac rhoncus libero tempor nec. Nam laoreet vel quam cursus scelerisque. Nulla at neque vestibulum, gravida erat ut, maximus tortor. Quisque aliquet ac ex sit amet aliquet. Cras ut bibendum ligula. Nulla sed orci eu dolor maximus scelerisque. Aliquam ut felis sed justo aliquet sodales eget a eros.\n" +
                "\n" +
                "Phasellus eu aliquam magna. Integer massa libero, sagittis quis porttitor non, aliquam eget magna. In ullamcorper magna id ex pharetra, nec fermentum eros mattis. Morbi suscipit nulla a leo congue condimentum. Integer eu turpis facilisis, varius dui ac, maximus ante. Aliquam ullamcorper dignissim tempor. Cras lorem quam, porta non facilisis id, malesuada eu purus. Pellentesque accumsan imperdiet consectetur. Aenean volutpat ipsum quis metus pretium, a dictum augue fermentum. Integer a elit eu sem egestas euismod in at ligula. Praesent id facilisis ex. Aenean placerat, risus id faucibus aliquam, nisi ex placerat urna, quis fringilla justo turpis id diam. Duis lacinia, ligula non pulvinar ullamcorper, ante elit porttitor velit, nec semper est lorem at metus. Suspendisse rhoncus nisl gravida porta hendrerit. Fusce pharetra sapien urna, quis euismod nibh pellentesque at. Pellentesque feugiat, urna ac interdum venenatis, lacus justo facilisis sem, sit amet feugiat felis arcu ut arcu. ");
        /*
        recordButton = findViewById(R.id.recordButton);
        bottomSheet = findViewById(R.id.bottom_sheet_container);

        // Add a global layout listener to the bottom sheet to get its height after the layout is
        // completed
        bottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Check if the bottomSheetHeight is not set yet (0 by default) and set it to the
                // current height of the bottom sheet
                if (bottomSheetHeight == 0) {
                    bottomSheetHeight = bottomSheet.getHeight();
                }

                // Remove the global layout listener to avoid multiple calls and to prevent memory
                // leaks
                bottomSheet.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentHeight;
                int targetHeight;

                if (isBottomSheetExpanded) {
                    currentHeight = bottomSheetHeight * 3;
                    targetHeight = bottomSheetHeight;
                } else {
                    currentHeight = bottomSheetHeight;
                    targetHeight = bottomSheetHeight * 3;
                }

                // Animate the corner rounding
                int animationDrawableResId = isBottomSheetExpanded ? R.drawable.bottom_sheet_animation_reverse : R.drawable.bottom_sheet_animation;
                AnimationDrawable cornerAnimation = (AnimationDrawable) ResourcesCompat.getDrawable(getResources(), animationDrawableResId, getTheme());
                bottomSheet.setBackground(cornerAnimation);
                cornerAnimation.start();

                animateBottomSheetHeight(currentHeight, targetHeight);
                isBottomSheetExpanded = !isBottomSheetExpanded;

                toggleRecording();
            }
        });
         */
    }

    private void animateBottomSheetHeight(int fromHeight, int toHeight) {
        ValueAnimator heightAnimator = ValueAnimator.ofInt(fromHeight, toHeight);
        heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        heightAnimator.setDuration(300); // Animation duration in milliseconds

        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int newHeight = (int) animation.getAnimatedValue();
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) bottomSheet.getLayoutParams();
                layoutParams.height = newHeight;
                bottomSheet.setLayoutParams(layoutParams);
            }
        });

        heightAnimator.start();
    }

    private void toggleRecording() {
        TextView titleText = findViewById(R.id.title_text);
        TextView timeText = findViewById(R.id.time_text);

        isRecording = !isRecording;

        if (isRecording) {
            // Animate the visibility of the TextViews
            titleText.setVisibility(View.VISIBLE);
            timeText.setVisibility(View.VISIBLE);

            titleText.setAlpha(0f);
            timeText.setAlpha(0f);

            titleText.animate().alpha(1f).setDuration(500).start();
            timeText.animate().alpha(1f).setDuration(500).start();

            startRecording();
        } else {
            // Stop recording, hide time label and audio view
            titleText.setVisibility(View.GONE);
            timeText.setVisibility(View.GONE);

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

