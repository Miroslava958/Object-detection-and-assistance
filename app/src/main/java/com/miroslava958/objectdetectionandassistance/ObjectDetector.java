package com.miroslava958.objectdetectionandassistance;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ObjectDetector performs real-time image analysis using a TensorFlow Lite model.
 * It receives frames from CameraX, converts them into Bitmaps, runs inference,
 * and logs object detection results.
 *
 * Author: Miroslava Milcheva
 * Course: BSc Computing - Final Year Project
 */
public class ObjectDetector implements ImageAnalysis.Analyzer {

    // TensorFlow Lite interpreter used to run the model
    private final Interpreter tflite;
    // List of labels representing object classes
    private final List<String> labels;

    /**
     * Constructs the ObjectDetector with a loaded TFLite model and label list.
     *
     * @param tflite  A TensorFlow Lite Interpreter for object detection
     * @param labels  A list of class labels for interpreting detection results
     */
    public ObjectDetector(Interpreter tflite, List<String> labels) {
        this.tflite = tflite;
        this.labels = labels;
    }

    /**
     * Called by CameraX to analise each camera frame.
     *
     * @param imageProxy The camera frame to process
     */
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        analyse(imageProxy);
    }

    /**
     * Converts the image frame to a Bitmap, performs object detection using TensorFlow Lite,
     * and logs detected labels.
     *
     * @param imageProxy The image frame received from the camera
     */
    public void analyse(@NonNull ImageProxy imageProxy) {
        @SuppressLint("UnsafeOptInUsageError")
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {
            try {
                // Convert the YUV image to a Bitmap using ImageUtils
                Bitmap bitmap = ImageUtils.toBitmapFromYUV(mediaImage);
                // Resize the Bitmap to the model's expected input dimensions
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
                // Prepare the image for TensorFlow Lite
                TensorImage tensorImage = new TensorImage(DataType.UINT8);
                tensorImage.load(resized);

                // Set up output containers for model inference
                float[][][] outputBoxes = new float[1][10][4];      // Bounding box coordinates
                float[][] outputScores = new float[1][10];          // Confidence scores
                float[][] outputClasses = new float[1][10];         // Class indices
                float[] numDetections = new float[1];               // Number of valid detections

                // Prepare model input and output mappings
                Object[] inputs = new Object[]{tensorImage.getBuffer()};
                Map<Integer, Object> outputs = new HashMap<>();
                outputs.put(0, outputBoxes);
                outputs.put(1, outputClasses);
                outputs.put(2, outputScores);
                outputs.put(3, numDetections);
                // Run the TensorFlow Lite model
                tflite.runForMultipleInputsOutputs(inputs, outputs);

                // Iterate over each detection result
                for (int i = 0; i < numDetections[0]; i++) {
                    float score = outputScores[0][i];
                    if (score > 0.5) {
                        int labelIndex = (int) outputClasses[0][i];
                        String label = labelIndex < labels.size() ? labels.get(labelIndex) : "Unknown";
                        Log.d("TFLite", "Detected: " + label + " (score: " + score + ")");
                    }
                }

            } catch (Exception e) {
                Log.e("ObjectDetector", "Detection failed: " + e.getMessage(), e);
            } finally {
                // Release the memory to free the camera
                imageProxy.close();
            }
        } else {
            Log.w("ObjectDetector", "Received null image from camera");
            imageProxy.close();
        }
    }
}