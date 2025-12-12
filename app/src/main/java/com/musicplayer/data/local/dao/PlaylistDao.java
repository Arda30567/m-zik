package com.musicplayer.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Transaction;

import com.musicplayer.data.local.entities.Playlist;
import com.musicplayer.data.local.entities.PlaylistItem;
import com.musicplayer.data.local.entities.Track;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Data Access Object for Playlist entity.
 * Provides database operations for playlists and playlist items.
 */
@Dao
public interface PlaylistDao {
    
    // Playlist operations
    @Insert
    long insert(Playlist playlist);
    
    @Insert
    List<Long> insertAll(List<Playlist> playlists);
    
    @Update
    int update(Playlist playlist);
    
    @Update
    int updateAll(List<Playlist> playlists);
    
    @Delete
    int delete(Playlist playlist);
    
    @Query("DELETE FROM playlists WHERE id = :playlistId")
    int deleteById(long playlistId);
    
    @Query("DELETE FROM playlists")
    int deleteAll();
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    Playlist getById(long playlistId);
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    Single<Playlist> getByIdSingle(long playlistId);
    
    @Query("SELECT * FROM playlists WHERE name = :name")
    Playlist getByName(String name);
    
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    List<Playlist> getAll();
    
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    Flowable<List<Playlist>> getAllFlowable();
    
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    Single<List<Playlist>> getAllSingle();
    
    @Query("SELECT * FROM playlists WHERE isSmart = 0 ORDER BY name ASC")
    List<Playlist> getUserPlaylists();
    
    @Query("SELECT * FROM playlists WHERE isSmart = 1 ORDER BY name ASC")
    List<Playlist> getSmartPlaylists();
    
    @Query("SELECT COUNT(*) FROM playlists")
    int getCount();
    
    @Query("SELECT COUNT(*) FROM playlists WHERE isSmart = 0")
    int getUserPlaylistCount();
    
    @Query("SELECT COUNT(*) FROM playlists WHERE isSmart = 1")
    int getSmartPlaylistCount();
    
    // PlaylistItem operations
    @Insert
    long insertItem(PlaylistItem item);
    
    @Insert
    List<Long> insertItems(List<PlaylistItem> items);
    
    @Update
    int updateItem(PlaylistItem item);
    
    @Update
    int updateItems(List<PlaylistItem> items);
    
    @Delete
    int deleteItem(PlaylistItem item);
    
    @Query("DELETE FROM playlist_items WHERE id = :itemId")
    int deleteItemById(long itemId);
    
    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    int deleteAllItems(long playlistId);
    
    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND trackId = :trackId")
    int deleteItem(long playlistId, long trackId);
    
    @Query("SELECT * FROM playlist_items WHERE id = :itemId")
    PlaylistItem getItemById(long itemId);
    
    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY position ASC")
    List<PlaylistItem> getItems(long playlistId);
    
    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY position ASC")
    Flowable<List<PlaylistItem>> getItemsFlowable(long playlistId);
    
    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId AND trackId = :trackId")
    PlaylistItem getItem(long playlistId, long trackId);
    
    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId")
    int getItemCount(long playlistId);
    
    @Query("SELECT tracks.* FROM tracks " +
           "JOIN playlist_items ON tracks.id = playlist_items.trackId " +
           "WHERE playlist_items.playlistId = :playlistId " +
           "ORDER BY playlist_items.position ASC")
    List<Track> getTracks(long playlistId);
    
    @Query("SELECT tracks.* FROM tracks " +
           "JOIN playlist_items ON tracks.id = playlist_items.trackId " +
           "WHERE playlist_items.playlistId = :playlistId " +
           "ORDER BY playlist_items.position ASC")
    Flowable<List<Track>> getTracksFlowable(long playlistId);
    
    @Query("SELECT tracks.* FROM tracks " +
           "JOIN playlist_items ON tracks.id = playlist_items.trackId " +
           "WHERE playlist_items.playlistId = :playlistId " +
           "ORDER BY playlist_items.position ASC")
    Single<List<Track>> getTracksSingle(long playlistId);
    
    // Position management
    @Query("UPDATE playlist_items SET position = position + 1 WHERE playlistId = :playlistId AND position >= :position")
    int shiftPositions(long playlistId, int position);
    
    @Query("UPDATE playlist_items SET position = position - 1 WHERE playlistId = :playlistId AND position > :position")
    int unshiftPositions(long playlistId, int position);
    
    @Query("UPDATE playlist_items SET position = :newPosition WHERE id = :itemId")
    int updatePosition(long itemId, int newPosition);
    
    @Query("SELECT MAX(position) FROM playlist_items WHERE playlistId = :playlistId")
    int getMaxPosition(long playlistId);
    
    // Batch operations
    @Query("SELECT trackId FROM playlist_items WHERE playlistId = :playlistId ORDER BY position ASC")
    List<Long> getTrackIds(long playlistId);
    
    @Query("SELECT playlistId FROM playlist_items WHERE trackId = :trackId")
    List<Long> getPlaylistsForTrack(long trackId);
    
    @Transaction
    default void addTrackToPlaylist(long playlistId, long trackId) {
        int maxPosition = getMaxPosition(playlistId);
        PlaylistItem item = new PlaylistItem(playlistId, trackId, maxPosition + 1);
        insertItem(item);
        
        // Update playlist stats
        updatePlaylistStats(playlistId);
    }
    
    @Transaction
    default void removeTrackFromPlaylist(long playlistId, long trackId) {
        PlaylistItem item = getItem(playlistId, trackId);
        if (item != null) {
            deleteItem(item);
            unshiftPositions(playlistId, item.getPosition());
            updatePlaylistStats(playlistId);
        }
    }
    
    @Transaction
    default void moveTrack(long playlistId, int fromPosition, int toPosition) {
        if (fromPosition == toPosition) return;
        
        // Get the item being moved
        @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId AND position = :position")
        PlaylistItem item = getItemByPosition(playlistId, fromPosition);
        
        if (item == null) return;
        
        if (fromPosition < toPosition) {
            // Moving down - shift items up
            shiftPositions(playlistId, fromPosition + 1, toPosition, -1);
        } else {
            // Moving up - shift items down
            shiftPositions(playlistId, toPosition, fromPosition - 1, 1);
        }
        
        // Update the moved item's position
        updatePosition(item.getId(), toPosition);
    }
    
    // Helper query for move operation
    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId AND position = :position")
    PlaylistItem getItemByPosition(long playlistId, int position);
    
    // Helper for batch position update
    @Query("UPDATE playlist_items SET position = position + :offset " +
           "WHERE playlistId = :playlistId AND position >= :startPos AND position <= :endPos")
    int shiftPositions(long playlistId, int startPos, int endPos, int offset);
    
    // Playlist statistics update
    @Query("UPDATE playlists SET trackCount = (SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId), " +
           "duration = (SELECT SUM(tracks.duration) FROM tracks " +
           "JOIN playlist_items ON tracks.id = playlist_items.trackId " +
           "WHERE playlist_items.playlistId = :playlistId), " +
           "dateModified = :modifiedDate " +
           "WHERE id = :playlistId")
    int updatePlaylistStats(long playlistId, Date modifiedDate);
    
    @Transaction
    default void updatePlaylistStats(long playlistId) {
        updatePlaylistStats(playlistId, new java.util.Date());
    }
    
    // Smart playlist operations
    @Query("SELECT * FROM playlists WHERE smartType = :smartType")
    List<Playlist> getSmartPlaylistsByType(String smartType);
    
    @Query("UPDATE playlists SET smartCriteria = :criteria WHERE id = :playlistId")
    int updateSmartCriteria(long playlistId, String criteria);
    
    // Search operations
    @Query("SELECT playlists.* FROM playlists " +
           "WHERE playlists.name LIKE :query AND playlists.isSmart = 0")
    List<Playlist> searchUserPlaylists(String query);
    
    @Query("SELECT playlists.* FROM playlists " +
           "WHERE playlists.name LIKE :query")
    List<Playlist> searchAllPlaylists(String query);
    
    // Import/Export operations
    @Query("UPDATE playlists SET importUrl = :importUrl WHERE id = :playlistId")
    int setImportUrl(long playlistId, String importUrl);
    
    @Query("UPDATE playlists SET exportUrl = :exportUrl WHERE id = :playlistId")
    int setExportUrl(long playlistId, String exportUrl);
    
    // Data validation
    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId AND trackId NOT IN (SELECT id FROM tracks)")
    int getOrphanedItemCount(long playlistId);
    
    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND trackId NOT IN (SELECT id FROM tracks)")
    int deleteOrphanedItems(long playlistId);
    
    // Playlist item statistics
    @Query("SELECT SUM(tracks.duration) FROM tracks " +
           "JOIN playlist_items ON tracks.id = playlist_items.trackId " +
           "WHERE playlist_items.playlistId = :playlistId")
    long getPlaylistDuration(long playlistId);
    
    @Query("SELECT SUM(tracks.playCount) FROM tracks " +
           "JOIN playlist_items ON tracks.id = playlist_items.trackId " +
           "WHERE playlist_items.playlistId = :playlistId")
    int getPlaylistPlayCount(long playlistId);
    
    @Query("SELECT AVG(tracks.rating) FROM tracks " +
           "JOIN playlist_items ON tracks.id = playlist_items.trackId " +
           "WHERE playlist_items.playlistId = :playlistId AND tracks.rating > 0")
    float getPlaylistAverageRating(long playlistId);
}