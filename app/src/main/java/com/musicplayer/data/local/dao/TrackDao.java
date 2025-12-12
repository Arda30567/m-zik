package com.musicplayer.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Transaction;

import com.musicplayer.data.local.entities.Track;
import com.musicplayer.data.local.entities.Album;
import com.musicplayer.data.local.entities.Artist;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Data Access Object for Track entity.
 * Provides database operations for music tracks.
 */
@Dao
public interface TrackDao {
    
    // Insert operations
    @Insert
    long insert(Track track);
    
    @Insert
    List<Long> insertAll(List<Track> tracks);
    
    // Update operations
    @Update
    int update(Track track);
    
    @Update
    int updateAll(List<Track> tracks);
    
    // Delete operations
    @Delete
    int delete(Track track);
    
    @Delete
    int deleteAll(List<Track> tracks);
    
    @Query("DELETE FROM tracks WHERE id = :trackId")
    int deleteById(long trackId);
    
    @Query("DELETE FROM tracks")
    int deleteAll();
    
    // Query operations
    @Query("SELECT * FROM tracks WHERE id = :trackId")
    Track getById(long trackId);
    
    @Query("SELECT * FROM tracks WHERE id = :trackId")
    Single<Track> getByIdSingle(long trackId);
    
    @Query("SELECT * FROM tracks WHERE filePath = :filePath")
    Track getByFilePath(String filePath);
    
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    List<Track> getAll();
    
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    Flowable<List<Track>> getAllFlowable();
    
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    Single<List<Track>> getAllSingle();
    
    // Search operations
    @Query("SELECT * FROM tracks WHERE title LIKE :query OR artist LIKE :query OR album LIKE :query ORDER BY title ASC")
    List<Track> search(String query);
    
    @Query("SELECT * FROM tracks WHERE title LIKE :query OR artist LIKE :query OR album LIKE :query ORDER BY title ASC")
    Flowable<List<Track>> searchFlowable(String query);
    
    @Query("SELECT * FROM tracks WHERE title LIKE :query ORDER BY title ASC")
    List<Track> searchByTitle(String query);
    
    @Query("SELECT * FROM tracks WHERE artist LIKE :query ORDER BY artist ASC, title ASC")
    List<Track> searchByArtist(String query);
    
    @Query("SELECT * FROM tracks WHERE album LIKE :query ORDER BY album ASC, trackNumber ASC")
    List<Track> searchByAlbum(String query);
    
    // Filter operations
    @Query("SELECT * FROM tracks WHERE genre = :genre ORDER BY title ASC")
    List<Track> getByGenre(String genre);
    
    @Query("SELECT * FROM tracks WHERE year = :year ORDER BY title ASC")
    List<Track> getByYear(int year);
    
    @Query("SELECT * FROM tracks WHERE favorite = 1 ORDER BY title ASC")
    List<Track> getFavorites();
    
    @Query("SELECT * FROM tracks WHERE favorite = 1 ORDER BY title ASC")
    Flowable<List<Track>> getFavoritesFlowable();
    
    // Smart playlist queries
    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC LIMIT :limit")
    List<Track> getRecent(int limit);
    
    @Query("SELECT * FROM tracks ORDER BY playCount DESC LIMIT :limit")
    List<Track> getMostPlayed(int limit);
    
    @Query("SELECT * FROM tracks WHERE lastPlayed IS NOT NULL ORDER BY lastPlayed DESC LIMIT :limit")
    List<Track> getRecentlyPlayed(int limit);
    
    @Query("SELECT * FROM tracks WHERE tags LIKE :tag ORDER BY title ASC")
    List<Track> getByTag(String tag);
    
    // Album and artist related queries
    @Query("SELECT * FROM tracks WHERE albumId = :albumId ORDER BY trackNumber ASC, title ASC")
    List<Track> getByAlbum(long albumId);
    
    @Query("SELECT * FROM tracks WHERE artistId = :artistId ORDER BY album ASC, trackNumber ASC")
    List<Track> getByArtist(long artistId);
    
    // Count queries
    @Query("SELECT COUNT(*) FROM tracks")
    int getCount();
    
    @Query("SELECT COUNT(*) FROM tracks WHERE favorite = 1")
    int getFavoriteCount();
    
    @Query("SELECT COUNT(*) FROM tracks WHERE genre = :genre")
    int getCountByGenre(String genre);
    
    // Update operations for specific fields
    @Query("UPDATE tracks SET playCount = playCount + 1, lastPlayed = :lastPlayed WHERE id = :trackId")
    int incrementPlayCount(long trackId, Date lastPlayed);
    
    @Query("UPDATE tracks SET favorite = :favorite WHERE id = :trackId")
    int setFavorite(long trackId, boolean favorite);
    
    @Query("UPDATE tracks SET rating = :rating WHERE id = :trackId")
    int setRating(long trackId, int rating);
    
    @Query("UPDATE tracks SET bookmark = :bookmark WHERE id = :trackId")
    int setBookmark(long trackId, long bookmark);
    
    @Query("UPDATE tracks SET tags = :tags WHERE id = :trackId")
    int setTags(long trackId, String tags);
    
    @Query("UPDATE tracks SET downloadStatus = :status WHERE id = :trackId")
    int setDownloadStatus(long trackId, int status);
    
    @Query("UPDATE tracks SET albumArtPath = :artPath WHERE id = :trackId")
    int setAlbumArtPath(long trackId, String artPath);
    
    // Metadata update
    @Query("UPDATE tracks SET title = :title, artist = :artist, album = :album, genre = :genre, " +
           "year = :year, trackNumber = :trackNumber, lyrics = :lyrics, composer = :composer " +
           "WHERE id = :trackId")
    int updateMetadata(long trackId, String title, String artist, String album, String genre,
                       int year, int trackNumber, String lyrics, String composer);
    
    // Batch operations
    @Query("SELECT id FROM tracks WHERE filePath IN (:filePaths)")
    List<Long> getIdsByFilePaths(List<String> filePaths);
    
    @Query("UPDATE tracks SET favorite = 1 WHERE id IN (:trackIds)")
    int setFavorites(List<Long> trackIds);
    
    @Query("UPDATE tracks SET favorite = 0 WHERE id IN (:trackIds)")
    int unsetFavorites(List<Long> trackIds);
    
    // Statistics
    @Query("SELECT SUM(playCount) FROM tracks")
    int getTotalPlayCount();
    
    @Query("SELECT SUM(duration) FROM tracks")
    long getTotalDuration();
    
    @Query("SELECT AVG(rating) FROM tracks WHERE rating > 0")
    float getAverageRating();
    
    @Query("SELECT COUNT(DISTINCT genre) FROM tracks WHERE genre IS NOT NULL AND genre != ''")
    int getGenreCount();
    
    @Query("SELECT genre, COUNT(*) as count FROM tracks WHERE genre IS NOT NULL AND genre != '' " +
           "GROUP BY genre ORDER BY count DESC")
    List<GenreCount> getGenreDistribution();
    
    // Data cleanup
    @Query("DELETE FROM tracks WHERE filePath IS NULL OR filePath = ''")
    int deleteInvalidTracks();
    
    @Query("DELETE FROM tracks WHERE isLocal = 1 AND filePath NOT IN (:validPaths)")
    int deleteMissingTracks(List<String> validPaths);
    
    // Utility class for genre distribution
    class GenreCount {
        public String genre;
        public int count;
    }
}