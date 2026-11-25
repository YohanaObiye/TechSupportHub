package com.example.techsupporthub.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techsupporthub.R;
import com.example.techsupporthub.models.Payment;
import com.example.techsupporthub.models.UserModel;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {
    private static final String API_URL = "https://api-mpesa.kuwesakenya.com/pay";
    private static final String APP_NAME = "TechSupportHub - by Shyra"; // Your app name

    private final ArrayList<Payment> paymentList;
    private final Context context;
    private final String currentUserRole;
    private final DatabaseReference mDatabase;

    public PaymentAdapter(ArrayList<Payment> paymentList, Context context, String currentUserRole) {
        this.paymentList = paymentList;
        this.context = context;
        this.currentUserRole = currentUserRole;
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentList.get(position);

        // Display amount and timestamp
        holder.textViewAmount.setText("Kshs. " + payment.getAmount());
        holder.textViewTimestamp.setText(getFormattedTime(payment.getTimestamp()));
        if (payment.isPaid()) holder.status.setText("Paid");
        else holder.status.setText("Not Paid");

        // Fetch and set technician name
        fetchUserName(payment.getTechnicianId(), holder.textViewTechnician);

        // Fetch and set user name
        fetchUserName(payment.getUserId(), holder.textViewUser);

        // Handle paid status
        if (payment.isPaid()) {
            holder.buttonMarkPaid.setVisibility(View.GONE);
        } else {
            holder.buttonMarkPaid.setVisibility(View.VISIBLE);
        }

        // Show payment dialog for users only
        if ("USER".equalsIgnoreCase(currentUserRole)) {
            holder.buttonMarkPaid.setOnClickListener(v -> showPaymentDialog(payment));
        } else {
            holder.buttonMarkPaid.setVisibility(View.GONE);
        }
    }

    private void fetchUserName(String userId, TextView textView) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserModel user = dataSnapshot.getValue(UserModel.class);
                    if (user != null) {
                        textView.setText(user.getUsername()); // Set the username
                    } else {
                        textView.setText("Unknown User");
                    }
                } else {
                    textView.setText("Unknown User");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PaymentAdapter", "Failed to fetch user name", databaseError.toException());
                textView.setText("Error");
            }
        });
    }

    // --- Payment Dialog ---
    private void showPaymentDialog(final Payment payment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Phone Number");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setHint("07********");
        int padding = (int) (16 * context.getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        builder.setView(input);
        builder.setPositiveButton("Pay", (dialog, which) -> {
            String phoneNumber = input.getText().toString().trim();
            if (isValidPhoneNumber(phoneNumber)) {
                String formattedNumber = formatPhoneNumber(phoneNumber);
                makeMpesaPayment(payment, formattedNumber);
            } else {
                Toast.makeText(context, "Invalid phone number! Use format 07********", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // --- M-Pesa Payment Logic ---
    private void makeMpesaPayment(final Payment payment, final String formattedNumber) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Processing payment...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject payload = new JSONObject();
                payload.put("phoneNumber", formattedNumber);
                payload.put("amount", Integer.parseInt(payment.getAmount())); // Convert String to int
                payload.put("accountReference", APP_NAME);
                payload.put("transactionDesc", "Payment to " + payment.getTechnicianId()); // Customize description

                RequestBody body = RequestBody.create(payload.toString(), MediaType.get("application/json"));
                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String responseData = Objects.requireNonNull(response.body()).string();
                    Log.d("PaymentResponse", responseData);

                    // Update payment status in Firebase
                    updatePaymentStatus(payment, formattedNumber);
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Payment Successful: " + responseData, Toast.LENGTH_SHORT).show();

                    });
                } else {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Payment Failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Payment Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // --- Update Payment Status ---
    private void updatePaymentStatus(Payment payment, String formattedNumber) {
        payment.setPaid(true);
        payment.setPhone(formattedNumber);

        mDatabase.child("payments")
                .child(payment.getId())
                .setValue(payment) // Save the updated Payment model directly
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Payment marked as paid", Toast.LENGTH_SHORT).show();
//                    notifyItemChanged(paymentList.indexOf(payment));
                })
                .addOnFailureListener(e -> {
                    Log.e("PaymentAdapter", "Failed to update payment status", e);
                    Toast.makeText(context, "Failed to update payment status", Toast.LENGTH_SHORT).show();
                });
    }

    // --- Helper Methods ---
    private boolean isValidPhoneNumber(String phoneNumber) {
        return Pattern.matches("^07\\d{8}$", phoneNumber);
    }

    private String formatPhoneNumber(String phoneNumber) {
        return "254" + phoneNumber.substring(1); // Convert "0712345678" to "254712345678"
    }

    private String getFormattedTime(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    private void runOnUiThread(Runnable action) {
        ((Activity) context).runOnUiThread(action);
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTechnician;
        TextView textViewUser;
        TextView textViewAmount;
        TextView textViewTimestamp;
        Button buttonMarkPaid;
        Chip status;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTechnician = itemView.findViewById(R.id.textViewTechnician);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            buttonMarkPaid = itemView.findViewById(R.id.buttonMarkPaid);
            status = itemView.findViewById(R.id.status);
        }
    }
}