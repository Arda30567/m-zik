package com.musicplayer.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Transaction;

import com.musicplayer.data.local.entities.Artist;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Data Access Object for Artist entity.
 * Provides database operations for music artists.
 */
@Dao
public interface ArtistDao {
    
    // Insert operations
    @Insert
    long insert(Artist artist);
    
    @Insert
    List<Long> insertAll(List<Artist> artists);
    
    // Update operations
    @Update
    int update(Artist artist);
    
    @Update
    int updateAll(List<Artist> artists);
    
    // Delete operations
    @Delete
    int delete(Artist artist);
    
    @Delete
    int deleteAll(List<Artist> artists);
    
    @Query("DELETE FROM artists WHERE id = :artistId")
    int deleteById(long artistId);
    
    @Query("DELETE FROM artists")
    int deleteAll();
    
    // Query operations
    @Query("SELECT * FROM artists WHERE id = :artistId")
    Artist getById(long artistId);
    
    @Query("SELECT * FROM artists WHERE id = :artistId")
    Single<Artist> getByIdSingle(long artistId);
    
    @Query("SELECT * FROM artists WHERE name = :name")
    Artist getByName(String name);
    
    @Query("SELECT * FROM artists ORDER BY name ASC")
    List<Artist> getAll();
    
    @Query("SELECT * FROM artists ORDER BY name ASC")
    Flowable<List<Artist>> getAllFlowable();
    
    @Query("SELECT * FROM artists ORDER BY name ASC")
    Single<List<Artist>> getAllSingle();
    
    // Search operations
    @Query("SELECT * FROM artists WHERE name LIKE :query ORDER BY name ASC")
    List<Artist> search(String query);
    
    @Query("SELECT * FROM artists WHERE name LIKE :query ORDER BY name ASC")
    Flowable<List<Artist>> searchFlowable(String query);
    
    @Query("SELECT * FROM artists WHERE genre = :genre ORDER BY name ASC")
    List<Artist> getByGenre(String genre);
    
    // Get artists with tracks
    @Query("SELECT DISTINCT artists.* FROM artists " +
           "JOIN tracks ON artists.id = tracks.artistId " +
           "ORDER BY artists.name ASC")
    List<Artist> getArtistsWithTracks();
    
    @Transaction
    @Query("SELECT artists.*, COUNT(DISTINCT albums.id) as albumCount, " +
           "COUNT(DISTINCT tracks.id) as trackCount, " +
           "SUM(tracks.duration) as totalDuration, " +
           "SUM(tracks.playCount) as totalPlayCount, " +
           "MAX(tracks.lastPlayed) as lastPlayed " +
           "FROM artists " +
           "LEFT JOIN albums ON artists.id = albums.artistId " +
           "LEFT JOIN tracks ON artists.id = tracks.artistId " +
           "GROUP BY artists.id ORDER BY artists.name ASC")
    List<ArtistWithStats> getArtistsWithStats();
    
    // Count queries
    @Query("SELECT COUNT(*) FROM artists")
    int getCount();
    
    @Query("SELECT COUNT(*) FROM artists WHERE genre = :genre")
    int getCountByGenre(String genre);
    
    @Query("SELECT COUNT(DISTINCT artists.id) FROM artists " +
           "JOIN tracks ON artists.id = tracks.artistId")
    int getArtistCountWithTracks();
    
    // Update operations
    @Query("UPDATE artists SET trackCount = :trackCount, albumCount = :albumCount, " +
           "duration = :duration WHERE id = :artistId")
    int updateStats(long artistId, int trackCount, int albumCount, long duration);
    
    @Query("UPDATE artists SET artPath = :artPath WHERE id = :artistId")
    int setArtPath(long artistId, String artPath);
    
    @Query("UPDATE artists SET bio = :bio WHERE id = :artistId")
    int setBio(long artistId, String bio);
    
    @Query("UPDATE artists SET favorite = :favorite WHERE id = :artistId")
    int setFavorite(long artistId, boolean favorite);
    
    // Get favorite artists
    @Query("SELECT * FROM artists WHERE favorite = 1 ORDER BY name ASC")
    List<Artist> getFavorites();
    
    @Query("SELECT * FROM artists WHERE favorite = 1 ORDER BY name ASC")
    Flowable<List<Artist>> getFavoritesFlowable();
    
    // Get most played artists
    @Query("SELECT artists.*, SUM(tracks.playCount) as totalPlayCount FROM artists " +
           "JOIN tracks ON artists.id = tracks.artistId " +
           "GROUP BY artists.id ORDER BY totalPlayCount DESC LIMIT :limit")
    List<Artist> getMostPlayedArtists(int limit);
    
    // Get recently played artists
    @Query("SELECT DISTINCT artists.* FROM artists " +
           "JOIN tracks ON artists.id = tracks.artistId " +
           "WHERE tracks.lastPlayed IS NOT NULL " +
           "ORDER BY tracks.lastPlayed DESC LIMIT :limit")
    List<Artist> getRecentlyPlayedArtists(int limit);
    
    // Statistics
    @Query("SELECT genre, COUNT(*) as count FROM artists WHERE genre IS NOT NULL AND genre != '' " +
           "GROUP BY genre ORDER BY count DESC")
    List<GenreCount> getGenreDistribution();
    
    @Query("SELECT COUNT(DISTINCT genre) FROM artists WHERE genre IS NOT NULL AND genre != ''")
    int getGenreCount();
    
    @Query("SELECT SUM(trackCount) FROM artists")
    int getTotalTrackCount();
    
    @Query("SELECT SUM(albumCount) FROM artists")
    int getTotalAlbumCount();
    
    @Query("SELECT SUM(duration) FROM artists")
    long getTotalDuration();
    
    // Data cleanup
    @Query("DELETE FROM artists WHERE id NOT IN (SELECT DISTINCT artistId FROM tracks WHERE artistId > 0)")
    int deleteEmptyArtists();
    
    // Batch operations
    @Query("SELECT id FROM artists WHERE name IN (:names)")
    List<Long> getIdsByNames(List<String> names);
    
    @Query("UPDATE artists SET favorite = 1 WHERE id IN (:artistIds)")
    int setFavorites(List<Long> artistIds);
    
    @Query("UPDATE artists SET favorite = 0 WHERE id IN (:artistIds)")
    int unsetFavorites(List<Long> artistIds);
    
    // Utility classes
    class ArtistWithStats {
        public long id;
        public String name;
        public String bio;
        public String artPath;
        public String origin;
        public String genre;
        public String website;
        public String birthDate;
        public String deathDate;
        public Date dateAdded;
        public Date dateModified;
        public boolean favorite;
        public int playCount;
        public Date lastPlayed;
        public int albumCount;
        public int trackCount;
        public long totalDuration;
        public int totalPlayCount;
        public Date artistLastPlayed;
    }
    
    class GenreCount {
        public String genre;
        public int count;
    }
}