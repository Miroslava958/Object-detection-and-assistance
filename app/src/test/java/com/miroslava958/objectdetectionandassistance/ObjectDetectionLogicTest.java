package com.miroslava958.objectdetectionandassistance;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ObjectDetectionLogicTest {
    private List<String> labels;

    @Before
    public void setUp() throws Exception {
        // Simulate loading labelmap.txt from resources
        labels = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(this.getClass().getResourceAsStream("/labelmap_cleaned.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                labels.add(line.trim());
            }
        }
    }

    @Test
    public void testLabels() {
        int[] mockClasses = {77, 0, 3, 15};
        float[] mockScores = {0.82f, 0.45f, 0.67f, 0.29f};
        float threshold = 0.5f;
        List<String> detectedLabels = new ArrayList<>();

        for (int i = 0; i < mockScores.length; i++) {
            float score = mockScores[i];
            int index = mockClasses[i];

            if (score > threshold && index < labels.size()) {
                String label = labels.get(index);
                if (!label.toLowerCase().startsWith("unknown")) {
                    detectedLabels.add(label);
                }
            }
        }
        assertEquals(2, detectedLabels.size());
        assertTrue(detectedLabels.contains("cell phone"));
        assertTrue(detectedLabels.contains("car"));
    }
}