package com.example.techsupporthub;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techsupporthub.adapters.WorkshopAdapter;
import com.example.techsupporthub.model.Workshop;
import com.example.techsupporthub.utils.Constants;
import com.example.techsupporthub.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WorkshopActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WorkshopAdapter workshopAdapter;
    private List<Workshop> workshopList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        workshopList = new ArrayList<>();
        workshopAdapter = new WorkshopAdapter(this, workshopList);
        recyclerView.setAdapter(workshopAdapter);


        // Initialize Firebase Realtime Database reference
        databaseReference = Utils.getDatabaseReference(Constants.WORKSHOP_DB);

        // Retrieve workshops from Firebase Realtime Database
        retrieveWorkshops();
    }

    private void retrieveWorkshops() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                workshopList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Workshop workshop = snapshot.getValue(Workshop.class);
                    if (workshop != null) {
                        workshopList.add(workshop);
                    }
                }
                workshopAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("WorkshopActivity", "Failed to read value.", databaseError.toException());
                Toast.makeText(WorkshopActivity.this, "Failed to retrieve workshops", Toast.LENGTH_SHORT).show();
            }
        });
    }
}