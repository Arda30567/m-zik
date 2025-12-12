package com.musicplayer.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;

import java.util.Date;

/**
 * Entity class representing a music artist in the database.
 * Stores artist information and aggregates track/album counts.
 */
@Entity(
    tableName = "artists",
    indices = {
        @Index(value = "name", unique = true)
    }
)
public class Artist {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "bio")
    private String bio;
    
    @ColumnInfo(name = "artPath")
    private String artPath;
    
    @ColumnInfo(name = "albumCount")
    private int albumCount;
    
    @ColumnInfo(name = "trackCount")
    private int trackCount;
    
    @ColumnInfo(name = "duration")
    private long duration; // total duration in milliseconds
    
    @ColumnInfo(name = "dateAdded")
    private Date dateAdded;
    
    @ColumnInfo(name = "dateModified")
    private Date dateModified;
    
    @ColumnInfo(name = "birthDate")
    private String birthDate;
    
    @ColumnInfo(name = "deathDate")
    private String deathDate;
    
    @ColumnInfo(name = "origin")
    private String origin;
    
    @ColumnInfo(name = "genre")
    private String genre;
    
    @ColumnInfo(name = "website")
    private String website;
    
    @ColumnInfo(name = "playCount")
    private int playCount;
    
    @ColumnInfo(name = "lastPlayed")
    private Date lastPlayed;
    
    @ColumnInfo(name = "favorite")
    private boolean favorite;
    
    // Constructors
    public Artist() {
        this.dateAdded = new Date();
        this.dateModified = new Date();
        this.albumCount = 0;
        this.trackCount = 0;
        this.duration = 0;
        this.playCount = 0;
        this.favorite = false;
    }
    
    public Artist(String name) {
        this();
        this.name = name;
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public String getArtPath() { return artPath; }
    public void setArtPath(String artPath) { this.artPath = artPath; }
    
    public int getAlbumCount() { return albumCount; }
    public void setAlbumCount(int albumCount) { this.albumCount = albumCount; }
    
    public int getTrackCount() { return trackCount; }
    public void setTrackCount(int trackCount) { this.trackCount = trackCount; }
    
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    
    public Date getDateAdded() { return dateAdded; }
    public void setDateAdded(Date dateAdded) { this.dateAdded = dateAdded; }
    
    public Date getDateModified() { return dateModified; }
    public void setDateModified(Date dateModified) { this.dateModified = dateModified; }
    
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    
    public String getDeathDate() { return deathDate; }
    public void setDeathDate(String deathDate) { this.deathDate = deathDate; }
    
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public int getPlayCount() { return playCount; }
    public void setPlayCount(int playCount) { this.playCount = playCount; }
    
    public Date getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(Date lastPlayed) { this.lastPlayed = lastPlayed; }
    
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    
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
    
    public String getFormattedTrackCount() {
        return trackCount + " tracks";
    }
    
    public String getFormattedAlbumCount() {
        return albumCount + " albums";
    }
}