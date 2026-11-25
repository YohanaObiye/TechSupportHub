package com.example.techsupporthub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.techsupporthub.models.UserModel;
import com.example.techsupporthub.utils.Constants;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText emailInput, usernameInput, passwordInput, confirmPasswordInput;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.USERS_DB);

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering user...");
        progressDialog.setCancelable(false);

        findViewById(R.id.registerButton).setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = Objects.requireNonNull(emailInput.getText()).toString().trim();
        String username = Objects.requireNonNull(usernameInput.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordInput.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(confirmPasswordInput.getText()).toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        // Show progress dialog
        progressDialog.show();

        // Create user in Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss(); // Dismiss the progress dialog after task completion
                    if (task.isSuccessful()) {
                        String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                        long createdAt = System.currentTimeMillis();

                        // Create UserModel and save to Firebase Database
                        UserModel userModel = new UserModel(userId, username, email, "user", createdAt, "", "", "");
                        databaseReference.child(userId).setValue(userModel)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                                        // Navigate to UserActivity after successful registration
                                        Intent intent = new Intent(RegisterActivity.this, UserActivity.class);
                                        startActivity(intent);
                                        finish(); // Close RegisterActivity
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Failed to save user data: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
