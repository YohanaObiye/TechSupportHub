package com.example.techsupporthub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techsupporthub.adapters.UserChatListAdapter;
import com.example.techsupporthub.models.UserModel;
import com.example.techsupporthub.utils.Constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserChatListAdapter userAdapter;
    private ArrayList<UserModel> userList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        findViewById(R.id.ai).setOnClickListener(v -> startActivity(new Intent(ChatListActivity.this, ChatWithAiActivity.class)));


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            fetchUserRole(userId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchUserRole(String userId) {
        mDatabase.child(Constants.USERS_DB).child(userId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String role = dataSnapshot.getValue(String.class);
                if (role != null) {
                    userAdapter = new UserChatListAdapter(userList, ChatListActivity.this, role);
                    recyclerView.setAdapter(userAdapter);
                    fetchUsers(role);
                    Toast.makeText(ChatListActivity.this, "Role: " + role, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatListActivity.this, "Role not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatListActivity", "Failed to fetch user role", databaseError.toException());
                Toast.makeText(ChatListActivity.this, "Failed to fetch user role", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUsers(String role) {
        String targetRole = role.equals("user") ? "technician" : "user";
        mDatabase.child("users").orderByChild("role").equalTo(targetRole.toLowerCase()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatListActivity", "Failed to fetch users", databaseError.toException());
                Toast.makeText(ChatListActivity.this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}