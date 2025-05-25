package com.miroslava958.objectdetectionandassistance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A view that draws bounding boxes and labels on top of the camera preview.
 * The class makes the visual feedback accessible and easy to interpret for users with low vision.
 * Designed to overlay object detection output from TensorFlow Lite on the live camera feed.
 *
 * @author Miroslava
 */
public class OverlayView extends View {
    // List of detection results on the view
    private List<DetectionResult> results = new ArrayList<>();
    // Paint for drawing the bounding boxes
    private final Paint boxPaint = new Paint();
    // Paint for drawing the text labels
    private final Paint textPaint = new Paint();

    /**
     * Constructor used when creating the view from layout.
     *
     * @param context The context of the app
     * @param attrs   The attributes set from the layout
     */
    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Set up paint for bounding boxes
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(6); // Thick border for better visibility
        boxPaint.setColor(Color.YELLOW); // High contrast color
        boxPaint.setAntiAlias(true); // Smooth edges

        // Set up paint for labels
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE); // Readable against most backgrounds
        textPaint.setTextSize(54); // Large text for readability
        textPaint.setFakeBoldText(true); // Bold for emphasis
        textPaint.setShadowLayer(6.0f, 2.0f, 2.0f, Color.BLACK); // Add shadow for contrast
        textPaint.setAntiAlias(true); // Smooth text edges
    }

    /**
     * Sets the list of results to be drawn.
     *
     * @param results A list of DetectionResult objects to be visualised
     */
    public void setResults(List<DetectionResult> results) {
        this.results = results;
        invalidate(); // Request redraw of the view
    }

    /**
     * Called by the system to draw the bounding boxes and labels.
     *
     * @param canvas The Canvas to draw onto
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        for (DetectionResult result : results) {
            RectF box = result.getBoundingBox();
            // Draw the bounding box
            canvas.drawRect(box, boxPaint);

            // Draw the label slightly above the top-left of the box to avoid going off-screen)
            float labelY = Math.max(box.top - 15, 50); // Prevent text from going above the screen
            canvas.drawText(result.getLabel(), box.left + 10, labelY, textPaint);

            android.util.Log.d("Overlay", "Drawing " + result.getLabel() + " at " + box);
        }
    }
}