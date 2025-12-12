package com.musicplayer.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.musicplayer.data.local.AppDatabase;
import com.musicplayer.data.local.entities.Playlist;
import com.musicplayer.data.local.entities.PlaylistItem;
import com.musicplayer.data.local.entities.Track;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * Repository class for managing playlist data.
 * Handles all playlist-related operations including smart playlists.
 */
public class PlaylistRepository {
    
    private static final String TAG = "PlaylistRepository";
    private static PlaylistRepository INSTANCE;
    
    private final AppDatabase database;
    private final TrackRepository trackRepository;
    
    // LiveData for observing changes
    private final MutableLiveData<List<Playlist>> allPlaylists = new MutableLiveData<>();
    private final MutableLiveData<List<Playlist>> userPlaylists = new MutableLiveData<>();
    private final MutableLiveData<List<Playlist>> smartPlaylists = new MutableLiveData<>();
    
    private PlaylistRepository(Context context) {
        this.database = AppDatabase.getInstance(context);
        this.trackRepository = TrackRepository.getInstance(context);
        initializeDefaultPlaylists();
    }
    
    /**
     * Gets the singleton instance of PlaylistRepository.
     */
    public static PlaylistRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (PlaylistRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PlaylistRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Creates a new playlist.
     */
    public Completable createPlaylist(String name, String description) {
        return Completable.fromAction(() -> {
            Playlist playlist = new Playlist(name);
            playlist.setDescription(description);
            database.playlistDao().insert(playlist);
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Inserts a playlist into the database.
     */
    public Completable insert(Playlist playlist) {
        return Completable.fromAction(() -> database.playlistDao().insert(playlist))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Updates a playlist.
     */
    public Completable update(Playlist playlist) {
        return Completable.fromAction(() -> database.playlistDao().update(playlist))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Deletes a playlist.
     */
    public Completable delete(Playlist playlist) {
        return Completable.fromAction(() -> database.playlistDao().delete(playlist))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets all playlists.
     */
    public Flowable<List<Playlist>> getAll() {
        return database.playlistDao().getAllFlowable()
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets all playlists as LiveData.
     */
    public LiveData<List<Playlist>> getAllAsLiveData() {
        loadAllPlaylists();
        return allPlaylists;
    }
    
    /**
     * Gets user-created playlists.
     */
    public Single<List<Playlist>> getUserPlaylists() {
        return Single.fromCallable(() -> database.playlistDao().getUserPlaylists())
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets user playlists as LiveData.
     */
    public LiveData<List<Playlist>> getUserPlaylistsAsLiveData() {
        loadUserPlaylists();
        return userPlaylists;
    }
    
    /**
     * Gets smart playlists.
     */
    public Single<List<Playlist>> getSmartPlaylists() {
        return Single.fromCallable(() -> database.playlistDao().getSmartPlaylists())
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets smart playlists as LiveData.
     */
    public LiveData<List<Playlist>> getSmartPlaylistsAsLiveData() {
        loadSmartPlaylists();
        return smartPlaylists;
    }
    
    /**
     * Gets a playlist by ID.
     */
    public Single<Playlist> getById(long playlistId) {
        return database.playlistDao().getByIdSingle(playlistId)
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets tracks in a playlist.
     */
    public Flowable<List<Track>> getTracks(long playlistId) {
        return database.playlistDao().getTracksFlowable(playlistId)
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Adds a track to a playlist.
     */
    public Completable addTrackToPlaylist(long playlistId, long trackId) {
        return Completable.fromAction(() -> 
                database.playlistDao().addTrackToPlaylist(playlistId, trackId))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Removes a track from a playlist.
     */
    public Completable removeTrackFromPlaylist(long playlistId, long trackId) {
        return Completable.fromAction(() -> 
                database.playlistDao().removeTrackFromPlaylist(playlistId, trackId))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Moves a track to a different position in the playlist.
     */
    public Completable moveTrack(long playlistId, int fromPosition, int toPosition) {
        return Completable.fromAction(() -> 
                database.playlistDao().moveTrack(playlistId, fromPosition, toPosition))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Creates a smart playlist.
     */
    public Completable createSmartPlaylist(String name, String smartType, String criteria) {
        return Completable.fromAction(() -> {
            Playlist playlist = new Playlist(name);
            playlist.setSmart(true);
            playlist.setSmartType(smartType);
            playlist.setSmartCriteria(criteria);
            database.playlistDao().insert(playlist);
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Refreshes a smart playlist based on its criteria.
     */
    public Completable refreshSmartPlaylist(long playlistId) {
        return Single.fromCallable(() -> database.playlistDao().getById(playlistId))
                .flatMapCompletable(playlist -> {
                    if (!playlist.isSmart()) {
                        return Completable.complete();
                    }
                    
                    return refreshSmartPlaylistItems(playlist);
                })
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Searches playlists by name.
     */
    public Single<List<Playlist>> search(String query) {
        return Single.fromCallable(() -> database.playlistDao().searchUserPlaylists("%" + query + "%"))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Imports a playlist from M3U file.
     */
    public Completable importFromM3U(String filePath, String playlistName) {
        return Completable.fromAction(() -> {
            // M3U import implementation would go here
            // This is a placeholder for the actual implementation
            Log.d(TAG, "Importing playlist from: " + filePath);
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Exports a playlist to M3U file.
     */
    public Completable exportToM3U(long playlistId, String filePath) {
        return Completable.fromAction(() -> {
            // M3U export implementation would go here
            // This is a placeholder for the actual implementation
            Log.d(TAG, "Exporting playlist to: " + filePath);
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets playlist statistics.
     */
    public Single<PlaylistStats> getPlaylistStats(long playlistId) {
        return Single.fromCallable(() -> {
            PlaylistStats stats = new PlaylistStats();
            stats.trackCount = database.playlistDao().getItemCount(playlistId);
            stats.duration = database.playlistDao().getPlaylistDuration(playlistId);
            stats.playCount = database.playlistDao().getPlaylistPlayCount(playlistId);
            stats.averageRating = database.playlistDao().getPlaylistAverageRating(playlistId);
            return stats;
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Checks if a track is in a playlist.
     */
    public Single<Boolean> isTrackInPlaylist(long playlistId, long trackId) {
        return Single.fromCallable(() -> {
            PlaylistItem item = database.playlistDao().getItem(playlistId, trackId);
            return item != null;
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets playlists containing a specific track.
     */
    public Single<List<Playlist>> getPlaylistsForTrack(long trackId) {
        return Single.fromCallable(() -> {
            List<Long> playlistIds = database.playlistDao().getPlaylistsForTrack(trackId);
            List<Playlist> playlists = new ArrayList<>();
            for (Long playlistId : playlistIds) {
                Playlist playlist = database.playlistDao().getById(playlistId);
                if (playlist != null) {
                    playlists.add(playlist);
                }
            }
            return playlists;
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Duplicates a playlist.
     */
    public Completable duplicatePlaylist(long playlistId, String newName) {
        return Single.fromCallable(() -> database.playlistDao().getById(playlistId))
                .flatMapCompletable(originalPlaylist -> {
                    Playlist newPlaylist = new Playlist(newName);
                    newPlaylist.setDescription(originalPlaylist.getDescription() + " (Copy)");
                    long newPlaylistId = database.playlistDao().insert(newPlaylist);
                    
                    // Copy all items
                    List<PlaylistItem> items = database.playlistDao().getItems(playlistId);
                    for (PlaylistItem item : items) {
                        PlaylistItem newItem = new PlaylistItem(newPlaylistId, item.getTrackId(), item.getPosition());
                        database.playlistDao().insertItem(newItem);
                    }
                    
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io());
    }
    
    // Private helper methods
    
    private void initializeDefaultPlaylists() {
        Completable.fromAction(() -> {
            // Check if default playlists exist
            Playlist favorites = database.playlistDao().getByName("Favorites");
            if (favorites == null) {
                createDefaultSmartPlaylist("Favorites", Playlist.SMART_TYPE_FAVORITES, "");
            }
            
            Playlist recent = database.playlistDao().getByName("Recently Added");
            if (recent == null) {
                createDefaultSmartPlaylist("Recently Added", Playlist.SMART_TYPE_RECENT, "");
            }
            
            Playlist mostPlayed = database.playlistDao().getByName("Most Played");
            if (mostPlayed == null) {
                createDefaultSmartPlaylist("Most Played", Playlist.SMART_TYPE_MOST_PLAYED, "");
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }
    
    private void createDefaultSmartPlaylist(String name, String smartType, String criteria) {
        Playlist playlist = new Playlist(name);
        playlist.setSmart(true);
        playlist.setSmartType(smartType);
        playlist.setSmartCriteria(criteria);
        playlist.setDescription("Auto-generated smart playlist");
        database.playlistDao().insert(playlist);
    }
    
    private Completable refreshSmartPlaylistItems(Playlist playlist) {
        return Completable.fromAction(() -> {
            // Clear existing items
            database.playlistDao().deleteAllItems(playlist.getId());
            
            List<Track> tracks = new ArrayList<>();
            
            switch (playlist.getSmartType()) {
                case Playlist.SMART_TYPE_FAVORITES:
                    tracks = database.trackDao().getFavorites();
                    break;
                case Playlist.SMART_TYPE_RECENT:
                    tracks = database.trackDao().getRecent(100);
                    break;
                case Playlist.SMART_TYPE_MOST_PLAYED:
                    tracks = database.trackDao().getMostPlayed(100);
                    break;
                case Playlist.SMART_TYPE_CUSTOM:
                    // Handle custom smart playlist logic here
                    tracks = database.trackDao().getAll();
                    break;
            }
            
            // Add tracks to playlist
            int position = 0;
            for (Track track : tracks) {
                PlaylistItem item = new PlaylistItem(playlist.getId(), track.getId(), position++);
                database.playlistDao().insertItem(item);
            }
            
            // Update playlist stats
            database.playlistDao().updatePlaylistStats(playlist.getId());
        }).subscribeOn(Schedulers.io());
    }
    
    private void loadAllPlaylists() {
        database.playlistDao().getAllFlowable()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        playlists -> allPlaylists.postValue(playlists),
                        throwable -> {
                            Log.e(TAG, "Error loading playlists", throwable);
                            allPlaylists.postValue(new ArrayList<>());
                        }
                );
    }
    
    private void loadUserPlaylists() {
        Single.fromCallable(() -> database.playlistDao().getUserPlaylists())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        playlists -> userPlaylists.postValue(playlists),
                        throwable -> {
                            Log.e(TAG, "Error loading user playlists", throwable);
                            userPlaylists.postValue(new ArrayList<>());
                        }
                );
    }
    
    private void loadSmartPlaylists() {
        Single.fromCallable(() -> database.playlistDao().getSmartPlaylists())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        playlists -> smartPlaylists.postValue(playlists),
                        throwable -> {
                            Log.e(TAG, "Error loading smart playlists", throwable);
                            smartPlaylists.postValue(new ArrayList<>());
                        }
                );
    }
    
    /**
     * Statistics class for playlist information.
     */
    public static class PlaylistStats {
        public int trackCount;
        public long duration;
        public int playCount;
        public float averageRating;
    }
    
    /**
     * Destroys the repository instance (for testing).
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }
}