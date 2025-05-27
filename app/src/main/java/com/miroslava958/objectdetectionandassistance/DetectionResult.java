package com.miroslava958.objectdetectionandassistance;

import android.graphics.RectF;

/**
 * Represents the result of a single object detection.
 * Holds the bounding box, label, and score for each detected object.
 * Used to pass detection results to the overlay view for visualisation.
 *
 * @author Miroslava
 */
public class DetectionResult {
    // Rectangle defining the area of the detected object in the image
    private final RectF boundingBox;
    // Label of the detected object
    private final String label;
    // Confidence score for the detection
    private final float score;

    /**
     * Constructs a DetectionResult object.
     *
     * @param boundingBox Rectangle area around the detected object
     * @param label Text label for the object
     * @param score The level of the detection
     */
    public DetectionResult(RectF boundingBox, String label, float score) {
        this.boundingBox = boundingBox;
        this.label = label;
        this.score = score;
    }

    /**
     * Gets the bounding box of the detection.
     *
     * @return RectF shows the object's location in the image
     */
    public RectF getBoundingBox() {
        return boundingBox;
    }

    /**
     * Gets the label of the detection.
     *
     * @return The object label as "dog" or "chair"
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the score of the detection.
     *
     * @return Confidence as a float 0.0 to 1.0
     */
    public float getScore() {
        return score;
    }
}
