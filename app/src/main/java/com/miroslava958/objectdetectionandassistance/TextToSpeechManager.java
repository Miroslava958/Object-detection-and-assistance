package com.miroslava958.objectdetectionandassistance;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Manages text-to-speech functionality for the app.
 * Initialises the TTS and provides a method to speak text.
 * Handles shutdown of the TTS.
 *
 * Author: Miroslava Milcheva
 * Course: BSc Computing - Final Year Project
 */
public class TextToSpeechManager {

    private TextToSpeech tts;
    private boolean isInitialised = false;
    private String lastSpoken = "";
    private long lastSpokenTime = 0;
    private final Set<String> lastSpokenLabels = new HashSet<>();
    private static final long SPEAK_DELAY_MS = 2000; // Minimum delay between speech in ms

    /**
     * Constructor that initialises the TextToSpeech engine.
     *
     * @param context The application context.
     */
    public TextToSpeechManager(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Set the TTS language to UK English
                int result = tts.setLanguage(Locale.UK);

                // Customise pitch and speech rate for clarity
                tts.setPitch(1.5f);      // Slightly higher pitch
                tts.setSpeechRate(1.1f); // Slightly slower rate

                // Check if the language data is available and supported
                isInitialised = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED;

                if (!isInitialised) {
                    Log.e("TTS", "Language not supported or missing data");
                }
            } else {
                Log.e("TTS", "Initialisation failed");
            }
        });
    }

    /**
     * Speaks the given text aloud if TTS is initialised.
     * Avoids repeating the same label or speaking too frequently.
     *
     * @param text The string to be spoken
     */
    public void speak(String text) {
        long now = System.currentTimeMillis();

        if (isInitialised && text != null && !text.isEmpty()) {
            if (!text.equalsIgnoreCase(lastSpoken) || (now - lastSpokenTime > SPEAK_DELAY_MS)) {
                tts.speak("I see a " + text, TextToSpeech.QUEUE_FLUSH, null, null);
                lastSpoken = text;
                lastSpokenTime = now;
            }
        }
    }

    /**
     * Speaks only new labels from the current detection results.
     *
     * @param currentLabels List of detected labels in the current frame
     */
    public void speakMultiple(List<String> currentLabels) {
        if (!isInitialised || currentLabels == null || currentLabels.isEmpty()) return;

        Set<String> currentSet = new HashSet<>(currentLabels);

        // Find only the new labels, not already spoken
        currentSet.removeAll(lastSpokenLabels);

        if (!currentSet.isEmpty()) {
            String toSpeak = String.join(", ", currentSet);
            tts.speak("I see: " + toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);

            // Update the spoken history
            lastSpokenLabels.clear();
            lastSpokenLabels.addAll(currentLabels);
        }
    }

    /**
     * Clears the history of last spoken labels to avoid repeated announcements.
     */
    public void clearLastSpokenLabels() {
        lastSpokenLabels.clear();
    }

    /**
     * Releases the TTS resources.
     * Should be called from the activity's onDestroy method.
     */
    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}