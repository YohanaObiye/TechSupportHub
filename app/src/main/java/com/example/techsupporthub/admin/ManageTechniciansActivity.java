package com.example.techsupporthub.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techsupporthub.R;
import com.example.techsupporthub.adapters.UserAdapter;
import com.example.techsupporthub.models.UserModel;
import com.example.techsupporthub.utils.Constants;
import com.example.techsupporthub.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageTechniciansActivity extends AppCompatActivity {
    private final ArrayList<UserModel> technicianList = new ArrayList<>();
    private RecyclerView recyclerView;
    private UserAdapter technicianAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_technicians);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        technicianAdapter = new UserAdapter(this, technicianList);
        recyclerView.setAdapter(technicianAdapter);

        FloatingActionButton fabCreate = findViewById(R.id.create);
        fabCreate.setOnClickListener(v -> startActivity(new Intent(this, CreateTechniciansActivity.class)));

        fetchTechnicians();
    }

    private void fetchTechnicians() {
        Utils.getDatabaseReference(Constants.USERS_DB)
                .orderByChild("role")
                .equalTo("technician") // Filter users by role
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        technicianList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            UserModel technician = ds.getValue(UserModel.class);
                            if (technician != null) {
                                technicianList.add(technician);
                            }
                        }
                        technicianAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ManageTechniciansActivity.this, "Failed to load technicians", Toast.LENGTH_SHORT).show();
                        Log.e("FirebaseError", error.getMessage());
                    }
                });
    }
}
