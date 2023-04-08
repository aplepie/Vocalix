package com.vocalix.app.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.vocalix.app.R;
import com.vocalix.app.database.entity.User;
import com.vocalix.app.database.model.UserViewModel;

public class ProfileActivity extends AppCompatActivity {

    private EditText nameField, surnameField, identifierField, emailField;
    private TextView userFullName;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userFullName = findViewById(R.id.user_full_name);
        nameField = findViewById(R.id.name_field);
        surnameField = findViewById(R.id.surname_field);
        identifierField = findViewById(R.id.identifier_field);
        emailField = findViewById(R.id.email_field);

        // Handle save button click event
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> saveUserData());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        ConstraintLayout fieldsContainer = findViewById(R.id.fields_container);
        fieldsContainer.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveUserData();
            }
        });

        // Set the IMEI as the initial identifier value
        setAndroidIdAsIdentifier();

        // Load user data
        loadUserData();
        setupToolbar();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ImageButton backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());
    }

    @SuppressLint("HardwareIds")
    private void setAndroidIdAsIdentifier() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (androidId != null) {
            identifierField.setText(androidId);
        }
    }

    private void loadUserData() {
        userViewModel.getUser().observe(this, user -> {
            String name = "Change";
            String surname = "Me";
            String identifier;

            if (user == null) {
                setAndroidIdAsIdentifier();
                identifier = identifierField.getText().toString();
            } else {
                if (user.getName() != null && !user.getName().isEmpty()) {
                    name = user.getName();
                }
                if (user.getSurname() != null && !user.getSurname().isEmpty()) {
                    surname = user.getSurname();
                }
                if (user.getIdentifier() != null && !user.getIdentifier().isEmpty()) {
                    identifier = user.getIdentifier();
                } else {
                    setAndroidIdAsIdentifier();
                    identifier = identifierField.getText().toString();
                }

                emailField.setText(user.getEmail());
            }

            userFullName.setText(String.format("%s %s", name, surname));
            nameField.setText(name);
            surnameField.setText(surname);
            identifierField.setText(identifier);
        });
    }

    private void saveUserData() {
        String name = nameField.getText().toString();
        String surname = surnameField.getText().toString();
        String identifier = identifierField.getText().toString();
        String email = emailField.getText().toString();

        User user = new User(1, name, surname, identifier, email, null);
        userViewModel.insertOrUpdate(user);

        userFullName.setText(String.format("%s %s", name, surname));

        setResult(Activity.RESULT_OK);
        finish();
    }
}
