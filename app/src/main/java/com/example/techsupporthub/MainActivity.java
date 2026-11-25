package com.example.techsupporthub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.techsupporthub.admin.AdminActivity;
import com.example.techsupporthub.tech.TechnicianActivity;
import com.example.techsupporthub.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.USERS_DB);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Call the method to check user authentication and navigate after a delay
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAuthentication();
            }
        }, 3000); // Delay of 3 seconds
    }

    private void checkAuthentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is authenticated, proceed to fetch user role
            fetchUserRole(currentUser.getUid());
        } else {
            // User is not authenticated, redirect to LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void fetchUserRole(String userId) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    navigateToRoleSpecificActivity(role);
                } else {
                    Toast.makeText(MainActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToRoleSpecificActivity(String role) {
        Intent intent;
        if ("admin".equalsIgnoreCase(role)) {
            intent = new Intent(MainActivity.this, AdminActivity.class);
        } else if ("technician".equalsIgnoreCase(role)) {
            intent = new Intent(MainActivity.this, TechnicianActivity.class);
        } else if ("user".equalsIgnoreCase(role)) {
            intent = new Intent(MainActivity.this, UserActivity.class);
        } else {
            Toast.makeText(this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
        finish();
    }
}
