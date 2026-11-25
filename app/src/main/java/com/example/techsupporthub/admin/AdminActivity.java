package com.example.techsupporthub.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.techsupporthub.LoginActivity;
import com.example.techsupporthub.R;
import com.example.techsupporthub.models.UserModel;
import com.example.techsupporthub.utils.Constants;
import com.example.techsupporthub.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button logoutButton;
    private MaterialCardView manageUsers, manageTechnicians, manageWorkshops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize the variables
        manageUsers = findViewById(R.id.manage_users);
        manageTechnicians = findViewById(R.id.manage_technicians);
        manageWorkshops = findViewById(R.id.manage_workshops);

        // Find the MaterialToolbar and set it as the action bar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        manageUsers.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, ManageUsersActivity.class));
        });
        manageTechnicians.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, ManageTechniciansActivity.class));
        });
        manageWorkshops.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, ManageWorkshopActivity.class));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.admin_main_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserDetails();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            // Handle the logout action
            showLogoutConfirmationDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setIcon(R.drawable.logo) // Set the app icon
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logoutUser();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void logoutUser() {
        // Logout the user from Firebase
        mAuth.signOut();
        // Redirect to login activity
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();  // Close the current activity
    }

    private void showNotifications() {
        Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
        // Implement the logic to show notifications
        // For example, you can start a new activity or open a fragment
    }


    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Confirmation")
                .setMessage("Are you sure you want to exit the app?")
                .setIcon(R.drawable.logo) // Set the app icon
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(); // Close the current activity
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void fetchUserDetails() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Get logged-in user ID

            Utils.getDatabaseReference(Constants.USERS_DB).child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                UserModel user = snapshot.getValue(UserModel.class); // Convert snapshot to UserModel
                                if (user != null) {
                                    updateUserName(user.getUsername());
                                }
                            } else {
                                Toast.makeText(AdminActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(AdminActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // If no user is logged in, redirect to login
            startActivity(new Intent(AdminActivity.this, LoginActivity.class));
            finish();
        }
    }

    // Method to update UI with username
    private void updateUserName(String username) {
        runOnUiThread(() -> {
            TextView userNameTextView = findViewById(R.id.user_name);
            if (userNameTextView != null && username != null) {
                userNameTextView.setText(username);
            }
        });
    }

}