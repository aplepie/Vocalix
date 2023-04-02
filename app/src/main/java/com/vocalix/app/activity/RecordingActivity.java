package com.vocalix.app.activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.vocalix.app.R;
import com.vocalix.app.adapter.RecordingAdapter;
import com.vocalix.app.model.AppDatabase;
import com.vocalix.app.model.Recording;
import com.vocalix.app.resources.RecordButton;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordingActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ConstraintLayout bottomSheet;
    private RecordButton recordButton;
    private boolean isBottomSheetExpanded = false;
    private boolean isRecording = false;
    private int bottomSheetHeight;

    private RecordingAdapter recordingAdapter;
    private List<Recording> recordingList;
    private AppDatabase appDatabase;

    private long recordingStartTime;
    private AudioRecord audioRecord;
    private Thread recordingThread;
    private String currentRecordingFilePath;
    private int bufferSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }

        initViews();
        setupToolbar();
        setupRecordButton();
        initRecyclerView();
        updateRecordingListVisibility();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRecordingListVisibility();
        fetchRecordings();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recordButton = findViewById(R.id.recordButton);
        bottomSheet = findViewById(R.id.bottom_sheet_container);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ImageButton backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());
    }

    private void updateRecordingListVisibility() {
        ScrollView pastRecordingsScrollView = findViewById(R.id.past_recordings_scroll_view);
        ImageView emptyStateImage = findViewById(R.id.empty_state_image);
        TextView emptyStateText = findViewById(R.id.empty_state_text);
        TextView emptyStateDescription = findViewById(R.id.empty_state_description);

        if (!recordingList.isEmpty()) {
            pastRecordingsScrollView.setVisibility(View.VISIBLE);
            emptyStateImage.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.GONE);
            emptyStateDescription.setVisibility(View.GONE);
        } else {
            pastRecordingsScrollView.setVisibility(View.GONE);
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

            assert cornerAnimation != null;
            cornerAnimation.start();

            animateBottomSheetHeight(currentHeight, targetHeight);
            isBottomSheetExpanded = !isBottomSheetExpanded;

            toggleRecording();
        });
    }

    private void animateBottomSheetHeight(int fromHeight, int toHeight) {
        ValueAnimator heightAnimator = ValueAnimator.ofInt(fromHeight, toHeight);
        heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        heightAnimator.setDuration(300); // Animation duration in milliseconds

        heightAnimator.addUpdateListener(animation -> {
            int newHeight = (int) animation.getAnimatedValue();
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

            isRecording = false;
        } else {
            // Animate the visibility of the TextViews
            titleText.setVisibility(View.VISIBLE);
            timeText.setVisibility(View.VISIBLE);

            titleText.setAlpha(0f);
            timeText.setAlpha(0f);

            titleText.animate().alpha(1f).setDuration(500).start();
            timeText.animate().alpha(1f).setDuration(500).start();

            startRecording();

            isRecording = true;
        }
    }

    private void startRecording() {
        bufferSize = AudioRecord.getMinBufferSize(44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
                String recordingFileName = "VOCALIX_" + timeStamp + ".pcm";
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                currentRecordingFilePath = new File(storageDir, recordingFileName).getAbsolutePath();
                FileOutputStream fos = new FileOutputStream(currentRecordingFilePath);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                DataOutputStream dos = new DataOutputStream(bos);

                byte[] buffer = new byte[bufferSize];
                while (isRecording) {
                    int bytesRead = audioRecord.read(buffer, 0, buffer.length);
                    for (int i = 0; i < bytesRead; i++) {
                        dos.writeByte(buffer[i]);
                    }

                    // Process the audio data in real-time here
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

            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();

            // Convert the PCM file to an MP3 file
            String mp3OutputPath = currentRecordingFilePath.replace(".pcm", ".mp3");
            convertPcmToMp3(currentRecordingFilePath, mp3OutputPath);

            Log.i("RecordingActivity", "Recording file path: " + currentRecordingFilePath);
            Log.i("RecordingActivity", "mp3OutputPath file path: " + mp3OutputPath);

            // Save the recording to the database
            String recordingName = "VOCALIX_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            long duration = System.currentTimeMillis() - recordingStartTime;
            Recording recording = new Recording(0, recordingName, mp3OutputPath, duration, new Date());

            new Thread(() -> {
                appDatabase.recordingDao().insert(recording);
                fetchRecordings();
            }).start();
        } else {
            Toast.makeText(this, "No recording to stop", Toast.LENGTH_SHORT).show();
        }
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

        if (appDatabase == null) {
            appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "recordings-db").build();
        }

        new Thread(() -> {
            recordingList.clear();
            recordingList.addAll(appDatabase.recordingDao().getAll());

            runOnUiThread(() -> {
                recordingAdapter.notifyDataSetChanged();
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
}
