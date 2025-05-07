package com.miroslava958.objectdetectionandassistance;

import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;

/**
 * MainActivity is the entry point of the Android application.
 * It initialises the layout, requests runtime camera permissions, and
 * starts the live camera preview using CameraX.
 * The activity forms the base for integrating real-time object detection
 * and audio feedback to assist visually impaired users.
 *
 * Course: BSc Computing - Final year project
 * Project: Object detection and assistance program for visually impaired users
 * @author Miroslava Milcheva
 * @version 1.0
 * @since 2025-05-01
 */

public class MainActivity extends AppCompatActivity {

    // PreviewView is the UI component that displays the camera feed
    private PreviewView previewView;
    /**
     * Called when the activity is first created.
     * Sets the layout, requests camera permission if needed, and initializes the camera preview.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the app has permission to access the camera, if not asks the user to grant it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Check if the app currently has CAMERA permission
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1001);
            }
        }

        // Link the PreviewView from layout to the variable
        previewView = findViewById(R.id.previewView);
        // Start the camera preview
        startCamera();
    }

    /**
     * Sets up and starts the CameraX preview.
     * Turns on the back camera and displays the live feed in the app.
     * The preview runs in the background and automatically stops when the activity is closed.
     */
    private void startCamera() {
        // Get the camera provider asynchronously
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        // When the camera provider is ready, initialise the camera
        cameraProviderFuture.addListener(() -> {
            try {
                // Get the actual camera provider
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Create a Preview use case
                Preview preview = new Preview.Builder().build();
                // Connect the preview to the PreviewView surface
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                // Select the back camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                // Unbind any previous use cases before binding new ones
                cameraProvider.unbindAll();
                // Bind the preview use case to the activity's lifecycle
                Camera camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview);

            } catch (ExecutionException | InterruptedException e) {
                // Handle any errors during camera setup
                Log.e("CameraX", "Camera initialization failed", e);
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }
}