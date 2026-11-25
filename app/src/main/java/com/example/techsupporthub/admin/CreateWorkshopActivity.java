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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.techsupporthub.R;
import com.example.techsupporthub.model.Workshop;
import com.example.techsupporthub.utils.Config;
import com.example.techsupporthub.utils.Constants;
import com.example.techsupporthub.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateWorkshopActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 101;
    private final OkHttpClient client = new OkHttpClient();
    private ImageView imgImage;
    private MaterialButton btnSelectImage;
    private MaterialButton createButton;
    private TextInputEditText edTitle;
    private TextInputEditText edStartDate;
    private TextInputEditText edEndDate;
    private TextInputEditText edDescription;
    private TextInputLayout tilStartDate;
    private TextInputLayout tilEndDate;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ProgressDialog progressDialog;
    private Uri selectedImageUri;
    private String startDate;
    private String endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workshop);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        imgImage = findViewById(R.id.img_image);
        btnSelectImage = findViewById(R.id.btn_select_image);
        createButton = findViewById(R.id.create);
        edTitle = findViewById(R.id.ed_title);
        edStartDate = findViewById(R.id.ed_start_date);
        edEndDate = findViewById(R.id.ed_end_date);
        edDescription = findViewById(R.id.ed_description);
        tilStartDate = findViewById(R.id.til_start_date);
        tilEndDate = findViewById(R.id.til_end_date);


        // Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    for (boolean isGranted : permissions.values()) {
                        allGranted = allGranted && isGranted;
                    }
                    if (allGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
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
                            imgImage.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        btnSelectImage.setOnClickListener(v -> requestPermissionsBasedOnVersion());

        edStartDate.setOnClickListener(v -> openStartDatePicker());
        edEndDate.setOnClickListener(v -> openEndDatePicker());

        createButton.setOnClickListener(v -> validateAndSendData());
    }

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

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void openStartDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Start Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER_START");
        datePicker.addOnPositiveButtonClickListener(selection -> {
            startDate = formatDate(selection);
            edStartDate.setText(startDate);
        });
    }

    private void openEndDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select End Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER_END");
        datePicker.addOnPositiveButtonClickListener(selection -> {
            endDate = formatDate(selection);
            edEndDate.setText(endDate);
        });
    }

    private String formatDate(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date(timeInMillis));
    }

    private void validateAndSendData() {
        String title = edTitle.getText().toString().trim();
        String description = edDescription.getText().toString().trim();

        if (title.isEmpty()) {
            edTitle.setError("Title is required");
            return;
        }

        if (startDate == null || startDate.isEmpty()) {
            tilStartDate.setError("Start date is required");
            return;
        }

        if (endDate == null || endDate.isEmpty()) {
            tilEndDate.setError("End date is required");
            return;
        }

        if (description.isEmpty()) {
            edDescription.setError("Description is required");
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadImage(title, description, startDate, endDate);
    }

    private void uploadImage(String title, String description, String startDate, String endDate) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Generate a unique file name
        String uniqueFileName = UUID.randomUUID().toString() + getFileExtension(selectedImageUri);

        // Convert Uri to byte array
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // Determine the MIME type and compress the bitmap accordingly
            String mimeType = getContentResolver().getType(selectedImageUri);
            if ("image/png".equals(mimeType)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            }

            byte[] byteArray = byteArrayOutputStream.toByteArray();

            MediaType mediaType = MediaType.parse(mimeType);
            RequestBody requestFile = RequestBody.create(mediaType, byteArray);
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
                public void onFailure(okhttp3.Call call, IOException e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(CreateWorkshopActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d("SUPABASE_ONE_RESPONSE", responseBody);
                        String imageUrl = Config.SUPABASE_URL + "/storage/v1/object/" + Config.STORAGE_BUCKET + "/" + uniqueFileName;
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Glide.with(CreateWorkshopActivity.this).load(imageUrl).into(imgImage);
                            sendToDB(title, description, startDate, endDate, imageUrl);
                        });
                    } else {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(CreateWorkshopActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                        });
                        Log.d("SUPABASE_ONE", response.toString());
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String mimeType = contentResolver.getType(uri);
        if ("image/png".equals(mimeType)) {
            return ".png";
        } else if ("image/jpeg".equals(mimeType) || "image/jpg".equals(mimeType)) {
            return ".jpg";
        } else {
            return ".jpg"; // Default to JPEG if unknown
        }
    }

    private void sendToDB(String title, String description, String startDate, String endDate, String imageUrl) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading data...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        // Generate a unique ID for the workshop
        String workshopId = Utils.getDatabaseReference(Constants.WORKSHOP_DB).push().getKey();

        // Create a Workshop object
        Workshop workshop = new Workshop(
                workshopId,
                title,
                description,
                startDate,
                endDate,
                imageUrl,
                System.currentTimeMillis()
        );

        // Push the workshop to Firebase Realtime Database
        if (workshopId != null) {
            Utils.getDatabaseReference(Constants.WORKSHOP_DB).child(workshopId).setValue(workshop)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(CreateWorkshopActivity.this, "Data sent to DB", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();

                        Toast.makeText(CreateWorkshopActivity.this, "Failed to send data to DB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to generate a unique ID", Toast.LENGTH_SHORT).show();
        }
    }
}