package com.miroslava958.objectdetectionandassistance;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

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
     *
     * @param text The string to be spoken
     */
    public void speak(String text) {
        if (isInitialised && text != null && !text.isEmpty()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
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