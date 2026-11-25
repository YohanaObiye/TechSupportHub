package com.example.techsupporthub.admin;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.techsupporthub.R;
import com.example.techsupporthub.models.UserModel;
import com.example.techsupporthub.utils.Config;
import com.example.techsupporthub.utils.Constants;
import com.example.techsupporthub.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateTechniciansActivity extends AppCompatActivity {

    private static final String TAG = "CreateTechnician";
    private final OkHttpClient client = new OkHttpClient();

    private ImageView imgProfile;
    private MaterialButton btnSelectImage, btnCreate;
    private TextInputEditText edName, edEmail, edPassword, edConfirmPassword, edDescription;
    private TextInputLayout tilPassword, tilConfirmPassword;
    private AutoCompleteTextView edCategory;
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;

    // Launchers for permissions and image picking
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    // Firebase Authentication instance
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_technicians);

        // Set up toolbar and back navigation
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind UI components
        imgProfile = findViewById(R.id.img_profile);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnCreate = findViewById(R.id.btn_create);
        edName = findViewById(R.id.ed_name);
        edEmail = findViewById(R.id.ed_email);
        edPassword = findViewById(R.id.ed_password);
        edConfirmPassword = findViewById(R.id.ed_confirm_password);
        edDescription = findViewById(R.id.ed_description);
        edCategory = findViewById(R.id.ed_category);
        tilPassword = findViewById(R.id.til_password);           // if you have defined IDs
        tilConfirmPassword = findViewById(R.id.til_confirm_password); // optional

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Constants.DEVICE_CATEGORIES);
        edCategory.setAdapter(adapter);

        // Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    for (boolean granted : permissions.values()) {
                        allGranted = allGranted && granted;
                    }
                    if (allGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(CreateTechniciansActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Initialize image picker launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                            imgProfile.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(CreateTechniciansActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Set up button click listeners
        btnSelectImage.setOnClickListener(v -> requestPermissionsBasedOnVersion());
        btnCreate.setOnClickListener(v -> validateAndSendData());
    }

    // Request image reading permissions based on Android version
    private void requestPermissionsBasedOnVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            });
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            });
        } else {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            });
        }
    }

    // Open image picker intent
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    // Validate user input and start technician creation process
    private void validateAndSendData() {
        String name = edName.getText().toString().trim();
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        String confirmPassword = edConfirmPassword.getText().toString().trim();
        String category = edCategory.getText().toString().trim();
        String about = edDescription.getText().toString().trim();

        if (name.isEmpty()) {
            edName.setError("Name is required");
            return;
        }
        if (email.isEmpty()) {
            edEmail.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            edPassword.setError("Password is required");
            return;
        }
        if (confirmPassword.isEmpty()) {
            edConfirmPassword.setError("Confirm your password");
            return;
        }
        if (!password.equals(confirmPassword)) {
            edConfirmPassword.setError("Passwords do not match");
            return;
        }
        if (category.isEmpty()) {
            edCategory.setError("Device category is required");
            return;
        }
        if (about.isEmpty()) {
            edDescription.setError("Description is required");
            return;
        }
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        // First, upload the image
        uploadImage(name, email, password, category, about);
    }

    // Upload image to Supabase and then register technician
    private void uploadImage(String name, String email, String password, String category, String about) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String uniqueFileName = UUID.randomUUID().toString() + getFileExtension(selectedImageUri);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            String mimeType = getContentResolver().getType(selectedImageUri);
            if ("image/png".equals(mimeType)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            }
            byte[] imageBytes = baos.toByteArray();

            MediaType mediaType = MediaType.parse(mimeType);
            RequestBody requestFile = RequestBody.create(mediaType, imageBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", uniqueFileName, requestFile);

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", uniqueFileName, requestFile)
                    .build();

            String uploadUrl = Config.SUPABASE_URL + "/storage/v1/object/" + Config.STORAGE_BUCKET + "/" + uniqueFileName;

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .addHeader("Authorization", Config.AUTHORIZATION_TOKEN)
                    .addHeader("apiKey", Config.API_KEY)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(CreateTechniciansActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String imageUrl = Config.SUPABASE_URL + "/storage/v1/object/" + Config.STORAGE_BUCKET + "/" + uniqueFileName;
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Glide.with(CreateTechniciansActivity.this).load(imageUrl).into(imgProfile);
                            // Proceed with Firebase registration after image upload
                            registerTechnician(name, email, password, category, about, imageUrl);
                        });
                    } else {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(CreateTechniciansActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                        });
                        Log.d(TAG, "Supabase response: " + response);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(CreateTechniciansActivity.this, "Failed to process image", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Helper to get file extension based on MIME type
    private String getFileExtension(Uri uri) {
        ContentResolver resolver = getContentResolver();
        String mimeType = resolver.getType(uri);
        if ("image/png".equals(mimeType)) {
            return ".png";
        } else if ("image/jpeg".equals(mimeType) || "image/jpg".equals(mimeType)) {
            return ".jpg";
        } else {
            return ".jpg"; // default
        }
    }

    // Register technician using Firebase Auth then push record to Realtime DB
    private void registerTechnician(String name, String email, String password, String category, String about, String imageUrl) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering technician...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get newly created user
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            // Create a new technician record
                            UserModel technician = new UserModel(
                                    uid,
                                    name,
                                    email,
                                    "technician", // role set as technician
                                    System.currentTimeMillis(),
                                    category,
                                    about, imageUrl
                            );
                            // Optionally, you can also add the profile image URL to your model if needed

                            // Push record to Firebase Realtime Database under USERS node
                            Utils.getDatabaseReference(Constants.USERS_DB).child(uid)
                                    .setValue(technician)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(CreateTechniciansActivity.this, "Technician created successfully", Toast.LENGTH_SHORT).show();
                                        // Sign out the technician to avoid auto login
                                        mAuth.signOut();
                                        // Optionally, navigate back to admin dashboard
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(CreateTechniciansActivity.this, "Failed to save technician: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(CreateTechniciansActivity.this, "Registration failed: User is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(CreateTechniciansActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
