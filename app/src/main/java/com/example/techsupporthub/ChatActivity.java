package com.example.techsupporthub;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techsupporthub.adapters.ChatAdapter;
import com.example.techsupporthub.models.ChatModel;
import com.example.techsupporthub.utils.Constants;
import com.example.techsupporthub.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ChatActivity extends AppCompatActivity {
    TextInputEditText edMessage;
    ArrayList<ChatModel> chatList = new ArrayList<>();
    RecyclerView recyclerView;
    ChatAdapter chatAdapter;
    String senderId, receiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        edMessage = findViewById(R.id.message);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(ChatActivity.this, chatList);
        recyclerView.setAdapter(chatAdapter);

        // Get sender ID from SharedPrefManager
        senderId = FirebaseAuth.getInstance().getUid();

        // Get receiver ID from intent
        receiverId = getIntent().getStringExtra("receiverId");
        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "Receiver ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        findViewById(R.id.send).setOnClickListener(v -> {
            String message = edMessage.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(ChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
            } else {
                sendMessageToDoctor(message);
                edMessage.setText("");
            }
        });

        loadChats();
    }

    private void loadChats() {
        String roomId1 = senderId + receiverId;
        String roomId2 = receiverId + senderId;
        Utils.getDatabaseReference(Constants.CHATS_DB).orderByChild("currentDate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String roomId = snapshot.getKey();
//                    Toast.makeText(ChatActivity.this, "roomId: "+roomId, Toast.LENGTH_SHORT).show();
                    if (roomId != null && (roomId.equals(roomId1) || roomId.equals(roomId2))) {
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            String chatId = messageSnapshot.child("chatId").getValue(String.class);
                            String senderId = messageSnapshot.child("senderId").getValue(String.class);
                            String receiverId = messageSnapshot.child("receiverId").getValue(String.class);
                            String message = messageSnapshot.child("message").getValue(String.class);
                            String currentDate = messageSnapshot.child("currentDate").getValue(String.class);

//                            Toast.makeText(ChatActivity.this, "chatId: " + chatId, Toast.LENGTH_SHORT).show();
                            ChatModel chat = new ChatModel(chatId, roomId, senderId, receiverId, message, currentDate);
                            chatList.add(chat);
                        }

                        // Sort the chatList based on the currentDate
                        Collections.sort(chatList, (chat1, chat2) -> chat1.getCurrentDate().compareTo(chat2.getCurrentDate()));

                        recyclerView.post(() -> {
                            // Scroll to the bottom of the RecyclerView
                            recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                        });
                        chatAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendMessageToDoctor(String message) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Sending...");
        progressDialog.show();

        String currentDate = Constants.getDate();
        String chatId = Utils.getDatabaseReference(Constants.CHATS_DB).push().getKey();
        String roomId = senderId + receiverId;

        ChatModel chat = new ChatModel(chatId, roomId, senderId, receiverId, message, currentDate);
        Utils.getDatabaseReference(Constants.CHATS_DB).child(roomId).child(chatId).setValue(chat)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(ChatActivity.this, "Failed to Send", Toast.LENGTH_SHORT).show();
                });
    }

}