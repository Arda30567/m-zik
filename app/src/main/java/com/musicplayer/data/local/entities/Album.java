package com.musicplayer.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;

import java.util.Date;

/**
 * Entity class representing a music album in the database.
 * Groups tracks by album and stores album-specific metadata.
 */
@Entity(
    tableName = "albums",
    indices = {
        @Index(value = "title"),
        @Index(value = "artist"),
        @Index(value = "artistId")
    }
)
public class Album {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "title")
    private String title;
    
    @ColumnInfo(name = "artist")
    private String artist;
    
    @ColumnInfo(name = "artistId")
    private long artistId;
    
    @ColumnInfo(name = "year")
    private int year;
    
    @ColumnInfo(name = "genre")
    private String genre;
    
    @ColumnInfo(name = "artPath")
    private String artPath;
    
    @ColumnInfo(name = "trackCount")
    private int trackCount;
    
    @ColumnInfo(name = "duration")
    private long duration; // total duration in milliseconds
    
    @ColumnInfo(name = "dateAdded")
    private Date dateAdded;
    
    @ColumnInfo(name = "dateModified")
    private Date dateModified;
    
    @ColumnInfo(name = "description")
    private String description;
    
    @ColumnInfo(name = "isCompilation")
    private boolean isCompilation;
    
    @ColumnInfo(name = "albumArtist")
    private String albumArtist;
    
    @ColumnInfo(name = "recordLabel")
    private String recordLabel;
    
    // Constructors
    public Album() {
        this.dateAdded = new Date();
        this.dateModified = new Date();
        this.trackCount = 0;
        this.duration = 0;
        this.isCompilation = false;
    }
    
    public Album(String title, String artist) {
        this();
        this.title = title;
        this.artist = artist;
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    
    public long getArtistId() { return artistId; }
    public void setArtistId(long artistId) { this.artistId = artistId; }
    
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    
    public String getArtPath() { return artPath; }
    public void setArtPath(String artPath) { this.artPath = artPath; }
    
    public int getTrackCount() { return trackCount; }
    public void setTrackCount(int trackCount) { this.trackCount = trackCount; }
    
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    
    public Date getDateAdded() { return dateAdded; }
    public void setDateAdded(Date dateAdded) { this.dateAdded = dateAdded; }
    
    public Date getDateModified() { return dateModified; }
    public void setDateModified(Date dateModified) { this.dateModified = dateModified; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isCompilation() { return isCompilation; }
    public void setCompilation(boolean compilation) { isCompilation = compilation; }
    
    public String getAlbumArtist() { return albumArtist; }
    public void setAlbumArtist(String albumArtist) { this.albumArtist = albumArtist; }
    
    public String getRecordLabel() { return recordLabel; }
    public void setRecordLabel(String recordLabel) { this.recordLabel = recordLabel; }
    
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
    
    public String getDisplayArtist() {
        if (albumArtist != null && !albumArtist.isEmpty()) {
            return albumArtist;
        }
        return artist;
    }
    
    public void updateModifiedDate() {
        this.dateModified = new Date();
    }
}