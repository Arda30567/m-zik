package com.musicplayer.data.local.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;

import java.util.Date;

/**
 * Entity class representing an item in a playlist.
 * Links tracks to playlists with ordering information.
 */
@Entity(
    tableName = "playlist_items",
    foreignKeys = {
        @ForeignKey(
            entity = Playlist.class,
            parentColumns = "id",
            childColumns = "playlistId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Track.class,
            parentColumns = "id",
            childColumns = "trackId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "playlistId"),
        @Index(value = "trackId"),
        @Index(value = {"playlistId", "position"})
    }
)
public class PlaylistItem {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "playlistId")
    private long playlistId;
    
    @ColumnInfo(name = "trackId")
    private long trackId;
    
    @ColumnInfo(name = "position")
    private int position;
    
    @ColumnInfo(name = "dateAdded")
    private Date dateAdded;
    
    @ColumnInfo(name = "dateModified")
    private Date dateModified;
    
    @ColumnInfo(name = "notes")
    private String notes;
    
    @ColumnInfo(name = "playCount")
    private int playCount;
    
    @ColumnInfo(name = "lastPlayed")
    private Date lastPlayed;
    
    @ColumnInfo(name = "isSkipped")
    private boolean isSkipped;
    
    @ColumnInfo(name = "skipCount")
    private int skipCount;
    
    @ColumnInfo(name = "rating")
    private int rating; // 0-5 stars for this specific playlist item
    
    @ColumnInfo(name = "bookmark")
    private long bookmark; // playback position in milliseconds
    
    @ColumnInfo(name = "isDownloaded")
    private boolean isDownloaded;
    
    @ColumnInfo(name = "downloadId")
    private long downloadId;
    
    @ColumnInfo(name = "filePath")
    private String filePath; // local file path if downloaded
    
    // Constructors
    public PlaylistItem() {
        this.dateAdded = new Date();
        this.dateModified = new Date();
        this.position = 0;
        this.playCount = 0;
        this.skipCount = 0;
        this.rating = 0;
        this.bookmark = 0;
        this.isSkipped = false;
        this.isDownloaded = false;
    }
    
    public PlaylistItem(long playlistId, long trackId, int position) {
        this();
        this.playlistId = playlistId;
        this.trackId = trackId;
        this.position = position;
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getPlaylistId() { return playlistId; }
    public void setPlaylistId(long playlistId) { this.playlistId = playlistId; }
    
    public long getTrackId() { return trackId; }
    public void setTrackId(long trackId) { this.trackId = trackId; }
    
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    
    public Date getDateAdded() { return dateAdded; }
    public void setDateAdded(Date dateAdded) { this.dateAdded = dateAdded; }
    
    public Date getDateModified() { return dateModified; }
    public void setDateModified(Date dateModified) { this.dateModified = dateModified; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public int getPlayCount() { return playCount; }
    public void setPlayCount(int playCount) { this.playCount = playCount; }
    
    public Date getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(Date lastPlayed) { this.lastPlayed = lastPlayed; }
    
    public boolean isSkipped() { return isSkipped; }
    public void setSkipped(boolean skipped) { isSkipped = skipped; }
    
    public int getSkipCount() { return skipCount; }
    public void setSkipCount(int skipCount) { this.skipCount = skipCount; }
    
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    
    public long getBookmark() { return bookmark; }
    public void setBookmark(long bookmark) { this.bookmark = bookmark; }
    
    public boolean isDownloaded() { return isDownloaded; }
    public void setDownloaded(boolean downloaded) { isDownloaded = downloaded; }
    
    public long getDownloadId() { return downloadId; }
    public void setDownloadId(long downloadId) { this.downloadId = downloadId; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    // Utility methods
    public void incrementPlayCount() {
        this.playCount++;
        this.lastPlayed = new Date();
        updateModifiedDate();
    }
    
    public void incrementSkipCount() {
        this.skipCount++;
        this.isSkipped = true;
        updateModifiedDate();
    }
    
    public void updateModifiedDate() {
        this.dateModified = new Date();
    }
    
    public void resetSkipStatus() {
        this.isSkipped = false;
        this.skipCount = 0;
        updateModifiedDate();
    }
    
    public boolean hasBookmark() {
        return bookmark > 0;
    }
    
    public String getFormattedPosition() {
        return String.format("%02d", position + 1);
    }
    
    public boolean hasNotes() {
        return notes != null && !notes.isEmpty();
    }
}