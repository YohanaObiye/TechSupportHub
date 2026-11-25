package com.example.techsupporthub;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techsupporthub.adapters.MessageAdapter;
import com.example.techsupporthub.models.Message;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatWithAiActivity extends AppCompatActivity {

    private static final String TAG = "ChatWithAiActivity";
    private final String URL = "http://gemini-api.kuwesakenya.com/generate";
    private List<Message> messages;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private TextInputEditText inputEditText;
    private FloatingActionButton sendButton;
    private boolean isAITyping;
    private Handler handler;
    private MaterialToolbar toolbar;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_ai);

        // Initialize UI elements
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        inputEditText = findViewById(R.id.inputEditText);
        sendButton = findViewById(R.id.fab);
        messages = new ArrayList<>();
        isAITyping = false;
        handler = new Handler(Looper.getMainLooper());
        client = new OkHttpClient();

        // Set up custom toolbar with back navigation
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set up RecyclerView with a LinearLayoutManager and the MessageAdapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(this, messages);
        recyclerView.setAdapter(messageAdapter);

        // Set up click listener for the send button (Floating Action Button)
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    // Called when the user sends a message
    private void sendMessage() {
        String userInput = inputEditText.getText().toString().trim();
        if (userInput.isEmpty()) return;

        // Add the user's message to the chat
        addMessage("You: " + userInput, "user");
        inputEditText.setText("");
        hideKeyboard(this, inputEditText);
        recyclerView.scrollToPosition(messages.size() - 1);

        // Get the AI's response
        getAIResponse(userInput);
    }

    // Make an asynchronous call to the AI API
    private void getAIResponse(String userMessage) {
        if (isAITyping) return;
        isAITyping = true;

        // Add a placeholder AI message
        addMessage("AI: ...", "ai");

        // Construct the JSON payload; note the user's query is injected into the prompt
        String jsonPayload = "{\n" +
                "  \"prompt\": \"You are an AI-powered technical support assistant named 'TechSupportHubAI' within the TechSupport Hub mobile application. " +
                "Your purpose is to diagnose and resolve user-reported technical issues, including hardware and software problems, related to the user's devices. " +
                "You are friendly and helpful, and respond in a professional and helpful tone.\\n\\n" +
                "Respond directly to the user's query without asking follow-up questions. Only provide the most reasonable answer based on the issue described.\\n\\n" +
                "User Query: " + userMessage + "\"\n" +
                "}";

        Log.d(TAG, "JSON Payload: " + jsonPayload);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonPayload);
        final Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        // Asynchronous API call using OkHttp
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "API call failed", e);
                final String errorMessage = "Network error: " + e.getMessage();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateLastMessage(errorMessage);
                        isAITyping = false;
                        Toast.makeText(ChatWithAiActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string(); // Only call once
                    Log.d(TAG, "API Response: " + responseBody);

                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String text = jsonObject.getString("text"); // This is a JSON string
                        JSONObject innerJson = new JSONObject(text); // Parse the inner JSON
                        String aiResponse = innerJson.getString("response");

                        handler.post(() -> {
                            updateLastMessage(aiResponse);
                            isAITyping = false;
                        });
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON", e);
                        String errorMessage = "Parsing error: " + e.getMessage();
                        handler.post(() -> {
                            updateLastMessage(errorMessage);
                            isAITyping = false;
                            Toast.makeText(ChatWithAiActivity.this, "Error parsing server response", Toast.LENGTH_SHORT).show();
                        });
                    } finally {
                        response.close();
                    }
                } else {
                    Log.e(TAG, "Server returned error: " + response.code() + " " + response.message());
                    handler.post(() -> {
                        updateLastMessage("Server error: " + response.code());
                        isAITyping = false;
                        Toast.makeText(ChatWithAiActivity.this, "Server returned error", Toast.LENGTH_SHORT).show();
                    });
                }
            }

        });
    }

    // Add a new message to the list and update the RecyclerView
    private void addMessage(String text, String sender) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messages.add(new Message(text, sender));
                messageAdapter.notifyItemInserted(messages.size() - 1);
                recyclerView.scrollToPosition(messages.size() - 1);
            }
        });
    }

    // Update the last message (used to replace the AI's placeholder with the actual response)
    private void updateLastMessage(String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messages.size() > 0 && messages.get(messages.size() - 1).getSender().equals("ai")) {
                    messages.get(messages.size() - 1).setText("AI: " + text);
                    messageAdapter.notifyItemChanged(messages.size() - 1);
                    recyclerView.scrollToPosition(messages.size() - 1);
                }
            }
        });
    }

    // Hide the keyboard after sending a message
    private void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
