package com.miroslava958.objectdetectionandassistance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.media.Image;
import android.util.Log;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ObjectDetector performs real-time image analysis using a TensorFlow Lite model.
 * It converts each camera frame into a Bitmap, runs inference, and provides
 * Create bounding boxes and textual feedback of detected objects.
 *
 * Author: Miroslava Milcheva
 * Course: BSc Computing - Final Year Project
 */
public class ObjectDetector implements ImageAnalysis.Analyzer {

    private final Interpreter tflite;
    private final List<String> labels;
    private final Context context;
    private final OverlayView overlayView;


    /**
     * Constructs the ObjectDetector with the necessary components.
     *
     * @param context      The app context (used for Toasts and UI updates)
     * @param tflite       The TensorFlow Lite model interpreter
     * @param labels       A list of label names for detected classes
     * @param overlayView  The custom view used to draw detection bounding boxes
     */
    public ObjectDetector(Context context, Interpreter tflite, List<String> labels, OverlayView overlayView) {
        this.context = context;
        this.tflite = tflite;
        this.labels = labels;
        this.overlayView = overlayView;
    }

    /**
     * Converts the camera image to a Bitmap, processes it through the model,
     * and draws bounding boxes and labels for high-confidence detections.
     *
     * @param imageProxy The input image from CameraX
     */
    public void analyze(@NonNull ImageProxy imageProxy) {
        Log.d("TFLite", "analyze() called for a new frame.");
        @SuppressLint("UnsafeOptInUsageError")
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {
            try {
                // Convert the YUV image to RGB Bitmap
                Bitmap bitmap = ImageUtils.toBitmapFromYUV(mediaImage);

                // Resize to model input size
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, 320, 320, true);
                TensorImage tensorImage = new TensorImage(DataType.UINT8);
                tensorImage.load(resized);

                // Prepare output arrays
                float[][][] outputBoxes = new float[1][25][4];      // Bounding boxes
                float[][] outputScores = new float[1][25];          // Confidence scores
                float[][] outputClasses = new float[1][25];         // Class indices
                float[] numDetections = new float[1];               // Number of detections

                // Prepare input/output for model
                Object[] inputs = new Object[]{tensorImage.getBuffer()};
                Map<Integer, Object> outputs = new HashMap<>();
                outputs.put(0, outputBoxes);
                outputs.put(1, outputClasses);
                outputs.put(2, outputScores);
                outputs.put(3, numDetections);

                // Run the model
                tflite.runForMultipleInputsOutputs(inputs, outputs);

                // Log raw model output to check if inference is working
                Log.d("TFLite", "numDetections: " + numDetections[0]);

                if (outputScores[0][0] > 0) {
                    Log.d("TFLite", "First detection - Score: " + outputScores[0][0] +
                            ", Class: " + outputClasses[0][0]);
                } else {
                    Log.d("TFLite", "No high-confidence detections in this frame.");
                }
                // Collect results
                List<DetectionResult> results = new ArrayList<>();

                Log.d("Output", "Model returned numDetections: " + numDetections[0]);

                for (int i = 0; i < numDetections[0]; i++) {
                    float score = outputScores[0][i];
                    if (score > 0.5f) {
                        int labelIndex = (int) outputClasses[0][i];
                        String label = (labelIndex >= 0 && labelIndex < labels.size()) ? labels.get(labelIndex) : "Unknown";

                        Log.d("Detection", "Class index: " + labelIndex + ", Label: " + label + ", Score: " + score);

                        float[] box = outputBoxes[0][i]; // top, left, bottom, right

                        Log.d("BoxDebug", "Raw box: top=" + box[0] + ", left=" + box[1] + ", bottom=" + box[2] + ", right=" + box[3]);
                        Log.d("BoxDebug", "Label: " + label + " Score: " + score);

                        // Scale the coordinates to match the bitmap size
                        int previewWidth = overlayView.getWidth();
                        int previewHeight = overlayView.getHeight();

                        float left = Math.max(0f, box[1]) * previewWidth;
                        float top = Math.max(0f, box[0]) * previewHeight;
                        float right = Math.min(1f, box[3]) * previewWidth;
                        float bottom = Math.min(1f, box[2]) * previewHeight;

                        RectF rect = new RectF(left, top, right, bottom);

                        results.add(new DetectionResult(rect, label, score));
                        Log.d("TFLite", "Detected: " + label + " (" + score + ")");
                    }
                }

                // Pass results to OverlayView
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        overlayView.setResults(results);
                    });
                }

            } catch (Exception e) {
                Log.e("ObjectDetector", "Detection failed: " + e.getMessage(), e);
            } finally {
                imageProxy.close(); // Always release the image
            }
        } else {
            imageProxy.close(); // Safeguard if mediaImage is null
        }
    }
}