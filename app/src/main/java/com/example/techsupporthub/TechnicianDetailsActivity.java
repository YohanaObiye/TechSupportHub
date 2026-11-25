package com.example.techsupporthub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.techsupporthub.models.UserModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class TechnicianDetailsActivity extends AppCompatActivity {
    private CircleImageView profileImage;
    private TextView technicianName, technicianEmail, technicianAbout;
    private Chip deviceCategory;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_details);

        // Initialize UI elements
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        profileImage = findViewById(R.id.profileImage);
        technicianName = findViewById(R.id.technicianName);
        technicianEmail = findViewById(R.id.technicianEmail);
        technicianAbout = findViewById(R.id.technicianAbout);
        deviceCategory = findViewById(R.id.deviceCategory);


        // Enable back button
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Get userId from Intent
        userId = getIntent().getStringExtra("userId");

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("receiverId", userId);
        findViewById(R.id.chat).setOnClickListener(v -> {
            TechnicianDetailsActivity.this.startActivity(intent);
        });

        // Fetch technician details
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        fetchTechnicianDetails();
    }

    private void fetchTechnicianDetails() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel technician = snapshot.getValue(UserModel.class);
                    if (technician != null) {
                        // Set UI values
                        technicianName.setText(technician.getUsername());
                        technicianEmail.setText("Email: " + technician.getEmail());
                        technicianAbout.setText(technician.getAbout());
                        deviceCategory.setText(technician.getCategory());

                        // Load profile image
                        Glide.with(TechnicianDetailsActivity.this)
                                .load(technician.getProfile())
                                .placeholder(R.drawable.profile)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error (optional: show a Toast)
            }
        });
    }
}