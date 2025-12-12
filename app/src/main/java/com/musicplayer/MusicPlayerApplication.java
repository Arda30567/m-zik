package com.musicplayer;

import android.app.Application;
import android.content.Context;

import com.musicplayer.data.local.AppDatabase;
import com.musicplayer.repository.PlaylistRepository;
import com.musicplayer.repository.TrackRepository;
import com.musicplayer.service.MusicPlayerService;

/**
 * Application class for the Music Player app.
 * Initializes global components and provides application-wide context.
 */
public class MusicPlayerApplication extends Application {
    
    private static MusicPlayerApplication instance;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Initialize repositories
        initializeRepositories();
        
        // Initialize other global components
        initializeComponents();
    }
    
    /**
     * Gets the singleton instance of the application.
     */
    public static MusicPlayerApplication getInstance() {
        return instance;
    }
    
    /**
     * Gets the application context.
     */
    public static Context getAppContext() {
        return instance.getApplicationContext();
    }
    
    /**
     * Initializes repositories and other application-wide components.
     */
    private void initializeRepositories() {
        // Initialize database
        AppDatabase.getInstance(this);
        
        // Initialize repositories
        TrackRepository.getInstance(this);
        PlaylistRepository.getInstance(this);
    }
    
    /**
     * Initializes other global components.
     */
    private void initializeComponents() {
        // Initialize any other application-wide components
        // For example: crash reporting, analytics, etc.
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        // Clean up resources if needed
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // Handle low memory situation
        // For example: clear caches, release non-essential resources
    }
}