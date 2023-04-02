package com.vocalix.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vocalix.app.R;
import com.vocalix.app.model.Recording;

import java.util.List;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder> {
    private List<Recording> recordings;
    private Context context;

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
        // Set up the views with the recording data
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Declare views for the recording_list_item layout

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
        }
    }
}