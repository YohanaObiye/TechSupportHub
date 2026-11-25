package com.example.techsupporthub;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techsupporthub.adapters.CategoryAdapter;
import com.example.techsupporthub.adapters.TechnicianAdapterUser;
import com.example.techsupporthub.models.UserModel;
import com.example.techsupporthub.tech.HelpActivity;
import com.example.techsupporthub.utils.Constants;
import com.example.techsupporthub.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class UserActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private RecyclerView techniciansRV;
    private TechnicianAdapterUser techniciansAdapter;
    private ArrayList<UserModel> techniciansList;
    private RecyclerView categoriesRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        techniciansRV = findViewById(R.id.techniciansRV);

        // Initialize RecyclerView
        categoriesRV = findViewById(R.id.categoriesRV);
        ArrayList<String> categoriesList = new ArrayList<>(Arrays.asList(Constants.DEVICE_CATEGORIES));

        CategoryAdapter adapter = new CategoryAdapter(this, categoriesList, Constants.DEVICE_CATEGORY_ICONS);
        categoriesRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoriesRV.setAdapter(adapter);

        techniciansList = new ArrayList<>();
        techniciansAdapter = new TechnicianAdapterUser(this, techniciansList);
        techniciansRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        techniciansRV.setAdapter(techniciansAdapter);

        findViewById(R.id.help).setOnClickListener(v -> startActivity(new Intent(UserActivity.this, HelpActivity.class)));
        findViewById(R.id.payment).setOnClickListener(v -> startActivity(new Intent(UserActivity.this, PaymentActivity.class)));

        // Fetch Technicians
        fetchTechnicians();

        // Find the MaterialToolbar and set it as the action bar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.chatAI).setOnClickListener(v -> {
            startActivity(new Intent(UserActivity.this, ChatWithAiActivity.class));
        });
        findViewById(R.id.workshops).setOnClickListener(v -> {
            startActivity(new Intent(UserActivity.this, WorkshopActivity.class));
        });
        findViewById(R.id.my_profile).setOnClickListener(v -> {
            startActivity(new Intent(UserActivity.this, ProfileActivity.class));
        });
        findViewById(R.id.chat).setOnClickListener(v -> {
            startActivity(new Intent(UserActivity.this, ChatListActivity.class));
        });
    }

    private void fetchTechnicians() {
        Utils.getDatabaseReference(Constants.USERS_DB).orderByChild("role").equalTo("technician")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        techniciansList.clear();
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            UserModel technician = userSnapshot.getValue(UserModel.class);
                            if (technician != null) {
                                techniciansList.add(technician);
                            }
                        }
                        techniciansAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
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
        Intent intent = new Intent(this, LoginActivity.class);
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
                                Toast.makeText(UserActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(UserActivity.this, LoginActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(UserActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // If no user is logged in, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
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