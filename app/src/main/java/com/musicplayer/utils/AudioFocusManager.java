package com.musicplayer.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

/**
 * Manages audio focus for the music player service.
 */
public class AudioFocusManager {
    
    private static final String TAG = "AudioFocusManager";
    
    private final Context context;
    private final AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private boolean hasAudioFocus = false;
    
    public AudioFocusManager(Context context) {
        this.context = context.getApplicationContext();
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }
    
    /**
     * Requests audio focus for music playback.
     */
    public boolean requestAudioFocus() {
        if (hasAudioFocus) {
            return true;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use AudioFocusRequest for Android O and above
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this::onAudioFocusChange)
                    .build();
            
            int result = audioManager.requestAudioFocus(audioFocusRequest);
            hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        } else {
            // Use deprecated method for older Android versions
            int result = audioManager.requestAudioFocus(
                    this::onAudioFocusChange,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
            );
            hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        }
        
        Log.d(TAG, "Audio focus request result: " + hasAudioFocus);
        return hasAudioFocus;
    }
    
    /**
     * Abandons audio focus.
     */
    public void abandonAudioFocus() {
        if (!hasAudioFocus) {
            return;
        }
        
        boolean result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result = audioManager.abandonAudioFocusRequest(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } else {
            result = audioManager.abandonAudioFocus(this::onAudioFocusChange) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
        
        hasAudioFocus = false;
        Log.d(TAG, "Audio focus abandoned: " + result);
    }
    
    /**
     * Handles audio focus changes.
     */
    private void onAudioFocusChange(int focusChange) {
        Log.d(TAG, "Audio focus change: " + focusChange);
        
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // We've regained audio focus
                hasAudioFocus = true;
                // Resume playback if needed
                break;
                
            case AudioManager.AUDIOFOCUS_LOSS:
                // We've lost audio focus permanently
                hasAudioFocus = false;
                // Stop playback
                break;
                
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // We've lost audio focus temporarily
                hasAudioFocus = false;
                // Pause playback
                break;
                
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // We've lost audio focus but can continue at lower volume
                hasAudioFocus = false;
                // Lower volume (duck)
                break;
                
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                // We've gained audio focus temporarily
                hasAudioFocus = true;
                // Restore normal volume
                break;
                
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                // We've gained audio focus but may need to duck
                hasAudioFocus = true;
                break;
                
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                // We've gained exclusive audio focus temporarily
                hasAudioFocus = true;
                break;
        }
    }
    
    /**
     * Checks if we currently have audio focus.
     */
    public boolean hasAudioFocus() {
        return hasAudioFocus;
    }
    
    /**
     * Gets the current audio focus state as a string for debugging.
     */
    public String getFocusStateString() {
        return hasAudioFocus ? "GAINED" : "LOST";
    }
}