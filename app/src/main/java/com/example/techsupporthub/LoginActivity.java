package com.example.techsupporthub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.techsupporthub.admin.AdminActivity;
import com.example.techsupporthub.tech.TechnicianActivity;
import com.example.techsupporthub.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;  // Add ProgressDialog instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.USERS_DB);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);  // Set to false to prevent dismissing by tapping outside

        loginButton.setOnClickListener(view -> loginUser());

        // Navigation links
        findViewById(R.id.registerLink).setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        // Show progress dialog while login is in progress
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressDialog.dismiss();  // Hide progress dialog after completion
            if (task.isSuccessful()) {
                String userId = mAuth.getCurrentUser().getUid();
                fetchUserRole(userId);
            } else {
                Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserRole(String userId) {
        // Show progress dialog while fetching user role
        progressDialog.show();

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();  // Hide progress dialog after data is fetched
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    navigateToRoleSpecificActivity(role);
                } else {
                    Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();  // Hide progress dialog on error
                Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToRoleSpecificActivity(String role) {
        Intent intent;
        if ("admin".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, AdminActivity.class);
        } else if ("technician".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, TechnicianActivity.class);
        } else if ("user".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, UserActivity.class);
        } else {
            Toast.makeText(this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
        finish();
    }
}
