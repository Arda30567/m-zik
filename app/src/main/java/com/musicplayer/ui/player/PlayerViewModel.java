package com.musicplayer.ui.player;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.musicplayer.data.local.entities.Track;
import com.musicplayer.service.MusicPlayerService;

import java.util.List;

/**
 * ViewModel for the music player.
 * Manages UI state and communicates with the music player service.
 */
public class PlayerViewModel extends ViewModel {
    
    private MusicPlayerService musicPlayerService;
    
    // UI state
    private final MutableLiveData<Boolean> isServiceConnected = new MutableLiveData<>(false);
    private final MutableLiveData<Track> currentTrack = new MutableLiveData<>();
    private final MutableLiveData<Integer> playbackState = new MutableLiveData<>();
    private final MutableLiveData<Long> currentPosition = new MutableLiveData<>(0L);
    private final MutableLiveData<Long> duration = new MutableLiveData<>(0L);
    private final MutableLiveData<Float> playbackSpeed = new MutableLiveData<>(1.0f);
    private final MutableLiveData<Boolean> isShuffleMode = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> repeatMode = new MutableLiveData<>(0);
    
    public PlayerViewModel() {
        // Initialize default values
        playbackState.setValue(MusicPlayerService.STATE_IDLE);
    }
    
    /**
     * Sets the music player service instance.
     */
    public void setMusicPlayerService(MusicPlayerService service) {
        this.musicPlayerService = service;
        isServiceConnected.setValue(service != null);
        
        if (service != null) {
            // Observe service state
            observeServiceState();
        }
    }
    
    /**
     * Observes state changes from the music player service.
     */
    private void observeServiceState() {
        if (musicPlayerService == null) return;
        
        // Observe current track
        musicPlayerService.getCurrentTrack().observeForever(track -> {
            currentTrack.postValue(track);
        });
        
        // Observe playback state
        musicPlayerService.getPlaybackState().observeForever(state -> {
            playbackState.postValue(state);
        });
        
        // Observe current position
        musicPlayerService.getCurrentPositionMs().observeForever(position -> {
            currentPosition.postValue(position);
        });
        
        // Observe duration
        // Note: You might need to add a getDuration() method to the service
        // For now, we'll update it when track changes
        currentTrack.observeForever(track -> {
            if (track != null) {
                duration.postValue(track.getDuration());
            }
        });
        
        // Observe playback speed
        playbackSpeed.postValue(musicPlayerService.getPlaybackSpeed());
        
        // Observe shuffle mode
        isShuffleMode.postValue(musicPlayerService.isShuffleMode());
        
        // Observe repeat mode
        repeatMode.postValue(musicPlayerService.getRepeatMode());
    }
    
    // Playback control methods
    
    public void playTrack(Track track) {
        if (musicPlayerService != null) {
            musicPlayerService.playTrack(track);
        }
    }
    
    public void playPlaylist(List<Track> playlist, int startPosition) {
        if (musicPlayerService != null) {
            musicPlayerService.playPlaylist(playlist, startPosition);
        }
    }
    
    public void togglePlayback() {
        if (musicPlayerService != null) {
            musicPlayerService.togglePlayback();
        }
    }
    
    public void play() {
        if (musicPlayerService != null) {
            musicPlayerService.play();
        }
    }
    
    public void pause() {
        if (musicPlayerService != null) {
            musicPlayerService.pause();
        }
    }
    
    public void stop() {
        if (musicPlayerService != null) {
            musicPlayerService.stop();
        }
    }
    
    public void next() {
        if (musicPlayerService != null) {
            musicPlayerService.next();
        }
    }
    
    public void previous() {
        if (musicPlayerService != null) {
            musicPlayerService.previous();
        }
    }
    
    public void seekTo(long position) {
        if (musicPlayerService != null) {
            musicPlayerService.seekTo(position);
        }
    }
    
    public void setPlaybackSpeed(float speed) {
        if (musicPlayerService != null) {
            musicPlayerService.setPlaybackSpeed(speed);
            playbackSpeed.postValue(speed);
        }
    }
    
    public void toggleShuffle() {
        if (musicPlayerService != null) {
            musicPlayerService.toggleShuffle();
            isShuffleMode.postValue(musicPlayerService.isShuffleMode());
        }
    }
    
    public void cycleRepeatMode() {
        if (musicPlayerService != null) {
            musicPlayerService.cycleRepeatMode();
            repeatMode.postValue(musicPlayerService.getRepeatMode());
        }
    }
    
    // Getters for LiveData
    
    public LiveData<Boolean> getIsServiceConnected() {
        return isServiceConnected;
    }
    
    public LiveData<Track> getCurrentTrack() {
        return currentTrack;
    }
    
    public LiveData<Integer> getPlaybackState() {
        return playbackState;
    }
    
    public LiveData<Long> getCurrentPosition() {
        return currentPosition;
    }
    
    public LiveData<Long> getDuration() {
        return duration;
    }
    
    public LiveData<Float> getPlaybackSpeed() {
        return playbackSpeed;
    }
    
    public LiveData<Boolean> getIsShuffleMode() {
        return isShuffleMode;
    }
    
    public LiveData<Integer> getRepeatMode() {
        return repeatMode;
    }
    
    // State checkers
    
    public boolean isPlaying() {
        return musicPlayerService != null && musicPlayerService.isPlaying();
    }
    
    public boolean isServiceBound() {
        return musicPlayerService != null;
    }
    
    // Utility methods
    
    public int getProgressPercentage() {
        Long current = currentPosition.getValue();
        Long total = duration.getValue();
        
        if (current != null && total != null && total > 0) {
            return (int) ((current * 100) / total);
        }
        return 0;
    }
    
    public String getFormattedPosition() {
        Long position = currentPosition.getValue();
        if (position != null) {
            return formatTime(position);
        }
        return "0:00";
    }
    
    public String getFormattedDuration() {
        Long total = duration.getValue();
        if (total != null) {
            return formatTime(total);
        }
        return "0:00";
    }
    
    /**
     * Formats time in milliseconds to MM:SS or HH:MM:SS format.
     */
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else {
            return String.format("%d:%02d", minutes, seconds % 60);
        }
    }
}