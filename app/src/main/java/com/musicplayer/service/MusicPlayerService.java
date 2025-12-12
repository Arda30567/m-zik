package com.musicplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.musicplayer.R;
import com.musicplayer.data.local.entities.Track;
import com.musicplayer.repository.TrackRepository;
import com.musicplayer.ui.main.MainActivity;
import com.musicplayer.utils.AudioFocusManager;
import com.musicplayer.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Music Player Service using ExoPlayer for audio playback.
 * Manages media playback, notifications, and media session.
 */
public class MusicPlayerService extends LifecycleService {
    
    private static final String TAG = "MusicPlayerService";
    private static final String CHANNEL_ID = "music_player_channel";
    private static final int NOTIFICATION_ID = 101;
    
    // Playback states
    public static final int STATE_IDLE = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_STOPPED = 3;
    
    // Actions
    public static final String ACTION_PLAY = "com.musicplayer.action.PLAY";
    public static final String ACTION_PAUSE = "com.musicplayer.action.PAUSE";
    public static final String ACTION_TOGGLE = "com.musicplayer.action.TOGGLE";
    public static final String ACTION_NEXT = "com.musicplayer.action.NEXT";
    public static final String ACTION_PREVIOUS = "com.musicplayer.action.PREVIOUS";
    public static final String ACTION_STOP = "com.musicplayer.action.STOP";
    public static final String ACTION_SEEK = "com.musicplayer.action.SEEK";
    
    // Broadcasts
    public static final String BROADCAST_PLAYBACK_STATE_CHANGED = "com.musicplayer.broadcast.PLAYBACK_STATE_CHANGED";
    public static final String BROADCAST_TRACK_CHANGED = "com.musicplayer.broadcast.TRACK_CHANGED";
    public static final String BROADCAST_PLAYLIST_CHANGED = "com.musicplayer.broadcast.PLAYLIST_CHANGED";
    
    // Binder for activity binding
    private final IBinder binder = new LocalBinder();
    
    // Core components
    private ExoPlayer player;
    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;
    private PlayerNotificationManager playerNotificationManager;
    private AudioFocusManager audioFocusManager;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    
    // Data
    private List<Track> currentPlaylist = new ArrayList<>();
    private int currentPosition = -1;
    private Track currentTrack;
    
    // State
    private int playbackState = STATE_IDLE;
    private boolean isShuffleMode = false;
    private int repeatMode = Player.REPEAT_MODE_OFF; // OFF, ONE, ALL
    private float playbackSpeed = 1.0f;
    private boolean isCrossfadeEnabled = false;
    private long crossfadeDuration = 2000; // 2 seconds
    
    // LiveData for observing state
    private final MutableLiveData<Integer> playbackStateLiveData = new MutableLiveData<>(STATE_IDLE);
    private final MutableLiveData<Track> currentTrackLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPositionLiveData = new MutableLiveData<>(-1);
    private final MutableLiveData<Long> currentPositionMsLiveData = new MutableLiveData<>(0L);
    
    // Disposables
    private final CompositeDisposable disposables = new CompositeDisposable();
    private Disposable progressDisposable;
    
    // Repositories
    private TrackRepository trackRepository;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Service created");
        
        initializeComponents();
        setupPhoneStateListener();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: " + intent.getAction());
        
        if (intent != null && intent.getAction() != null) {
            handleAction(intent);
        }
        
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Service destroyed");
        
        // Clean up resources
        if (progressDisposable != null && !progressDisposable.isDisposed()) {
            progressDisposable.dispose();
        }
        disposables.clear();
        
        if (player != null) {
            player.release();
        }
        
        if (mediaSession != null) {
            mediaSession.release();
        }
        
        if (telephonyManager != null && phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        
        stopForeground(true);
        stopSelf();
    }
    
    /**
     * Local binder for activity binding.
     */
    public class LocalBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }
    
    // Public API methods
    
    /**
     * Plays a single track.
     */
    public void playTrack(Track track) {
        if (track == null) return;
        
        List<Track> playlist = new ArrayList<>();
        playlist.add(track);
        playPlaylist(playlist, 0);
    }
    
    /**
     * Plays a playlist starting from the specified position.
     */
    public void playPlaylist(List<Track> playlist, int startPosition) {
        if (playlist == null || playlist.isEmpty()) return;
        
        this.currentPlaylist = new ArrayList<>(playlist);
        this.currentPosition = Math.max(0, Math.min(startPosition, playlist.size() - 1));
        
        prepareAndPlay();
    }
    
    /**
     * Plays or pauses current playback.
     */
    public void togglePlayback() {
        if (player == null) return;
        
        if (player.isPlaying()) {
            pause();
        } else {
            play();
        }
    }
    
    /**
     * Starts or resumes playback.
     */
    public void play() {
        if (player == null || currentTrack == null) return;
        
        if (audioFocusManager.requestAudioFocus()) {
            player.setPlayWhenReady(true);
            updatePlaybackState(STATE_PLAYING);
        }
    }
    
    /**
     * Pauses playback.
     */
    public void pause() {
        if (player == null) return;
        
        player.setPlayWhenReady(false);
        updatePlaybackState(STATE_PAUSED);
        audioFocusManager.abandonAudioFocus();
    }
    
    /**
     * Stops playback.
     */
    public void stop() {
        if (player == null) return;
        
        player.stop();
        updatePlaybackState(STATE_STOPPED);
        audioFocusManager.abandonAudioFocus();
    }
    
    /**
     * Skips to the next track.
     */
    public void next() {
        if (currentPlaylist.isEmpty()) return;
        
        if (isShuffleMode) {
            currentPosition = (int) (Math.random() * currentPlaylist.size());
        } else {
            currentPosition = (currentPosition + 1) % currentPlaylist.size();
        }
        
        prepareAndPlay();
    }
    
    /**
     * Skips to the previous track.
     */
    public void previous() {
        if (currentPlaylist.isEmpty()) return;
        
        if (player.getCurrentPosition() > 3000) { // 3 seconds
            seekTo(0);
        } else {
            if (isShuffleMode) {
                currentPosition = (int) (Math.random() * currentPlaylist.size());
            } else {
                currentPosition = (currentPosition - 1 + currentPlaylist.size()) % currentPlaylist.size();
            }
            prepareAndPlay();
        }
    }
    
    /**
     * Seeks to a specific position in the current track.
     */
    public void seekTo(long positionMs) {
        if (player != null) {
            player.seekTo(positionMs);
        }
    }
    
    /**
     * Sets playback speed.
     */
    public void setPlaybackSpeed(float speed) {
        this.playbackSpeed = speed;
        if (player != null) {
            PlaybackParameters parameters = new PlaybackParameters(speed);
            player.setPlaybackParameters(parameters);
        }
    }
    
    /**
     * Toggles shuffle mode.
     */
    public void toggleShuffle() {
        isShuffleMode = !isShuffleMode;
        if (player != null) {
            player.setShuffleModeEnabled(isShuffleMode);
        }
        broadcastPlaylistChanged();
    }
    
    /**
     * Cycles through repeat modes.
     */
    public void cycleRepeatMode() {
        repeatMode = (repeatMode + 1) % 3;
        if (player != null) {
            player.setRepeatMode(repeatMode);
        }
        broadcastPlaylistChanged();
    }
    
    // Getters for LiveData
    
    public LiveData<Integer> getPlaybackState() {
        return playbackStateLiveData;
    }
    
    public LiveData<Track> getCurrentTrack() {
        return currentTrackLiveData;
    }
    
    public LiveData<Integer> getCurrentPosition() {
        return currentPositionLiveData;
    }
    
    public LiveData<Long> getCurrentPositionMs() {
        return currentPositionMsLiveData;
    }
    
    // State getters
    
    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }
    
    public boolean isShuffleMode() {
        return isShuffleMode;
    }
    
    public int getRepeatMode() {
        return repeatMode;
    }
    
    public float getPlaybackSpeed() {
        return playbackSpeed;
    }
    
    public long getDuration() {
        return player != null ? player.getDuration() : 0;
    }
    
    public long getCurrentPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }
    
    public List<Track> getCurrentPlaylist() {
        return new ArrayList<>(currentPlaylist);
    }
    
    public int getCurrentPlaylistPosition() {
        return currentPosition;
    }
    
    // Private initialization methods
    
    private void initializeComponents() {
        trackRepository = TrackRepository.getInstance(this);
        
        // Initialize ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        
        // Setup audio attributes
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();
        player.setAudioAttributes(audioAttributes, true);
        
        // Setup player listeners
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                handlePlaybackStateChange(state);
            }
            
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    startProgressUpdates();
                } else {
                    stopProgressUpdates();
                }
            }
            
            @Override
            public void onMediaItemTransition(MediaItem mediaItem, int reason) {
                handleTrackChange();
            }
            
            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                playbackSpeed = playbackParameters.speed;
            }
        });
        
        // Initialize audio focus manager
        audioFocusManager = new AudioFocusManager(this);
        
        // Initialize media session
        setupMediaSession();
        
        // Initialize notification manager
        setupNotificationManager();
    }
    
    private void setupMediaSession() {
        mediaSession = new MediaSessionCompat(this, TAG);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                play();
            }
            
            @Override
            public void onPause() {
                pause();
            }
            
            @Override
            public void onSkipToNext() {
                next();
            }
            
            @Override
            public void onSkipToPrevious() {
                previous();
            }
            
            @Override
            public void onStop() {
                stop();
            }
            
            @Override
            public void onSeekTo(long pos) {
                seekTo(pos);
            }
        });
        
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                             MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        
        mediaSession.setActive(true);
    }
    
    private void setupNotificationManager() {
        PlayerNotificationManager.Builder builder = new PlayerNotificationManager.Builder(
                this, NOTIFICATION_ID, CHANNEL_ID);
        
        builder.setMediaDescriptionAdapter(new PlayerNotificationManager.MediaDescriptionAdapter() {
            @Override
            public String getCurrentContentTitle(Player player) {
                return currentTrack != null ? currentTrack.getTitle() : "Unknown";
            }
            
            @Override
            public String getCurrentContentText(Player player) {
                return currentTrack != null ? currentTrack.getArtist() : "Unknown";
            }
            
            @Override
            public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                if (currentTrack != null && currentTrack.hasAlbumArt()) {
                    return BitmapFactory.decodeFile(currentTrack.getAlbumArtPath());
                }
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);
            }
            
            @Override
            public PendingIntent createCurrentContentIntent(Player player) {
                Intent intent = new Intent(MusicPlayerService.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                return PendingIntent.getActivity(MusicPlayerService.this, 0, intent, 
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            }
        });
        
        builder.setNotificationListener(new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                if (ongoing) {
                    startForeground(notificationId, notification);
                } else {
                    stopForeground(false);
                }
            }
            
            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                if (dismissedByUser) {
                    stop();
                    stopSelf();
                }
            }
        });
        
        playerNotificationManager = builder.build();
        playerNotificationManager.setPlayer(player);
        playerNotificationManager.setUseNextAction(true);
        playerNotificationManager.setUsePreviousAction(true);
        playerNotificationManager.setUseStopAction(true);
    }
    
    private void setupPhoneStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if (isPlaying()) {
                            pause();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Resume playback if it was playing before call
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Player",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Music player notifications");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText("Ready to play")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setSilent(true);
        
        return builder.build();
    }
    
    // Action handling
    
    private void handleAction(Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        
        switch (action) {
            case ACTION_PLAY:
                play();
                break;
            case ACTION_PAUSE:
                pause();
                break;
            case ACTION_TOGGLE:
                togglePlayback();
                break;
            case ACTION_NEXT:
                next();
                break;
            case ACTION_PREVIOUS:
                previous();
                break;
            case ACTION_STOP:
                stop();
                break;
            case ACTION_SEEK:
                long position = intent.getLongExtra("position", 0);
                seekTo(position);
                break;
        }
    }
    
    // Player event handlers
    
    private void handlePlaybackStateChange(int state) {
        switch (state) {
            case Player.STATE_READY:
                if (player.getPlayWhenReady()) {
                    updatePlaybackState(STATE_PLAYING);
                } else {
                    updatePlaybackState(STATE_PAUSED);
                }
                break;
            case Player.STATE_ENDED:
                handlePlaybackComplete();
                break;
            case Player.STATE_BUFFERING:
                // Show buffering state if needed
                break;
            case Player.STATE_IDLE:
                updatePlaybackState(STATE_IDLE);
                break;
        }
    }
    
    private void handleTrackChange() {
        if (currentPosition >= 0 && currentPosition < currentPlaylist.size()) {
            currentTrack = currentPlaylist.get(currentPosition);
            currentTrackLiveData.postValue(currentTrack);
            currentPositionLiveData.postValue(currentPosition);
            
            // Update media session metadata
            updateMediaSessionMetadata();
            
            // Broadcast track change
            broadcastTrackChanged();
            
            // Update play count
            if (currentTrack != null) {
                trackRepository.incrementPlayCount(currentTrack.getId()).subscribe();
            }
        }
    }
    
    private void handlePlaybackComplete() {
        if (repeatMode == Player.REPEAT_MODE_ONE) {
            // Repeat current track
            seekTo(0);
            play();
        } else if (repeatMode == Player.REPEAT_MODE_OFF && currentPosition == currentPlaylist.size() - 1) {
            // End of playlist, stop playback
            stop();
        } else {
            // Continue to next track or repeat playlist
            next();
        }
    }
    
    // State management
    
    private void updatePlaybackState(int state) {
        this.playbackState = state;
        playbackStateLiveData.postValue(state);
        
        // Update media session state
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        long actions = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE |
                      PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                      PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_SEEK_TO;
        stateBuilder.setActions(actions);
        
        switch (state) {
            case STATE_PLAYING:
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, getCurrentPosition(), playbackSpeed);
                break;
            case STATE_PAUSED:
                stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, getCurrentPosition(), playbackSpeed);
                break;
            case STATE_STOPPED:
                stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, getCurrentPosition(), playbackSpeed);
                break;
            default:
                stateBuilder.setState(PlaybackStateCompat.STATE_NONE, getCurrentPosition(), playbackSpeed);
        }
        
        if (mediaSession != null) {
            mediaSession.setPlaybackState(stateBuilder.build());
        }
        
        broadcastPlaybackStateChanged();
    }
    
    private void prepareAndPlay() {
        if (currentPlaylist.isEmpty() || currentPosition < 0 || currentPosition >= currentPlaylist.size()) {
            return;
        }
        
        currentTrack = currentPlaylist.get(currentPosition);
        
        // Create media source
        MediaItem mediaItem = createMediaItem(currentTrack);
        
        if (mediaItem != null) {
            player.setMediaItem(mediaItem);
            player.prepare();
            
            // Restore bookmark position
            if (currentTrack.getBookmark() > 0) {
                seekTo(currentTrack.getBookmark());
            }
            
            play();
        }
    }
    
    private MediaItem createMediaItem(Track track) {
        if (track == null) return null;
        
        MediaItem.Builder builder = new MediaItem.Builder();
        
        if (track.isLocal() && track.getFilePath() != null) {
            builder.setUri(Uri.fromFile(new File(track.getFilePath())));
        } else if (track.getStreamUrl() != null) {
            builder.setUri(Uri.parse(track.getStreamUrl()));
        } else {
            return null;
        }
        
        // Set metadata
        MediaItem.Metadata metadata = new MediaItem.Builder()
                .setTitle(track.getTitle())
                .setArtist(track.getArtist())
                .setAlbumTitle(track.getAlbum())
                .setTrackNumber(track.getTrackNumber())
                .setYear(track.getYear())
                .build().mediaMetadata;
        
        builder.setMediaMetadata(metadata);
        
        return builder.build();
    }
    
    private void updateMediaSessionMetadata() {
        if (currentTrack == null || mediaSession == null) return;
        
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTrack.getTitle());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentTrack.getArtist());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentTrack.getAlbum());
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currentTrack.getDuration());
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, currentTrack.getTrackNumber());
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_YEAR, currentTrack.getYear());
        
        if (currentTrack.hasAlbumArt()) {
            Bitmap albumArt = BitmapFactory.decodeFile(currentTrack.getAlbumArtPath());
            if (albumArt != null) {
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt);
            }
        }
        
        mediaSession.setMetadata(metadataBuilder.build());
    }
    
    // Progress updates
    
    private void startProgressUpdates() {
        if (progressDisposable != null && !progressDisposable.isDisposed()) {
            progressDisposable.dispose();
        }
        
        progressDisposable = io.reactivex.Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(tick -> {
                    if (player != null) {
                        currentPositionMsLiveData.postValue(player.getCurrentPosition());
                    }
                }, throwable -> Log.e(TAG, "Error in progress updates", throwable));
    }
    
    private void stopProgressUpdates() {
        if (progressDisposable != null && !progressDisposable.isDisposed()) {
            progressDisposable.dispose();
        }
    }
    
    // Broadcast methods
    
    private void broadcastPlaybackStateChanged() {
        Intent intent = new Intent(BROADCAST_PLAYBACK_STATE_CHANGED);
        intent.putExtra("state", playbackState);
        intent.putExtra("isPlaying", isPlaying());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    private void broadcastTrackChanged() {
        Intent intent = new Intent(BROADCAST_TRACK_CHANGED);
        if (currentTrack != null) {
            intent.putExtra("track", currentTrack.getId());
        }
        intent.putExtra("position", currentPosition);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    private void broadcastPlaylistChanged() {
        Intent intent = new Intent(BROADCAST_PLAYLIST_CHANGED);
        intent.putExtra("shuffleMode", isShuffleMode);
        intent.putExtra("repeatMode", repeatMode);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}