package com.example.techsupporthub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techsupporthub.adapters.TechnicianCategoryAdapter;
import com.example.techsupporthub.models.UserModel;
import com.example.techsupporthub.utils.Constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TechniciansCategoryActivity extends AppCompatActivity {

    private final ArrayList<UserModel> filteredUsers = new ArrayList<>();
    MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private TechnicianCategoryAdapter adapter;
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technicians_category);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        Intent intent = getIntent();
        selectedCategory = intent.getStringExtra("category");

        if (selectedCategory == null) {
            Toast.makeText(this, "No category passed!", Toast.LENGTH_SHORT).show();
            return;
        }
        toolbar.setTitle(selectedCategory);

        fetchUsersByCategory();
    }

    private void fetchUsersByCategory() {
        FirebaseDatabase.getInstance().getReference(Constants.USERS_DB)
                .orderByChild("category")
                .equalTo(selectedCategory)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        filteredUsers.clear();
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            UserModel user = userSnap.getValue(UserModel.class);
                            if (user != null && "technician".equalsIgnoreCase(user.getRole())) {
                                filteredUsers.add(user);
                            }
                        }

                        adapter = new TechnicianCategoryAdapter(TechniciansCategoryActivity.this, filteredUsers);
                        recyclerView.setAdapter(adapter);

                        if (filteredUsers.isEmpty()) {
                            Toast.makeText(TechniciansCategoryActivity.this, "No technicians found for " + selectedCategory, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TechniciansCategoryActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
