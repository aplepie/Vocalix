package com.vocalix.app.database.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vocalix.app.R;
import com.vocalix.app.activity.DetailsActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> implements Filterable {

    private List<Map<String, Object>> exercises;
    private final List<Map<String, Object>> filteredExercises;
    private final Context context;
    private final OnExerciseClickListener onExerciseClickListener;

    public ExerciseAdapter(Context context, List<Map<String, Object>> exercises, OnExerciseClickListener onExerciseClickListener) {
        this.context = context;
        this.exercises = exercises;
        this.filteredExercises = new ArrayList<>(exercises);
        this.onExerciseClickListener = onExerciseClickListener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new ExerciseViewHolder(view, context, exercises);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Map<String, Object> exercise = filteredExercises.get(position);

        holder.exerciseName.setText((String) exercise.get("name"));
        holder.exerciseType.setText((String) exercise.get("type"));
        holder.exerciseDuration.setText((String) exercise.get("duration"));
        holder.exerciseDifficulty.setText((String) exercise.get("difficulty"));
        // TODO Set instructions in the details activity
        //holder.exerciseInstructions.setText((String) exercise.get("instructions"));

        String imageName = (String) exercise.get("image");
        @SuppressLint("DiscouragedApi") int imageResourceId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());

        Glide.with(context).load(imageResourceId).into(holder.exerciseImage);

        holder.exerciseName.setText((String) exercise.get("name"));
        holder.itemView.setOnClickListener(v -> onExerciseClickListener.onExerciseClick((String) exercise.get("name")));
    }

    @Override
    public int getItemCount() {
        return filteredExercises.size();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        ImageView exerciseImage;
        TextView exerciseName;
        TextView exerciseType;
        TextView exerciseDuration;
        TextView exerciseDifficulty;
        Context context;
        List<Map<String, Object>> exercises;

        public ExerciseViewHolder(@NonNull View itemView, Context context, List<Map<String, Object>> exercises) {
            super(itemView);
            this.context = context;
            this.exercises = exercises;

            exerciseImage = itemView.findViewById(R.id.exercise_image);
            exerciseName = itemView.findViewById(R.id.exercise_name);
            exerciseType = itemView.findViewById(R.id.exercise_type);
            exerciseDuration = itemView.findViewById(R.id.exercise_duration);
            exerciseDifficulty = itemView.findViewById(R.id.exercise_difficulty);
            // TODO Set instructions in the details activity
            // exerciseInstructions = itemView.findViewById(R.id.exercise_instructions);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Map<String, Object> exercise = exercises.get(position);
                    // Handle the item click event here, and navigate to the appropriate activity
                    Intent intent = new Intent(context, DetailsActivity.class);
                    intent.putExtra("exercise", (Serializable) exercise);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Map<String, Object>> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(exercises);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Map<String, Object> exercise : exercises) {
                        String name = Objects.requireNonNull(exercise.get("name")).toString().toLowerCase();
                        String type = Objects.requireNonNull(exercise.get("type")).toString().toLowerCase();

                        if (name.contains(filterPattern) || type.contains(filterPattern)) {
                            filteredList.add(exercise);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @SuppressLint("NotifyDataSetChanged")
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredExercises.clear();
                filteredExercises.addAll((List<Map<String, Object>>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateFilteredExercises(List<Map<String, Object>> newExercises) {
        this.filteredExercises.clear();
        this.filteredExercises.addAll(newExercises);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Map<String, Object>> newExercises) {
        this.exercises = newExercises;
        notifyDataSetChanged();
    }

    public interface OnExerciseClickListener {
        void onExerciseClick(String exerciseName);
    }
}

