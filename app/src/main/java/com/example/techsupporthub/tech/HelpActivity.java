package com.example.techsupporthub.tech;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.techsupporthub.R;
import com.google.android.material.appbar.MaterialToolbar;

public class HelpActivity extends AppCompatActivity {

    private WebView helpWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize the WebView
        helpWebView = findViewById(R.id.help_webview);

        // Enable JavaScript if needed
        WebSettings webSettings = helpWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load the help.html file from the assets folder
        helpWebView.loadUrl("file:///android_asset/help.html");

        // Set a WebViewClient to handle page navigation within the WebView
        helpWebView.setWebViewClient(new WebViewClient());
    }

    @Override
    public void onBackPressed() {
        // If the WebView can go back, go back to the previous page
        if (helpWebView.canGoBack()) {
            helpWebView.goBack();
        } else {
            // Otherwise, call the superclass method to handle the back button press
            super.onBackPressed();
        }
    }
}