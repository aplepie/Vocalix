package com.vocalix.app.database.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.vocalix.app.R;
import com.vocalix.app.activity.RecordingActivity;
import com.vocalix.app.database.AppDatabase;
import com.vocalix.app.database.entity.Recording;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder> {
    private final List<Recording> recordings;
    private final Context context;

    public RecordingAdapter(Context context, List<Recording> recordings) {
        this.context = context;
        this.recordings = recordings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recording_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recording recording = recordings.get(position);

        // Set the recording_title
        holder.recordingTitle.setText(recording.getName());

        // Set the recording_date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = dateFormat.format(recording.getDate());
        holder.recordingDate.setText(dateString);

        // Set the recording_duration
        long minutes = TimeUnit.MILLISECONDS.toMinutes(recording.getDuration());
        long seconds = TimeUnit.MILLISECONDS.toSeconds(recording.getDuration()) % 60;
        String durationString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        holder.recordingDuration.setText(durationString);

        // Set the score_indicator (assuming it's a number)
        holder.scoreIndicator.setText(String.valueOf(recording.getScore())); // Replace `getScore()` with the appropriate getter method for the score

        // Set the delete_icon OnClickListener
        holder.deleteIcon.setOnClickListener(v -> {
            // Remove the recording from the list and the device
            removeRecording(position, ((RecordingActivity) context)::updateRecordingListVisibility);
        });
    }


    @Override
    public int getItemCount() {
        return recordings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recordingTitle;
        TextView recordingDate;
        TextView recordingDuration;
        TextView scoreIndicator;
        ImageView deleteIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recordingTitle = itemView.findViewById(R.id.recording_title);
            recordingDate = itemView.findViewById(R.id.recording_date);
            recordingDuration = itemView.findViewById(R.id.recording_duration);
            scoreIndicator = itemView.findViewById(R.id.score_indicator);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }
    }

    private void removeRecording(int position, Runnable afterRemove) {
        Recording recording = recordings.get(position);

        // Delete the recording from the device
        File file = new File(recording.getFilePath());
        if (file.exists()) {
            if (file.delete()) {
                Log.d("RecordingAdapter", "Recording file deleted: " + recording.getFilePath());
            } else {
                Log.e("RecordingAdapter", "Failed to delete recording file: " + recording.getFilePath());
            }
        }

        // Delete the recording from the database
        new Thread(() -> {
            AppDatabase appDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "recordings-db").build();
            appDatabase.recordingDao().deleteById((int) recording.getId());

            // Update the RecyclerView on the main thread
            ((AppCompatActivity) context).runOnUiThread(() -> {
                recordings.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, recordings.size());

                // Call the afterRemove Runnable
                if (afterRemove != null) {
                    afterRemove.run();
                }
            });
        }).start();
    }
}