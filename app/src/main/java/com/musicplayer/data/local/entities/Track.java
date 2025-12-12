package com.musicplayer.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;

import java.util.Date;

/**
 * Entity class representing a music track in the database.
 * Stores all metadata and playback information for individual tracks.
 */
@Entity(
    tableName = "tracks",
    indices = {
        @Index(value = "albumId"),
        @Index(value = "artistId"),
        @Index(value = "title"),
        @Index(value = "filePath", unique = true)
    }
)
public class Track {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "title")
    private String title;
    
    @ColumnInfo(name = "artist")
    private String artist;
    
    @ColumnInfo(name = "album")
    private String album;
    
    @ColumnInfo(name = "albumId")
    private long albumId;
    
    @ColumnInfo(name = "artistId")
    private long artistId;
    
    @ColumnInfo(name = "filePath")
    private String filePath;
    
    @ColumnInfo(name = "duration")
    private long duration; // in milliseconds
    
    @ColumnInfo(name = "trackNumber")
    private int trackNumber;
    
    @ColumnInfo(name = "year")
    private int year;
    
    @ColumnInfo(name = "genre")
    private String genre;
    
    @ColumnInfo(name = "mimeType")
    private String mimeType;
    
    @ColumnInfo(name = "albumArtPath")
    private String albumArtPath;
    
    @ColumnInfo(name = "lyrics")
    private String lyrics;
    
    @ColumnInfo(name = "composer")
    private String composer;
    
    @ColumnInfo(name = "playCount")
    private int playCount;
    
    @ColumnInfo(name = "lastPlayed")
    private Date lastPlayed;
    
    @ColumnInfo(name = "dateAdded")
    private Date dateAdded;
    
    @ColumnInfo(name = "dateModified")
    private Date dateModified;
    
    @ColumnInfo(name = "favorite")
    private boolean favorite;
    
    @ColumnInfo(name = "rating")
    private int rating; // 0-5 stars
    
    @ColumnInfo(name = "bitrate")
    private int bitrate;
    
    @ColumnInfo(name = "sampleRate")
    private int sampleRate;
    
    @ColumnInfo(name = "channels")
    private int channels;
    
    @ColumnInfo(name = "bookmark")
    private long bookmark; // playback position in milliseconds
    
    @ColumnInfo(name = "tags")
    private String tags; // comma-separated custom tags
    
    @ColumnInfo(name = "isLocal")
    private boolean isLocal;
    
    @ColumnInfo(name = "streamUrl")
    private String streamUrl;
    
    @ColumnInfo(name = "downloadId")
    private long downloadId;
    
    @ColumnInfo(name = "downloadStatus")
    private int downloadStatus; // 0=not downloaded, 1=downloading, 2=downloaded
    
    @ColumnInfo(name = "fileSize")
    private long fileSize;
    
    // Constructors
    public Track() {
        this.dateAdded = new Date();
        this.dateModified = new Date();
        this.playCount = 0;
        this.rating = 0;
        this.favorite = false;
        this.isLocal = true;
        this.downloadStatus = 0;
    }
    
    public Track(String title, String artist, String album, String filePath) {
        this();
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.filePath = filePath;
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    
    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }
    
    public long getAlbumId() { return albumId; }
    public void setAlbumId(long albumId) { this.albumId = albumId; }
    
    public long getArtistId() { return artistId; }
    public void setArtistId(long artistId) { this.artistId = artistId; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    
    public int getTrackNumber() { return trackNumber; }
    public void setTrackNumber(int trackNumber) { this.trackNumber = trackNumber; }
    
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    
    public String getAlbumArtPath() { return albumArtPath; }
    public void setAlbumArtPath(String albumArtPath) { this.albumArtPath = albumArtPath; }
    
    public String getLyrics() { return lyrics; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }
    
    public String getComposer() { return composer; }
    public void setComposer(String composer) { this.composer = composer; }
    
    public int getPlayCount() { return playCount; }
    public void setPlayCount(int playCount) { this.playCount = playCount; }
    
    public Date getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(Date lastPlayed) { this.lastPlayed = lastPlayed; }
    
    public Date getDateAdded() { return dateAdded; }
    public void setDateAdded(Date dateAdded) { this.dateAdded = dateAdded; }
    
    public Date getDateModified() { return dateModified; }
    public void setDateModified(Date dateModified) { this.dateModified = dateModified; }
    
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    
    public int getBitrate() { return bitrate; }
    public void setBitrate(int bitrate) { this.bitrate = bitrate; }
    
    public int getSampleRate() { return sampleRate; }
    public void setSampleRate(int sampleRate) { this.sampleRate = sampleRate; }
    
    public int getChannels() { return channels; }
    public void setChannels(int channels) { this.channels = channels; }
    
    public long getBookmark() { return bookmark; }
    public void setBookmark(long bookmark) { this.bookmark = bookmark; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    public boolean isLocal() { return isLocal; }
    public void setLocal(boolean local) { isLocal = local; }
    
    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }
    
    public long getDownloadId() { return downloadId; }
    public void setDownloadId(long downloadId) { this.downloadId = downloadId; }
    
    public int getDownloadStatus() { return downloadStatus; }
    public void setDownloadStatus(int downloadStatus) { this.downloadStatus = downloadStatus; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    // Utility methods
    public void incrementPlayCount() {
        this.playCount++;
        this.lastPlayed = new Date();
    }
    
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
    
    public boolean hasAlbumArt() {
        return albumArtPath != null && !albumArtPath.isEmpty();
    }
    
    public boolean isDownloaded() {
        return downloadStatus == 2;
    }
    
    public boolean isDownloading() {
        return downloadStatus == 1;
    }
}