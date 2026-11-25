package com.example.techsupporthub;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techsupporthub.adapters.PaymentAdapter;
import com.example.techsupporthub.models.Payment;
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

public class PaymentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PaymentAdapter paymentAdapter;
    private ArrayList<Payment> paymentList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        paymentList = new ArrayList<>();
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());


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
                currentUserRole = dataSnapshot.getValue(String.class);
                if (currentUserRole != null) {
                    fetchPayments();
                    paymentAdapter = new PaymentAdapter(paymentList, PaymentActivity.this, currentUserRole);
                    recyclerView.setAdapter(paymentAdapter);
                } else {
                    Toast.makeText(PaymentActivity.this, "Role not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PaymentActivity", "Failed to fetch user role", databaseError.toException());
                Toast.makeText(PaymentActivity.this, "Failed to fetch user role", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPayments() {
        String path = Constants.PAYMENTS_DB;
        mDatabase.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                paymentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Payment payment = snapshot.getValue(Payment.class);
                    if (payment != null) {
                        if ("USER".equalsIgnoreCase(currentUserRole) && payment.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                            paymentList.add(payment);
                        } else if ("TECHNICIAN".equalsIgnoreCase(currentUserRole) && payment.getTechnicianId().equals(FirebaseAuth.getInstance().getUid())) {
                            paymentList.add(payment);
                        }
                    }
                }
                paymentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PaymentActivity", "Failed to fetch payments", databaseError.toException());
                Toast.makeText(PaymentActivity.this, "Failed to fetch payments", Toast.LENGTH_SHORT).show();
            }
        });
    }
}