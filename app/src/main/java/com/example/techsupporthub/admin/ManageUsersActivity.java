package com.example.techsupporthub.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techsupporthub.R;
import com.example.techsupporthub.adapters.UserAdapter;
import com.example.techsupporthub.models.UserModel;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<UserModel> userList, filteredList;
    private DatabaseReference usersRef;
    private ChipGroup chipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        recyclerView = findViewById(R.id.recycler_view);
        chipGroup = findViewById(R.id.chip_group);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new UserAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        fetchUsers();

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            Toast.makeText(this, "Huraah: ghgg", Toast.LENGTH_SHORT).show();
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0); // Get the first selected chip
                Toast.makeText(this, "Huraah: " + checkedId, Toast.LENGTH_SHORT).show();

                filterUsers(checkedId);
            }
        });
    }

    private void fetchUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    UserModel user = data.getValue(UserModel.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                filteredList.clear();
                filteredList.addAll(userList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageUsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(int checkedId) {
        filteredList.clear();

        String role = "";
        if (checkedId == R.id.chip_admins) {
            role = "Admin";
        } else if (checkedId == R.id.chip_technicians) {
            role = "Technician";
        } else if (checkedId == R.id.chip_users) {
            role = "User";
        }

        if (role.isEmpty()) {
            filteredList.addAll(userList); // Show all users if "ALL" is selected
        } else {
            for (UserModel user : userList) {
                if (user.getRole().equalsIgnoreCase(role)) {
                    filteredList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}