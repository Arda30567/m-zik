package com.musicplayer.repository;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.musicplayer.data.local.AppDatabase;
import com.musicplayer.data.local.entities.Track;
import com.musicplayer.data.local.entities.Album;
import com.musicplayer.data.local.entities.Artist;
import com.musicplayer.utils.MetadataExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * Repository class for managing track data.
 * Provides abstraction between data sources (database, file system) and ViewModels.
 */
public class TrackRepository {
    
    private static final String TAG = "TrackRepository";
    private static TrackRepository INSTANCE;
    
    private final AppDatabase database;
    private final ExecutorService executor;
    private final MetadataExtractor metadataExtractor;
    
    // LiveData for observing changes
    private final MutableLiveData<List<Track>> allTracks = new MutableLiveData<>();
    private final MutableLiveData<List<Track>> favorites = new MutableLiveData<>();
    private final MutableLiveData<List<Track>> recentTracks = new MutableLiveData<>();
    
    private TrackRepository(Context context) {
        this.database = AppDatabase.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.metadataExtractor = new MetadataExtractor(context);
    }
    
    /**
     * Gets the singleton instance of TrackRepository.
     */
    public static TrackRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TrackRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TrackRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Inserts a single track into the database.
     */
    public Completable insert(Track track) {
        return Completable.fromAction(() -> database.trackDao().insert(track))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Inserts multiple tracks into the database.
     */
    public Completable insertAll(List<Track> tracks) {
        return Completable.fromAction(() -> database.trackDao().insertAll(tracks))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Updates a track in the database.
     */
    public Completable update(Track track) {
        return Completable.fromAction(() -> database.trackDao().update(track))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Deletes a track from the database.
     */
    public Completable delete(Track track) {
        return Completable.fromAction(() -> database.trackDao().delete(track))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets a track by its ID.
     */
    public Single<Track> getById(long trackId) {
        return database.trackDao().getByIdSingle(trackId)
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets all tracks as a reactive stream.
     */
    public Flowable<List<Track>> getAll() {
        return database.trackDao().getAllFlowable()
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets all tracks as LiveData.
     */
    public LiveData<List<Track>> getAllAsLiveData() {
        loadAllTracks();
        return allTracks;
    }
    
    /**
     * Searches tracks by title, artist, or album.
     */
    public Flowable<List<Track>> search(String query) {
        String searchQuery = "%" + query + "%";
        return database.trackDao().searchFlowable(searchQuery)
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets favorite tracks.
     */
    public Flowable<List<Track>> getFavorites() {
        return database.trackDao().getFavoritesFlowable()
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets favorite tracks as LiveData.
     */
    public LiveData<List<Track>> getFavoritesAsLiveData() {
        loadFavorites();
        return favorites;
    }
    
    /**
     * Gets recent tracks (last added).
     */
    public Single<List<Track>> getRecentTracks(int limit) {
        return Single.fromCallable(() -> database.trackDao().getRecent(limit))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets most played tracks.
     */
    public Single<List<Track>> getMostPlayedTracks(int limit) {
        return Single.fromCallable(() -> database.trackDao().getMostPlayed(limit))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets tracks by genre.
     */
    public Single<List<Track>> getByGenre(String genre) {
        return Single.fromCallable(() -> database.trackDao().getByGenre(genre))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets tracks by artist.
     */
    public Single<List<Track>> getByArtist(long artistId) {
        return Single.fromCallable(() -> database.trackDao().getByArtist(artistId))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets tracks by album.
     */
    public Single<List<Track>> getByAlbum(long albumId) {
        return Single.fromCallable(() -> database.trackDao().getByAlbum(albumId))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Toggles favorite status of a track.
     */
    public Completable toggleFavorite(long trackId) {
        return Completable.fromAction(() -> {
            Track track = database.trackDao().getById(trackId);
            if (track != null) {
                track.setFavorite(!track.isFavorite());
                database.trackDao().update(track);
            }
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Sets track rating.
     */
    public Completable setRating(long trackId, int rating) {
        return Completable.fromAction(() -> database.trackDao().setRating(trackId, rating))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Sets track bookmark (playback position).
     */
    public Completable setBookmark(long trackId, long bookmark) {
        return Completable.fromAction(() -> database.trackDao().setBookmark(trackId, bookmark))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Increments play count for a track.
     */
    public Completable incrementPlayCount(long trackId) {
        return Completable.fromAction(() -> 
                database.trackDao().incrementPlayCount(trackId, new Date()))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Updates track metadata.
     */
    public Completable updateMetadata(long trackId, String title, String artist, String album, 
                                     String genre, int year, int trackNumber, 
                                     String lyrics, String composer) {
        return Completable.fromAction(() -> 
                database.trackDao().updateMetadata(trackId, title, artist, album, genre, 
                        year, trackNumber, lyrics, composer))
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Scans media store for music files and adds them to the database.
     */
    public Completable scanMediaStore() {
        return Completable.fromAction(() -> {
            List<Track> tracks = scanMediaStoreForTracks();
            if (!tracks.isEmpty()) {
                database.trackDao().insertAll(tracks);
            }
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Imports tracks from file paths.
     */
    public Completable importTracks(List<String> filePaths) {
        return Completable.fromAction(() -> {
            List<Track> tracks = new ArrayList<>();
            for (String filePath : filePaths) {
                Track track = createTrackFromFile(filePath);
                if (track != null) {
                    tracks.add(track);
                }
            }
            if (!tracks.isEmpty()) {
                database.trackDao().insertAll(tracks);
            }
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets total track count.
     */
    public Single<Integer> getCount() {
        return Single.fromCallable(() -> database.trackDao().getCount())
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets total duration of all tracks.
     */
    public Single<Long> getTotalDuration() {
        return Single.fromCallable(() -> database.trackDao().getTotalDuration())
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets total play count.
     */
    public Single<Integer> getTotalPlayCount() {
        return Single.fromCallable(() -> database.trackDao().getTotalPlayCount())
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Cleans up invalid tracks (missing files).
     */
    public Completable cleanupInvalidTracks() {
        return Completable.fromAction(() -> {
            List<Track> tracks = database.trackDao().getAll();
            List<Track> invalidTracks = new ArrayList<>();
            
            for (Track track : tracks) {
                if (track.isLocal() && track.getFilePath() != null) {
                    File file = new File(track.getFilePath());
                    if (!file.exists() || !file.canRead()) {
                        invalidTracks.add(track);
                    }
                }
            }
            
            if (!invalidTracks.isEmpty()) {
                database.trackDao().deleteAll(invalidTracks);
            }
        }).subscribeOn(Schedulers.io());
    }
    
    // Private helper methods
    
    private void loadAllTracks() {
        executor.execute(() -> {
            try {
                List<Track> tracks = database.trackDao().getAll();
                allTracks.postValue(tracks);
            } catch (Exception e) {
                Log.e(TAG, "Error loading tracks", e);
                allTracks.postValue(new ArrayList<>());
            }
        });
    }
    
    private void loadFavorites() {
        executor.execute(() -> {
            try {
                List<Track> favoriteTracks = database.trackDao().getFavorites();
                favorites.postValue(favoriteTracks);
            } catch (Exception e) {
                Log.e(TAG, "Error loading favorites", e);
                favorites.postValue(new ArrayList<>());
            }
        });
    }
    
    private List<Track> scanMediaStoreForTracks() {
        List<Track> tracks = new ArrayList<>();
        // This would typically query MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        // For now, return empty list - implement based on your needs
        return tracks;
    }
    
    private Track createTrackFromFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.canRead()) {
                return null;
            }
            
            // Extract metadata using MetadataExtractor
            Track track = metadataExtractor.extractMetadata(filePath);
            if (track != null) {
                // Check if track already exists
                Track existingTrack = database.trackDao().getByFilePath(filePath);
                if (existingTrack != null) {
                    track.setId(existingTrack.getId());
                }
                return track;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating track from file: " + filePath, e);
        }
        return null;
    }
    
    /**
     * Destroys the repository instance (for testing).
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }
}