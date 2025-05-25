package com.miroslava958.objectdetectionandassistance;

import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
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
    private OverlayView overlayView;

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

        try {
            // Load the pre-trained TFLite model file from the assets folder
            AssetFileDescriptor fileDescriptor = getAssets().openFd("efficientdet_lite0.tflite");
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();

            // Map the model file into memory and initialise the interpreter
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            tflite = new Interpreter(buffer);

            int[] inputShape = tflite.getInputTensor(0).shape(); // [1, 320, 320, 3]
            DataType inputType = tflite.getInputTensor(0).dataType(); // UINT8 or FLOAT32

            Log.d("ModelInput", "Shape: " + Arrays.toString(inputShape) + ", Type: " + inputType);

            // Load label list/object from labelmap.txt in assets
            labels = FileUtil.loadLabels(this, "labelmap.txt");

            // Notify the user that the model was loaded successfully
            Toast.makeText(this, "Model loaded successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load model", Toast.LENGTH_SHORT).show();
        }

        // Check if the app has permission to access the camera, if not asks the user to grant it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if the app currently has CAMERA permission
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Prompt the user to grant permission
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1001);
            }
        }

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
                // Create an instance of the custom ObjectDetector class
                ObjectDetector analyser = new ObjectDetector(this, tflite, labels, overlayView);

                // Set up the ImageAnalysis to analyse frames from the camera
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // use latest frame only
                        .build();

                // Set the custom analyser to handle each frame
                ObjectDetector analyzer = new ObjectDetector(this, tflite, labels, overlayView);
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), analyzer);
                // Unbind any previous use cases before binding new ones
                cameraProvider.unbindAll();
                // Bind the preview and analysis use cases to the activity's lifecycle
                Camera camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                // Handle any errors during camera setup
                Log.e("CameraX", "Camera initialisation failed", e);
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }
}
