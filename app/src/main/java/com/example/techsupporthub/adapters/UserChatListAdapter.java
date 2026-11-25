package com.example.techsupporthub.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.techsupporthub.ChatActivity;
import com.example.techsupporthub.R;
import com.example.techsupporthub.models.Payment;
import com.example.techsupporthub.models.UserModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserChatListAdapter extends RecyclerView.Adapter<UserChatListAdapter.UserViewHolder> {

    private final ArrayList<UserModel> userList;
    private final Context context;
    private final String currentUserRole;
    private final String currentUserID;
    private final DatabaseReference mDatabase;

    public UserChatListAdapter(ArrayList<UserModel> userList, Context context, String currentUserRole) {
        this.userList = userList;
        this.context = context;
        this.currentUserRole = currentUserRole;
        this.currentUserID = FirebaseAuth.getInstance().getUid();
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_chat, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.textViewName.setText(user.getUsername());

        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("receiverId", user.getId());
        holder.card.setOnClickListener(v -> {
            context.startActivity(intent);
        });

        // Set the role in the Chip
        if (user.getRole().equals("technician"))
            holder.textViewRole.setText(user.getCategory());
        else holder.textViewRole.setText(user.getRole());

        // Load the profile image using Glide
        if (user.getProfile() != null && !user.getProfile().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfile())
                    .placeholder(R.drawable.profile)
                    .into(holder.imageViewProfile);
        } else {
            holder.imageViewProfile.setImageResource(R.drawable.profile);
        }

        // Add long-click listener to the card
        holder.card.setOnLongClickListener(v -> {
            if ("TECHNICIAN".equalsIgnoreCase(currentUserRole)) {
                checkUnpaidPayments(user.getId(), () -> showPopupMenu(holder.card, user.getId()));
            }
            return true;
        });
    }

    private void checkUnpaidPayments(String userId, Runnable onSuccess) {
        mDatabase.child("payments")
                .orderByChild("technicianId")
                .equalTo(currentUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean hasUnpaidPayment = false;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Payment payment = snapshot.getValue(Payment.class);
                            if (payment != null && payment.getUserId().equals(userId) && !payment.isPaid()) {
                                hasUnpaidPayment = true;
                                break;
                            }
                        }

                        if (hasUnpaidPayment) {
                            Toast.makeText(context, "You have an unpaid payment for this user.", Toast.LENGTH_SHORT).show();
                        } else {
                            onSuccess.run();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("UserChatListAdapter", "Failed to check unpaid payments", databaseError.toException());
                        Toast.makeText(context, "Failed to check unpaid payments", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPopupMenu(View view, String userId) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_set_price) {
                showSetPriceDialog(userId);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showSetPriceDialog(String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set Price");

        // Set the logo
        builder.setIcon(R.drawable.logo); // Ensure you have a logo drawable

        final EditText input = new EditText(context);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String price = input.getText().toString().trim();
            if (!price.isEmpty()) {
                savePayment(userId, price);
            } else {
                Toast.makeText(context, "Please enter a valid price", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void savePayment(String userId, String amount) {
        String paymentId = mDatabase.child("payments").push().getKey();
        if (paymentId == null) {
            Toast.makeText(context, "Failed to generate payment ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Payment payment = new Payment(
                paymentId,
                currentUserID,
                userId,
                amount,
                "",
                false,
                System.currentTimeMillis()
        );

        mDatabase.child("payments").child(paymentId).setValue(payment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Payment set to: " + amount, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("UserChatListAdapter", "Failed to save payment", e);
                    Toast.makeText(context, "Failed to save payment", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProfile;
        TextView textViewName;
        Chip textViewRole;
        MaterialCardView card;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewRole = itemView.findViewById(R.id.textViewRole);
            card = itemView.findViewById(R.id.card);
        }
    }
}