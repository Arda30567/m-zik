package com.musicplayer.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;

import java.util.Date;

/**
 * Entity class representing a user-created playlist in the database.
 * Stores playlist metadata and configuration.
 */
@Entity(
    tableName = "playlists",
    indices = {
        @Index(value = "name", unique = true)
    }
)
public class Playlist {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "description")
    private String description;
    
    @ColumnInfo(name = "trackCount")
    private int trackCount;
    
    @ColumnInfo(name = "duration")
    private long duration; // total duration in milliseconds
    
    @ColumnInfo(name = "dateCreated")
    private Date dateCreated;
    
    @ColumnInfo(name = "dateModified")
    private Date dateModified;
    
    @ColumnInfo(name = "isSmart")
    private boolean isSmart;
    
    @ColumnInfo(name = "smartType")
    private String smartType; // "recent", "most_played", "favorites", "custom"
    
    @ColumnInfo(name = "smartCriteria")
    private String smartCriteria; // JSON string for custom smart playlist criteria
    
    @ColumnInfo(name = "sortOrder")
    private String sortOrder;
    
    @ColumnInfo(name = "artPath")
    private String artPath;
    
    @ColumnInfo(name = "isPublic")
    private boolean isPublic;
    
    @ColumnInfo(name = "importUrl")
    private String importUrl;
    
    @ColumnInfo(name = "exportUrl")
    private String exportUrl;
    
    @ColumnInfo(name = "playCount")
    private int playCount;
    
    @ColumnInfo(name = "lastPlayed")
    private Date lastPlayed;
    
    @ColumnInfo(name = "autoRefresh")
    private boolean autoRefresh;
    
    @ColumnInfo(name = "refreshInterval")
    private long refreshInterval; // in hours
    
    // Constants for smart playlist types
    public static final String SMART_TYPE_RECENT = "recent";
    public static final String SMART_TYPE_MOST_PLAYED = "most_played";
    public static final String SMART_TYPE_FAVORITES = "favorites";
    public static final String SMART_TYPE_CUSTOM = "custom";
    
    // Constants for sort orders
    public static final String SORT_BY_TITLE = "title";
    public static final String SORT_BY_ARTIST = "artist";
    public static final String SORT_BY_ALBUM = "album";
    public static final String SORT_BY_DURATION = "duration";
    public static final String SORT_BY_DATE_ADDED = "date_added";
    public static final String SORT_BY_PLAY_COUNT = "play_count";
    public static final String SORT_BY_TRACK_NUMBER = "track_number";
    public static final String SORT_BY_RANDOM = "random";
    
    // Constructors
    public Playlist() {
        this.dateCreated = new Date();
        this.dateModified = new Date();
        this.trackCount = 0;
        this.duration = 0;
        this.isSmart = false;
        this.isPublic = false;
        this.playCount = 0;
        this.autoRefresh = false;
        this.refreshInterval = 24; // default 24 hours
        this.sortOrder = SORT_BY_TITLE;
    }
    
    public Playlist(String name) {
        this();
        this.name = name;
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getTrackCount() { return trackCount; }
    public void setTrackCount(int trackCount) { this.trackCount = trackCount; }
    
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    
    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }
    
    public Date getDateModified() { return dateModified; }
    public void setDateModified(Date dateModified) { this.dateModified = dateModified; }
    
    public boolean isSmart() { return isSmart; }
    public void setSmart(boolean smart) { isSmart = smart; }
    
    public String getSmartType() { return smartType; }
    public void setSmartType(String smartType) { this.smartType = smartType; }
    
    public String getSmartCriteria() { return smartCriteria; }
    public void setSmartCriteria(String smartCriteria) { this.smartCriteria = smartCriteria; }
    
    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
    
    public String getArtPath() { return artPath; }
    public void setArtPath(String artPath) { this.artPath = artPath; }
    
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean aPublic) { isPublic = aPublic; }
    
    public String getImportUrl() { return importUrl; }
    public void setImportUrl(String importUrl) { this.importUrl = importUrl; }
    
    public String getExportUrl() { return exportUrl; }
    public void setExportUrl(String exportUrl) { this.exportUrl = exportUrl; }
    
    public int getPlayCount() { return playCount; }
    public void setPlayCount(int playCount) { this.playCount = playCount; }
    
    public Date getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(Date lastPlayed) { this.lastPlayed = lastPlayed; }
    
    public boolean isAutoRefresh() { return autoRefresh; }
    public void setAutoRefresh(boolean autoRefresh) { this.autoRefresh = autoRefresh; }
    
    public long getRefreshInterval() { return refreshInterval; }
    public void setRefreshInterval(long refreshInterval) { this.refreshInterval = refreshInterval; }
    
    // Utility methods
    public String getDurationString() {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else {
            return String.format("%d:%02d", minutes, seconds % 60);
        }
    }
    
    public boolean hasArtwork() {
        return artPath != null && !artPath.isEmpty();
    }
    
    public void incrementPlayCount() {
        this.playCount++;
        this.lastPlayed = new Date();
    }
    
    public void updateModifiedDate() {
        this.dateModified = new Date();
    }
    
    public void updateTrackCount(int count) {
        this.trackCount = count;
        updateModifiedDate();
    }
    
    public void updateDuration(long duration) {
        this.duration = duration;
        updateModifiedDate();
    }
    
    public boolean isSystemPlaylist() {
        return isSmart && (
            SMART_TYPE_RECENT.equals(smartType) ||
            SMART_TYPE_MOST_PLAYED.equals(smartType) ||
            SMART_TYPE_FAVORITES.equals(smartType)
        );
    }
    
    public String getFormattedTrackCount() {
        return trackCount + " tracks";
    }
}