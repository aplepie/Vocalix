package com.vocalix.app.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.vocalix.app.R;
import com.vocalix.app.database.entity.User;
import com.vocalix.app.database.model.UserViewModel;

import java.io.IOException;
import java.io.OutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1001;

    private CircleImageView profileImage;
    private EditText nameField, surnameField, identifierField, emailField;
    private TextView userFullName;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profile_image);
        ImageView changeProfileImageIcon = findViewById(R.id.change_profile_image_icon);
        userFullName = findViewById(R.id.user_full_name);
        nameField = findViewById(R.id.name_field);
        surnameField = findViewById(R.id.surname_field);
        identifierField = findViewById(R.id.identifier_field);
        emailField = findViewById(R.id.email_field);

        // Handle save button click event
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> saveUserData());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        changeProfileImageIcon.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSION_CODE);
            } else {
                pickImageFromGallery();
            }
        });

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

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> pickImageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    assert result.getData() != null;
                    Uri imageUri = result.getData().getData();
                    profileImage.setImageURI(imageUri);
                    // Save image Uri to the database
                    userViewModel.insertOrUpdate(new User(1, nameField.getText().toString(), surnameField.getText().toString(), identifierField.getText().toString(), emailField.getText().toString(), imageUri.toString()));
                }
            });

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("HardwareIds")
    private void setAndroidIdAsIdentifier() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (androidId != null) {
            identifierField.setText(androidId);
        }
    }

    @SuppressWarnings("deprecation")
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

                if (user.getImageUri() != null && !user.getImageUri().isEmpty()) {
                    try {
                        Uri imageUri = Uri.parse(user.getImageUri());

                        // Deprecation warning suppressed because the alternative method (ImageDecoder) is only available in API 28+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                            Bitmap bitmap = ImageDecoder.decodeBitmap(source);
                            profileImage.setImageBitmap(bitmap);
                        } else {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            profileImage.setImageBitmap(bitmap);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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

        String imageUriString = null;

        Drawable drawable = profileImage.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Uri imageUri = saveImageToGallery(bitmap);
            imageUriString = imageUri.toString();
        } else {
            Toast.makeText(this, "Error: Unable to save image URI", Toast.LENGTH_SHORT).show();
        }

        User user = new User(1, name, surname, identifier, email, imageUriString);
        userViewModel.insertOrUpdate(user);

        userFullName.setText(String.format("%s %s", name, surname));

        setResult(Activity.RESULT_OK);
        finish();
    }

    private Uri saveImageToGallery(Bitmap bitmap) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "ProfilePicture_" + System.currentTimeMillis());
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (imageUri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(imageUri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return imageUri;
    }
}
