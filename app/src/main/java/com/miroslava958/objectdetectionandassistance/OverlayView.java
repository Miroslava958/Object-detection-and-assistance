package com.miroslava958.objectdetectionandassistance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

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

    /**
     * Constructor used when creating the view from layout.
     *
     * @param context The context of the app
     * @param attrs   The attributes set from the layout
     */
    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    /**
     * Sets the list of results to be drawn.
     *
     * @param results A list of DetectionResult objects to be visualise
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6); // Thick border for visibility
        paint.setColor(Color.YELLOW); // High contrast colour
        paint.setTextSize(48);
        paint.setTextAlign(Paint.Align.LEFT);

        for (DetectionResult result : results) {
            RectF box = result.getBoundingBox();

            // Draw the bounding box
            canvas.drawRect(box, paint);

            // Draw the label slightly above the top-left of the box
            canvas.drawText(result.getLabel(), box.left + 10, box.top - 10, paint);

            android.util.Log.d("Overlay", "Drawing " + result.getLabel() + " at " + box.toString());
        }
    }
}
