package com.musicplayer.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.musicplayer.data.local.entities.Track;

import java.io.File;

/**
 * Utility class for extracting metadata from audio files.
 */
public class MetadataExtractor {
    
    private static final String TAG = "MetadataExtractor";
    private final Context context;
    
    public MetadataExtractor(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Extracts metadata from an audio file and creates a Track object.
     */
    public Track extractMetadata(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Track track = new Track();
        
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.canRead()) {
                Log.w(TAG, "File does not exist or cannot be read: " + filePath);
                return null;
            }
            
            // Set the data source
            retriever.setDataSource(filePath);
            
            // Extract basic metadata
            track.setFilePath(filePath);
            track.setTitle(extractMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_TITLE, file.getName()));
            track.setArtist(extractMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_ARTIST, "Unknown Artist"));
            track.setAlbum(extractMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_ALBUM, "Unknown Album"));
            
            // Extract duration
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr != null) {
                try {
                    track.setDuration(Long.parseLong(durationStr));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid duration: " + durationStr);
                }
            }
            
            // Extract track number
            String trackNumberStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
            if (trackNumberStr != null) {
                try {
                    track.setTrackNumber(Integer.parseInt(trackNumberStr));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid track number: " + trackNumberStr);
                }
            }
            
            // Extract year
            String yearStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
            if (yearStr != null) {
                try {
                    track.setYear(Integer.parseInt(yearStr));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid year: " + yearStr);
                }
            }
            
            // Extract genre
            track.setGenre(extractMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_GENRE, ""));
            
            // Extract composer
            track.setComposer(extractMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_COMPOSER, ""));
            
            // Extract MIME type
            String mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            if (mimeType != null) {
                track.setMimeType(mimeType);
            }
            
            // Extract bitrate
            String bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            if (bitrateStr != null) {
                try {
                    track.setBitrate(Integer.parseInt(bitrateStr));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid bitrate: " + bitrateStr);
                }
            }
            
            // Set file properties
            track.setFileSize(file.length());
            track.setIsLocal(true);
            
            // Extract album art path (if available)
            // Note: This would typically be handled by a separate album art extraction utility
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting metadata from: " + filePath, e);
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                Log.w(TAG, "Error releasing MediaMetadataRetriever", e);
            }
        }
        
        return track;
    }
    
    /**
     * Extracts metadata with fallback to default value.
     */
    private String extractMetadata(MediaMetadataRetriever retriever, int key, String defaultValue) {
        String value = retriever.extractMetadata(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Extracts metadata from a URI.
     */
    public Track extractMetadataFromUri(Uri uri) {
        if (uri == null) {
            return null;
        }
        
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Track track = new Track();
        
        try {
            retriever.setDataSource(context, uri);
            
            // Extract metadata similar to file-based extraction
            track.setTitle(extractMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_TITLE, "Unknown"));
            track.setArtist(extractMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_ARTIST, "Unknown Artist"));
            track.setAlbum(extractMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_ALBUM, "Unknown Album"));
            
            // Duration
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr != null) {
                try {
                    track.setDuration(Long.parseLong(durationStr));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid duration: " + durationStr);
                }
            }
            
            // Set as streaming track
            track.setStreamUrl(uri.toString());
            track.setIsLocal(false);
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting metadata from URI: " + uri, e);
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                Log.w(TAG, "Error releasing MediaMetadataRetriever", e);
            }
        }
        
        return track;
    }
    
    /**
     * Gets file extension from file path.
     */
    public static String getFileExtension(String filePath) {
        if (filePath == null || filePath.lastIndexOf('.') == -1) {
            return "";
        }
        return filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();
    }
    
    /**
     * Checks if a file is an audio file based on its extension.
     */
    public static boolean isAudioFile(String filePath) {
        String extension = getFileExtension(filePath);
        switch (extension) {
            case "mp3":
            case "m4a":
            case "aac":
            case "flac":
            case "wav":
            case "ogg":
            case "wma":
                return true;
            default:
                return false;
        }
    }
}