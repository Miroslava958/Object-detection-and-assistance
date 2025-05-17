package com.miroslava958.objectdetectionandassistance;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ObjectDetectionLogicTest {

    private List<String> labels;

    @Before
    public void setUp() throws IOException {
        labels = new ArrayList<>();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("labelmap.txt")) {
            if (inputStream == null) {
                throw new IOException("labelmap.txt not found in test resources");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                labels.add(line);
            }
        }
    }

    private List<String> extractLabels(float[] scores, float[] classes, float threshold) {
        List<String> detected = new ArrayList<>();
        for (int i = 0; i < scores.length; i++) {
            float score = scores[i];
            int index = (int) classes[i];

            if (score > threshold && index >= 0 && index < labels.size()) {
                String label = labels.get(index);
                if (!label.toLowerCase().startsWith("unknown")) {
                    detected.add(label);
                }
            }
        }
        return detected;
    }

    @Test
    public void testLabels() {
        float[] scores = {0.82f, 0.45f, 0.67f, 0.29f};
        float[] classes = {77, 0, 3, 15};
        List<String> detectedLabels = extractLabels(scores, classes, 0.5f);

        System.out.println(detectedLabels);
        assertEquals(2, detectedLabels.size());
        assertTrue(detectedLabels.contains("cell phone"));
        assertTrue(detectedLabels.contains("car"));
    }

    @Test
    public void testScoreBelowThreshold() {
        float[] scores = {0.2f, 0.3f};
        float[] classes = {1, 2};
        List<String> result = extractLabels(scores, classes, 0.5f);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testInvalidIndex() {
        float[] scores = {0.9f};
        float[] classes = {100};
        List<String> result = extractLabels(scores, classes, 0.5f);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testUnknownLabel() {
        float[] scores = {0.95f};
        float[] classes = {0}; // "unknown_object_1"
        List<String> result = extractLabels(scores, classes, 0.5f);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testValidDetections() {
        float[] scores = {0.85f, 0.92f};
        float[] classes = {77, 3};
        List<String> result = extractLabels(scores, classes, 0.5f);
        assertEquals(2, result.size());
        assertTrue(result.contains("cell phone"));
        assertTrue(result.contains("car"));
    }

    @Test
    public void testValidInvalidLabels() {
        float[] scores = {0.85f, 0.3f, 0.95f, 0.8f};
        float[] classes = {77, 0, 2, 100};
        List<String> result = extractLabels(scores, classes, 0.5f);
        assertEquals(2, result.size());
        assertTrue(result.contains("cell phone"));
        assertTrue(result.contains("bicycle"));
    }
}