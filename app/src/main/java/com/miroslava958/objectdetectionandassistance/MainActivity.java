package com.miroslava958.objectdetectionandassistance;

import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * MainActivity is the entry point of the Android application.
 * It initialises the layout, requests runtime camera permissions, and
 * starts the live camera preview using CameraX.
 * The activity forms the base for integrating real-time object detection
 * and audio feedback to assist visually impaired users.
 *
 * Course: BSc Computing - Final year project
 * Project: Object detection and assistance programme for visually impaired users
 * Author: Miroslava Milcheva
 * Version: 1.0
 * Since: 2025-05-01
 */
public class MainActivity extends AppCompatActivity {
    // PreviewView is the UI component that displays the camera feed
    private PreviewView previewView;
    // TensorFlow Lite interpreter for running the object detection model
    private Interpreter tflite;
    // Label list corresponding to the model's output classes
    private List<String> labels;
    // Custom view that overlays bounding boxes and labels on top of the camera preview
    private OverlayView overlayView;
    // Object detection engine that handles camera frame analysis and TFLite inference
    private ObjectDetector objectDetector;
    // Manages text-to-speech functionality to provide spoken feedback to the user
    private TextToSpeechManager ttsManager;

    /**
     * Called when the activity is first created.
     * Sets the layout, requests camera permission if needed, and initialises the camera preview.
     *
     * @param savedInstanceState Saves state of the activity if it was previously paused or stopped
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the user interface layout for this activity
        setContentView(R.layout.activity_main);
        // Link the OverlayView from layout to the variable
        overlayView = findViewById(R.id.overlayView);
        // Link the PreviewView from layout to the variable
        previewView = findViewById(R.id.previewView);
        // Initialise TextToSpeech
        ttsManager = new TextToSpeechManager(this);

        // Load the model and label list
        try (
                // Open the TFLite model file from the assets folder
                AssetFileDescriptor fileDescriptor = getAssets().openFd("efficientdet_lite0.tflite");

                // Create input stream and channel to read the model data
                FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
                FileChannel fileChannel = inputStream.getChannel()
        ) {
            // Get the start and length of the file
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            // Map the model file into memory
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            // Initialise the TFLite interpreter with the model
            tflite = new Interpreter(buffer);

            // Get input shape and data type for debug/logging purposes
            int[] inputShape = tflite.getInputTensor(0).shape(); // e.g. [1, 320, 320, 3]
            DataType inputType = tflite.getInputTensor(0).dataType(); // e.g. UINT8 or FLOAT32
            Log.d("ModelInput", "Shape: " + Arrays.toString(inputShape) + ", Type: " + inputType);

            // Load the label map from assets
            labels = FileUtil.loadLabels(this, "labelmap.txt");

            // Inform the user that the model has been successfully loaded
            Toast.makeText(this, "Model loaded successfully!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            // Log the error and inform the user if the model failed to load
            Log.e("ModelLoad", "Error loading model: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to load model", Toast.LENGTH_SHORT).show();
        }

        // Create object detector only after interpreter and labels are loaded
        objectDetector = new ObjectDetector(this, tflite, labels, overlayView, ttsManager);

        // Check for camera permission and request if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1001);
        } else {
            // Start camera if permission already granted
            startCamera();
        }

        // Handle Stop App button click
        Button exitButton = findViewById(R.id.btnExit);
        exitButton.setOnClickListener(v -> {
            Toast.makeText(this, "App closing...", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    /**
     * Called after the user responds to a permission request.
     * If permission granted, camera is started.
     *
     * @param requestCode  Identifier for the permission request
     * @param permissions  The requested permissions
     * @param grantResults The grant results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
        }
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
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Create a Preview use case
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Select the back camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Create ImageAnalysis and bind ObjectDetector to it
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), objectDetector);

                // Unbind any previous use cases before binding new ones
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Camera initialisation failed", e);
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Called when the activity is destroyed.
     * Shut down TextToSpeech and release resources.
     */
    @Override
    protected void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}