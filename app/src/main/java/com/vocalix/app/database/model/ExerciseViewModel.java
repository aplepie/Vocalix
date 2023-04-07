package com.vocalix.app.database.model;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vocalix.app.database.AppDatabase;
import com.vocalix.app.database.dao.ExerciseDao;
import com.vocalix.app.database.entity.Exercise;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExerciseViewModel extends AndroidViewModel {

    private final ExerciseDao exerciseDao;
    private final MutableLiveData<List<Map<String, Object>>> vocalExercises;
    private final Executor executor;

    public ExerciseViewModel(Application application) {
        super(application);

        AppDatabase db = AppDatabase.getInstance(application);
        exerciseDao = db.vocalExerciseDao();
        vocalExercises = new MutableLiveData<>();
        executor = Executors.newSingleThreadExecutor();

        loadVocalExercises();
    }

    public LiveData<List<Map<String, Object>>> getVocalExercises() {
        return vocalExercises;
    }

    private void loadVocalExercises() {
        executor.execute(() -> {
            List<Map<String, Object>> exercises = loadExercisesFromDatabase();
            vocalExercises.postValue(exercises);
        });
    }

    private List<Map<String, Object>> loadExercisesFromDatabase() {
        List<Exercise> exerciseList = exerciseDao.getAll();
        ArrayList<Map<String, Object>> exercises = new ArrayList<>();

        for (Exercise exercise : exerciseList) {
            Map<String, Object> exerciseMap = new HashMap<>();

            exerciseMap.put("name", exercise.getName());
            exerciseMap.put("type", exercise.getType());
            exerciseMap.put("instructions", exercise.getInstructions());
            exerciseMap.put("description", exercise.getDescription());
            exerciseMap.put("image", exercise.getImage());
            exerciseMap.put("duration", exercise.getDuration());
            exerciseMap.put("difficulty", exercise.getDifficulty());

            exercises.add(exerciseMap);
        }
        return exercises;
    }

    public void updateDataInDatabase() {
        executor.execute(this::insertData);
    }

    private void insertData() {
        List<Exercise> exercises = loadVocalExercisesFromJSON();

        List<Exercise> existingExercises = exerciseDao.getAll();
        List<Exercise> newExercises = new ArrayList<>();
        List<Exercise> updatedExercises = new ArrayList<>();

        for (Exercise exercise : exercises) {
            boolean exists = false;

            for (Exercise existingExercise : existingExercises) {
                if (exercise.getName().equals(existingExercise.getName())) {
                    exists = true;
                    exercise.setId(existingExercise.getId());
                    updatedExercises.add(exercise);

                    break;
                }
            }
            if (!exists) {
                newExercises.add(exercise);
            }
        }

        exerciseDao.insertAll(newExercises.toArray(new Exercise[0]));
        exerciseDao.updateAll(updatedExercises.toArray(new Exercise[0]));

        // Load the updated exercises from the database and update the LiveData
        loadVocalExercises();
    }


    public List<Exercise> loadVocalExercisesFromJSON() {
        List<Exercise> exercises = new ArrayList<>();
        String jsonString = null;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getApplication().getAssets().open("vocal_exercises.json")))) {

            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            jsonString = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jsonString != null) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                Log.d("ExerciseViewModel", "JSON Array Length: " + jsonArray.length());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Exercise exercise = new Exercise();
                    exercise.setName(jsonObject.getString("name"));
                    exercise.setType(jsonObject.getString("type"));
                    exercise.setDescription(jsonObject.getString("description"));
                    exercise.setImage(jsonObject.getString("image"));
                    exercise.setDuration(jsonObject.getString("duration"));
                    exercise.setDifficulty(jsonObject.getString("difficulty"));

                    // Parse instructions as a list of strings
                    JSONArray instructionsArray = jsonObject.getJSONArray("instructions");
                    List<String> instructionsList = new ArrayList<>();
                    for (int j = 0; j < instructionsArray.length(); j++) {
                        instructionsList.add(instructionsArray.getString(j));
                    }
                    exercise.setInstructions(instructionsList);

                    exercises.add(exercise);
                    Log.d("ExerciseViewModel", "Exercise added: " + exercise.getName());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return exercises;
    }
}
