package com.example.techsupporthub;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.techsupporthub.models.UserModel;
import com.example.techsupporthub.utils.Constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imageProfile;
    private Chip txtUsername, txtEmail, txtCategory;
    private MaterialTextView txtAbout;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageProfile = findViewById(R.id.imageProfile);
        txtUsername = findViewById(R.id.txtUsername);
        txtEmail = findViewById(R.id.txtEmail);
        txtCategory = findViewById(R.id.txtCategory);
        txtAbout = findViewById(R.id.txtAbout);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseDatabase.getInstance().getReference(Constants.USERS_DB)
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel user = snapshot.getValue(UserModel.class);
                        if (user != null) {
                            txtUsername.setText(user.getUsername());
                            txtEmail.setText(user.getEmail());
                            txtCategory.setText(user.getCategory());
                            txtAbout.setText(user.getAbout());

                            if (user.getProfile() != null && !user.getProfile().isEmpty()) {
                                Glide.with(ProfileActivity.this)
                                        .load(user.getProfile())
                                        .into(imageProfile);
                            } else {
                                imageProfile.setImageResource(R.drawable.profile);
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
