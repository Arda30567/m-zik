package com.musicplayer.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Transaction;

import com.musicplayer.data.local.entities.Album;
import com.musicplayer.data.local.entities.Track;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Data Access Object for Album entity.
 * Provides database operations for music albums.
 */
@Dao
public interface AlbumDao {
    
    // Insert operations
    @Insert
    long insert(Album album);
    
    @Insert
    List<Long> insertAll(List<Album> albums);
    
    // Update operations
    @Update
    int update(Album album);
    
    @Update
    int updateAll(List<Album> albums);
    
    // Delete operations
    @Delete
    int delete(Album album);
    
    @Delete
    int deleteAll(List<Album> albums);
    
    @Query("DELETE FROM albums WHERE id = :albumId")
    int deleteById(long albumId);
    
    @Query("DELETE FROM albums")
    int deleteAll();
    
    // Query operations
    @Query("SELECT * FROM albums WHERE id = :albumId")
    Album getById(long albumId);
    
    @Query("SELECT * FROM albums WHERE id = :albumId")
    Single<Album> getByIdSingle(long albumId);
    
    @Query("SELECT * FROM albums ORDER BY title ASC")
    List<Album> getAll();
    
    @Query("SELECT * FROM albums ORDER BY title ASC")
    Flowable<List<Album>> getAllFlowable();
    
    @Query("SELECT * FROM albums ORDER BY title ASC")
    Single<List<Album>> getAllSingle();
    
    // Search operations
    @Query("SELECT * FROM albums WHERE title LIKE :query OR artist LIKE :query ORDER BY title ASC")
    List<Album> search(String query);
    
    @Query("SELECT * FROM albums WHERE title LIKE :query OR artist LIKE :query ORDER BY title ASC")
    Flowable<List<Album>> searchFlowable(String query);
    
    @Query("SELECT * FROM albums WHERE title LIKE :query ORDER BY title ASC")
    List<Album> searchByTitle(String query);
    
    @Query("SELECT * FROM albums WHERE artist LIKE :query ORDER BY artist ASC, title ASC")
    List<Album> searchByArtist(String query);
    
    // Filter operations
    @Query("SELECT * FROM albums WHERE artistId = :artistId ORDER BY year DESC, title ASC")
    List<Album> getByArtist(long artistId);
    
    @Query("SELECT * FROM albums WHERE genre = :genre ORDER BY title ASC")
    List<Album> getByGenre(String genre);
    
    @Query("SELECT * FROM albums WHERE year = :year ORDER BY title ASC")
    List<Album> getByYear(int year);
    
    @Query("SELECT * FROM albums WHERE year >= :startYear AND year <= :endYear ORDER BY year DESC, title ASC")
    List<Album> getByYearRange(int startYear, int endYear);
    
    // Get albums with tracks
    @Transaction
    @Query("SELECT * FROM albums WHERE id IN (SELECT DISTINCT albumId FROM tracks WHERE albumId > 0)")
    List<Album> getAlbumsWithTracks();
    
    // Count queries
    @Query("SELECT COUNT(*) FROM albums")
    int getCount();
    
    @Query("SELECT COUNT(*) FROM albums WHERE artistId = :artistId")
    int getCountByArtist(long artistId);
    
    @Query("SELECT COUNT(*) FROM albums WHERE genre = :genre")
    int getCountByGenre(String genre);
    
    @Query("SELECT COUNT(*) FROM albums WHERE year = :year")
    int getCountByYear(int year);
    
    // Update operations
    @Query("UPDATE albums SET trackCount = :trackCount, duration = :duration WHERE id = :albumId")
    int updateStats(long albumId, int trackCount, long duration);
    
    @Query("UPDATE albums SET artPath = :artPath WHERE id = :albumId")
    int setArtPath(long albumId, String artPath);
    
    @Query("UPDATE albums SET description = :description WHERE id = :albumId")
    int setDescription(long albumId, String description);
    
    // Get album with tracks (custom query with JOIN)
    @Query("SELECT albums.*, COUNT(tracks.id) as trackCount, SUM(tracks.duration) as totalDuration " +
           "FROM albums LEFT JOIN tracks ON albums.id = tracks.albumId " +
           "GROUP BY albums.id HAVING trackCount > 0 ORDER BY albums.title ASC")
    List<AlbumWithStats> getAlbumsWithStats();
    
    // Get recent albums
    @Query("SELECT DISTINCT albums.* FROM albums " +
           "JOIN tracks ON albums.id = tracks.albumId " +
           "WHERE tracks.dateAdded IS NOT NULL " +
           "ORDER BY tracks.dateAdded DESC LIMIT :limit")
    List<Album> getRecentAlbums(int limit);
    
    // Get most played albums
    @Query("SELECT albums.*, SUM(tracks.playCount) as totalPlayCount FROM albums " +
           "JOIN tracks ON albums.id = tracks.albumId " +
           "GROUP BY albums.id ORDER BY totalPlayCount DESC LIMIT :limit")
    List<Album> getMostPlayedAlbums(int limit);
    
    // Statistics
    @Query("SELECT COUNT(DISTINCT genre) FROM albums WHERE genre IS NOT NULL AND genre != ''")
    int getGenreCount();
    
    @Query("SELECT genre, COUNT(*) as count FROM albums WHERE genre IS NOT NULL AND genre != '' " +
           "GROUP BY genre ORDER BY count DESC")
    List<GenreCount> getGenreDistribution();
    
    @Query("SELECT MIN(year) FROM albums WHERE year > 0")
    int getOldestYear();
    
    @Query("SELECT MAX(year) FROM albums WHERE year > 0")
    int getNewestYear();
    
    @Query("SELECT AVG(year) FROM albums WHERE year > 0")
    double getAverageYear();
    
    // Data cleanup
    @Query("DELETE FROM albums WHERE id NOT IN (SELECT DISTINCT albumId FROM tracks WHERE albumId > 0)")
    int deleteEmptyAlbums();
    
    // Utility classes
    class AlbumWithStats {
        public long id;
        public String title;
        public String artist;
        public long artistId;
        public int year;
        public String genre;
        public String artPath;
        public String description;
        public boolean isCompilation;
        public String albumArtist;
        public String recordLabel;
        public Date dateAdded;
        public Date dateModified;
        public int trackCount;
        public long totalDuration;
    }
    
    class GenreCount {
        public String genre;
        public int count;
    }
}