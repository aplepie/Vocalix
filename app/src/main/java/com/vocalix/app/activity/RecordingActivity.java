package com.vocalix.app.activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.google.android.material.snackbar.Snackbar;
import com.vocalix.app.R;
import com.vocalix.app.database.adapter.RecordingAdapter;
import com.vocalix.app.database.AppDatabase;
import com.vocalix.app.database.entity.Recording;
import com.vocalix.app.database.model.ExerciseViewModel;
import com.vocalix.app.database.model.UserViewModel;
import com.vocalix.app.ui.customviews.InstructionView;
import com.vocalix.app.ui.customviews.RecordButton;
import com.vocalix.app.ui.customviews.WaveformView;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class RecordingActivity extends AppCompatActivity {

    private static final float BOTTOM_SHEET_HEIGHT = 3.75f;

    private String exerciseName;

    private Toolbar toolbar;
    private ConstraintLayout bottomSheet;
    private LinearLayout instructionsContainer;
    private RecordButton recordButton;
    private boolean isBottomSheetExpanded = false;
    private boolean isRecording = false;
    private int bottomSheetHeight;
    private AtomicReference<String> userIdentifier;

    private RecordingAdapter recordingAdapter;
    private List<Recording> recordingList;
    private AppDatabase appDatabase;

    private long recordingStartTime;
    private AudioRecord audioRecord;
    private Thread recordingThread;
    private String currentRecordingFilePath;
    private int bufferSize;

    private WaveformView waveformView;
    private float maxAmplitude;
    private float amplitudeSum = 0;
    private int amplitudeCount = 0;
    private Handler timeHandler;
    private Runnable updateTimeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        initializeVariables();
        checkAndRequestAudioPermission();
        getIdFromViewModel();

        initViews();
        setupToolbar();
        setupRecordButton();
        initRecyclerView();

        // Call the loadInstructions method with the loaded instructions
        loadExerciseDetails(this::loadInstructions);

        updateRecordingListVisibility();
        initHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRecordingListVisibility();
        fetchRecordings();
    }

    @SuppressLint("HardwareIds")
    private void initializeVariables() {
        userIdentifier = new AtomicReference<>(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        exerciseName = getIntent().getStringExtra("exerciseName");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recordButton = findViewById(R.id.recordButton);
        bottomSheet = findViewById(R.id.bottom_sheet_container);
        waveformView = findViewById(R.id.waveform_view);
        instructionsContainer = findViewById(R.id.instructions_container);

        // Set the fixed width for the time_text TextView
        TextView timeText = findViewById(R.id.time_text);
        int maxTimeTextWidth = getMaxTimeTextWidth();
        timeText.setWidth(maxTimeTextWidth);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ImageButton backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());
    }

    private void checkAndRequestAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 100);
        }
    }

    private void updateRecordingListVisibility() {
        ImageView emptyStateImage = findViewById(R.id.empty_state_image);
        TextView emptyStateText = findViewById(R.id.empty_state_text);
        TextView emptyStateDescription = findViewById(R.id.empty_state_description);

        if (!recordingList.isEmpty()) {
            emptyStateImage.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.GONE);
            emptyStateDescription.setVisibility(View.GONE);
        } else {
            emptyStateImage.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateDescription.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecordButton() {
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

        recordButton.setOnClickListener(v -> {
            float currentHeight;
            float targetHeight;

            if (isBottomSheetExpanded) {
                currentHeight = bottomSheetHeight * BOTTOM_SHEET_HEIGHT;
                targetHeight = bottomSheetHeight;
            } else {
                currentHeight = bottomSheetHeight;
                targetHeight = bottomSheetHeight * BOTTOM_SHEET_HEIGHT;
            }

            // Animate the corner rounding
            int animationDrawableResId = isBottomSheetExpanded ? R.drawable.bottom_sheet_animation_reverse : R.drawable.bottom_sheet_animation;
            AnimationDrawable cornerAnimation = (AnimationDrawable) ResourcesCompat.getDrawable(getResources(), animationDrawableResId, getTheme());
            bottomSheet.setBackground(cornerAnimation);

            assert cornerAnimation != null;
            cornerAnimation.start();

            animateBottomSheetHeight(currentHeight, targetHeight);
            isBottomSheetExpanded = !isBottomSheetExpanded;

            toggleRecording();
        });
    }

    private void animateBottomSheetHeight(float fromHeight, float toHeight) {
        ValueAnimator heightAnimator = ValueAnimator.ofFloat(fromHeight, toHeight);
        heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        heightAnimator.setDuration(300); // Animation duration in milliseconds

        heightAnimator.addUpdateListener(animation -> {
            int newHeight = Math.round((float) animation.getAnimatedValue());

            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) bottomSheet.getLayoutParams();
            layoutParams.height = newHeight;
            bottomSheet.setLayoutParams(layoutParams);
        });

        heightAnimator.start();
    }

    private void toggleRecording() {
        TextView titleText = findViewById(R.id.title_text);
        TextView timeText = findViewById(R.id.time_text);

        if (isRecording) {
            // Stop recording, hide time label and audio view
            stopRecording();

            titleText.setVisibility(View.GONE);
            timeText.setVisibility(View.GONE);
            waveformView.setVisibility(View.GONE);

            isRecording = false;
        } else {
            // Animate the visibility of the TextViews
            titleText.setVisibility(View.VISIBLE);
            timeText.setVisibility(View.VISIBLE);
            waveformView.setVisibility(View.VISIBLE);

            titleText.setAlpha(0f);
            timeText.setAlpha(0f);
            waveformView.setAlpha(0f);

            titleText.animate().alpha(1f).setDuration(500).start();
            timeText.animate().alpha(1f).setDuration(500).start();
            waveformView.animate().alpha(1f).setDuration(500).start();

            startRecording();

            isRecording = true;
        }
    }

    private void getIdFromViewModel() {
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getUser().observe(this, user -> {
            if (user != null) {
                userIdentifier.set(user.getIdentifier());
            }
        });
    }

    private void startRecording() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 100);
            return;
        }

        bufferSize = AudioRecord.getMinBufferSize(44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        audioRecord.startRecording();
        isRecording = true;

        recordingThread = new Thread(() -> {
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String recordingFileName = userIdentifier.get() + "_" + timeStamp + ".pcm";

                currentRecordingFilePath = new File(getCacheDir(), recordingFileName).getAbsolutePath();
                FileOutputStream fos = new FileOutputStream(currentRecordingFilePath);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                DataOutputStream dos = new DataOutputStream(bos);

                maxAmplitude = Float.MIN_VALUE;

                byte[] buffer = new byte[bufferSize];
                while (isRecording) {
                    int bytesRead = audioRecord.read(buffer, 0, buffer.length);

                    for (int i = 0; i < bytesRead; i++) {
                        dos.writeByte(buffer[i]);
                    }

                    // Process the audio data in real-time
                    short[] shortBuffer = new short[bufferSize / 2];
                    ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortBuffer);

                    float sum = 0;

                    for (short value : shortBuffer) {
                        sum += Math.abs(value);
                        maxAmplitude = Math.max(maxAmplitude, Math.abs(value));
                    }

                    float amplitude = sum / shortBuffer.length;

                    amplitudeSum += amplitude;
                    amplitudeCount++;

                    // Calculate the average amplitude
                    float averageAmplitude = amplitudeSum / amplitudeCount;

                    // Set a dynamic threshold value based on the average amplitude
                    float dynamicThreshold = averageAmplitude * 6.5f;

                    // Normalize the amplitude by dividing it by the current maximum amplitude or dynamic threshold, whichever is smaller
                    float normalizedAmplitude = amplitude / Math.min(maxAmplitude, dynamicThreshold);

                    // Apply an exponent to emphasize lower amplitudes (change the exponent value as needed)
                    float scaledAmplitude = (float) Math.pow(normalizedAmplitude, 0.5);

                    // Fade-in effect: reduce the amplitude during the first 500ms of the recording
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - recordingStartTime < 500) {
                        scaledAmplitude *= (currentTime - recordingStartTime) / 500.0f;
                    }

                    waveformView.addAmplitude(scaledAmplitude);
                }

                dos.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        recordingThread.start();
        recordingStartTime = System.currentTimeMillis();
        Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();

        // Start updating the recording time
        timeHandler.post(updateTimeRunnable);
    }

    private void stopRecording() {
        if (audioRecord != null) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;

            // Stop the recording thread
            try {
                recordingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recordingThread = null;

            // Stop updating the recording time
            timeHandler.removeCallbacks(updateTimeRunnable);

            waveformView.clear();

            handleRecordingCompletion();
        } else {
            Toast.makeText(this, "No recording to stop", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRecordingCompletion() {
        // Calculate the percentage of the maximum possible amplitude (32,767 for 16-bit audio)
        float maxAmplitudePercentage = (maxAmplitude / 32767) * 100;

        // Set thresholds as percentages of the maximum amplitude
        float lowAmplitudePercentage = 15; // 15% of maximum amplitude
        float saturationPercentage = 85; // 85% of maximum amplitude

        boolean isLowAmplitude = maxAmplitudePercentage < lowAmplitudePercentage;
        boolean isSaturated = maxAmplitudePercentage > saturationPercentage;

        if (isLowAmplitude || isSaturated) {
            // Ask the user to repeat the recording and delete the file
            File fileToDelete = new File(currentRecordingFilePath);

            if (fileToDelete.exists()) {
                boolean success = fileToDelete.delete();

                if (!success) {
                    Log.w(TAG, "Failed to delete recording file: " + currentRecordingFilePath);
                }
            }

            // Show a message to the user indicating the need to repeat the recording
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "La grabación tiene un nivel muy bajo de sonido o hay indicios de que pueda estar saturada. Por favor, repita la grabación.",
                    Snackbar.LENGTH_LONG);

            // Customize the Snackbar (Optional)
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.setTextColor(Color.WHITE);
            snackbar.setBackgroundTint(Color.DKGRAY);
            snackbar.setAction("OK", v -> snackbar.dismiss());

            // Show the Snackbar
            snackbar.show();

        } else {
            // Save the recording
            saveRecording();
        }
    }

    private void saveRecording() {
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();

        // Convert the PCM file to an MP3 file (API level 28 and below)
        String recordingFileName = new File(currentRecordingFilePath).getName();
        String mp3OutputPath = new File(getCacheDir(), recordingFileName.replace(".pcm", ".mp3")).getAbsolutePath();
        convertPcmToMp3(currentRecordingFilePath, mp3OutputPath);

        // Save the recording to the database
        String recordingName = new File(currentRecordingFilePath).getName();
        long duration = System.currentTimeMillis() - recordingStartTime;
        Recording recording = new Recording(0, recordingName, mp3OutputPath, duration, new Date());

        // TODO: Get the exercise code from the exercise
        generateScore(recording);

        new Thread(() -> {
            appDatabase.recordingDao().insert(recording);
            fetchRecordings();
        }).start();

        // Save the recording to the gallery
        saveRecordingToGallery(mp3OutputPath);
    }

    private void convertPcmToMp3(String inputPath, String outputPath) {
        String[] command = {"-y", "-f", "s16le", "-ar", "44100", "-ac", "1", "-i", inputPath, "-b:a", "128k", outputPath};
        String commandString = TextUtils.join(" ", command);
        FFmpegSession session = FFmpegKit.execute(commandString);

        if (ReturnCode.isSuccess(session.getReturnCode())) {
            Log.i("FFmpegKit", "PCM to MP3 conversion successful.");
        } else {
            Log.e("FFmpegKit", "PCM to MP3 conversion failed.");
        }
    }

    private void initRecyclerView() {
        RecyclerView pastRecordingsRecyclerView = findViewById(R.id.past_recordings_recycler_view);
        pastRecordingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        recordingList = new ArrayList<>();
        recordingAdapter = new RecordingAdapter(this, recordingList);
        pastRecordingsRecyclerView.setAdapter(recordingAdapter);
    }

    private void fetchRecordings() {
        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "recordings-db").build();

        new Thread(() -> {
            List<Recording> recordings = appDatabase.recordingDao().getAllByDateDescending();

            runOnUiThread(() -> {
                int oldSize = recordingList.size();
                recordingList.clear();

                if (oldSize > 0) {
                    recordingAdapter.notifyItemRangeRemoved(0, oldSize);
                }

                recordingList.addAll(recordings);
                int newSize = recordingList.size();

                if (newSize > 0) {
                    recordingAdapter.notifyItemRangeInserted(0, newSize);
                }

                updateRecordingListVisibility();
            });
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void saveRecordingToGallery(String recordingFilePath) {
        File recordingFile = new File(recordingFilePath);
        String mimeType = "audio/mp3";
        String fileName = recordingFile.getName();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        contentValues.put(MediaStore.Audio.AudioColumns.ARTIST, "Vocalix");
        contentValues.put(MediaStore.Audio.AudioColumns.ALBUM, "Vocalix Recordings");
        contentValues.put(MediaStore.Audio.AudioColumns.TITLE, fileName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/VocalixRecordings");
            contentValues.put(MediaStore.Audio.Media.IS_PENDING, 1);
        } else {
            contentValues.put(MediaStore.MediaColumns.DATA, Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/VocalixRecordings/" + fileName);
        }

        Uri uri = getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);

        try (InputStream inputStream = Files.newInputStream(recordingFile.toPath());
             OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear();
            contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0);
            getContentResolver().update(uri, contentValues, null, null);
        }

        boolean isDeleted = new File(recordingFilePath).delete();

        if (!isDeleted) {
            Log.e("saveRecordingToGallery", "Failed to delete the temporary recording file: " + recordingFilePath);
        }
    }

    private void generateScore(@NonNull Recording recording) {
        // In the future, replace this implementation with analysis module
        SecureRandom random = new SecureRandom();

        // Generates a random integer between 0 (inclusive) and 32 (inclusive)
        int randomNumber = random.nextInt(33);
        recording.setScore(Integer.toString(randomNumber));
    }

    private void initHandler() {
        timeHandler = new Handler(Looper.getMainLooper());
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateRecordingTime();
                timeHandler.postDelayed(this, 50); // Update every 50 milliseconds
            }
        };
    }

    private void updateRecordingTime() {
        TextView timeText = findViewById(R.id.time_text);

        long elapsedTime = System.currentTimeMillis() - recordingStartTime;
        int minutes = (int) (elapsedTime / 60000);
        int seconds = (int) ((elapsedTime % 60000) / 1000);
        int milliseconds = (int) (elapsedTime % 1000) / 10;

        String timeString = String.format(Locale.getDefault(), "%02d:%02d,%02d", minutes, seconds, milliseconds);
        timeText.setText(timeString);
    }

    private int getMaxTimeTextWidth() {
        Paint textPaint = new Paint();
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));

        String maxTimeString = "99:59,99";
        Rect textBounds = new Rect();
        textPaint.getTextBounds(maxTimeString, 0, maxTimeString.length(), textBounds);

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()); // Add 4dp padding

        return textBounds.width() + padding;
    }

    private void loadInstructions(List<String> instructions) {
        for (int i = 0; i < instructions.size(); i++) {
            InstructionView stepView = new InstructionView(this);
            stepView.setStepNumber(i + 1);
            stepView.setInstructionText(instructions.get(i));
            instructionsContainer.addView(stepView);

            if (i < instructions.size() - 1) {
                Space space = new Space(this);
                space.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (int) getResources().getDisplayMetrics().density * 15
                ));

                instructionsContainer.addView(space);
            }
        }
    }

    private void loadExerciseDetails(ExerciseDetailsCallback callback) {
        ExerciseViewModel exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);

        exerciseViewModel.getVocalExercises().observe(this, exercises -> {
            for (Map<String, Object> exercise : exercises) {
                if (Objects.equals(exercise.get("name"), exerciseName)) {
                    @SuppressWarnings("unchecked")
                    List<String> instructions = (List<String>) exercise.get("instructions");
                    callback.onExerciseDetailsLoaded(instructions);

                    break;
                }
            }
        });
    }

    public interface ExerciseDetailsCallback {
        void onExerciseDetailsLoaded(List<String> instructions);
    }
}
