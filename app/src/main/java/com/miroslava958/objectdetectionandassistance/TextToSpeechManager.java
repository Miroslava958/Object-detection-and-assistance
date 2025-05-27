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
    private final Set<String> lastSpokenLabels = new HashSet<>();


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
            tts.speak("I see a " + toSpeak, TextToSpeech.QUEUE_ADD, null, null);
            Log.d("TTS", "Speaking: " + toSpeak);

            // Update the spoken history
            lastSpokenLabels.clear();
            lastSpokenLabels.addAll(currentLabels);
        }
        Log.d("TTS", "Last spoken labels: " + lastSpokenLabels);
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