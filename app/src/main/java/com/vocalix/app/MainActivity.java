package com.vocalix.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vocalix.app.activity.DetailsActivity;
import com.vocalix.app.activity.ProfileActivity;
import com.vocalix.app.database.adapter.ExerciseAdapter;
import com.vocalix.app.database.model.ExerciseViewModel;
import com.vocalix.app.database.model.UserViewModel;
import com.vocalix.app.ui.decorations.SpaceItemDecoration;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ExerciseAdapter.OnExerciseClickListener {

    private ActivityResultLauncher<Intent> profileActivityResultLauncher;
    private ExerciseAdapter exerciseAdapter;
    private ExerciseViewModel exerciseViewModel;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the ActivityResultLauncher
        profileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Call setUpViewModels() to refresh the UI with the updated user data
                        setUpViewModels();
                    }
                });

        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);

        setUpDataPreferences();
        setupToolbar();
        setupRecyclerView();
        setupSearchEditText();
        setUpViewModels();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        exerciseAdapter = new ExerciseAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(exerciseAdapter);

        int spaceBetweenItems = getResources().getDimensionPixelSize(R.dimen.space_between_items);
        SpaceItemDecoration itemDecoration = new SpaceItemDecoration(spaceBetweenItems);
        recyclerView.addItemDecoration(itemDecoration);
    }

    private void setupSearchEditText() {
        EditText searchEditText = findViewById(R.id.search_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                exerciseAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setUpDataPreferences() {
        exerciseViewModel.updateDataInDatabase();

        exerciseViewModel.getVocalExercises().observe(this, exercises -> {
            // Update your RecyclerView adapter with the new data
            exerciseAdapter.updateData(exercises);
            // Also update the filteredExercises list with the new data
            exerciseAdapter.updateFilteredExercises(exercises);
        });
    }

    private void setUpViewModels() {
        TextView user_name_text = findViewById(R.id.user_name_text);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getUser().observe(this, user -> {
            if (user != null) {
                String name = user.getName();
                String surname = user.getSurname();

                user_name_text.setText(String.format("%s %s", name, surname));
            }
        });
    }

    public void goToProfilePage(View view) {
        Animation buttonClickAnimation = AnimationUtils.loadAnimation(this, R.anim.profile_button_animation);
        view.startAnimation(buttonClickAnimation);

        profileActivityResultLauncher.launch(new Intent(MainActivity.this, ProfileActivity.class));
    }

    @Override
    public void onExerciseClick(String exerciseName) {
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra("exerciseName", exerciseName);
        startActivity(intent);
    }
}
