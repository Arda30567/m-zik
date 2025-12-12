package com.musicplayer.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.musicplayer.data.local.dao.TrackDao;
import com.musicplayer.data.local.dao.AlbumDao;
import com.musicplayer.data.local.dao.ArtistDao;
import com.musicplayer.data.local.dao.PlaylistDao;
import com.musicplayer.data.local.entities.Track;
import com.musicplayer.data.local.entities.Album;
import com.musicplayer.data.local.entities.Artist;
import com.musicplayer.data.local.entities.Playlist;
import com.musicplayer.data.local.entities.PlaylistItem;
import com.musicplayer.utils.Converters;

/**
 * Main Room Database class for the Music Player application.
 * Manages all database entities and provides DAO access.
 */
@Database(
    entities = {
        Track.class,
        Album.class,
        Artist.class,
        Playlist.class,
        PlaylistItem.class
    },
    version = 1,
    exportSchema = true
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static volatile AppDatabase INSTANCE;
    
    // Database name
    private static final String DATABASE_NAME = "music_player.db";
    
    // DAO accessors
    public abstract TrackDao trackDao();
    public abstract AlbumDao albumDao();
    public abstract ArtistDao artistDao();
    public abstract PlaylistDao playlistDao();
    
    /**
     * Gets the singleton instance of the database.
     * Uses double-checked locking for thread safety.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = buildDatabase(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Builds the database with configuration and migrations.
     */
    private static AppDatabase buildDatabase(Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        // Database created for the first time
                        // Initialize with default data if needed
                    }
                    
                    @Override
                    public void onOpen(SupportSQLiteDatabase db) {
                        super.onOpen(db);
                        // Database opened
                        // Set WAL mode for better performance
                        db.execSQL("PRAGMA journal_mode=WAL");
                        db.execSQL("PRAGMA synchronous=NORMAL");
                        db.execSQL("PRAGMA cache_size=10000");
                        db.execSQL("PRAGMA temp_store=memory");
                    }
                })
                .fallbackToDestructiveMigration()
                .build();
    }
    
    /**
     * Database migration from version 1 to 2.
     * Example migration - add new columns or tables.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Example migration operations:
            // database.execSQL("ALTER TABLE tracks ADD COLUMN new_column TEXT DEFAULT NULL");
            // database.execSQL("CREATE TABLE new_table (...)");
            // database.execSQL("CREATE INDEX index_name ON table_name(column_name)");
        }
    };
    
    /**
     * Database migration from version 2 to 3.
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add more migrations as needed
        }
    };
    
    /**
     * Destroys the database instance (for testing).
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }
    
    /**
     * Checks if the database is open and ready to use.
     */
    public boolean isDatabaseOpen() {
        return INSTANCE != null && INSTANCE.isOpen();
    }
    
    /**
     * Gets the database file path.
     */
    public static String getDatabasePath(Context context) {
        return context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
    }
    
    /**
     * Database configuration constants.
     */
    public static class Config {
        // Maximum number of tracks to load at once for pagination
        public static final int TRACK_PAGE_SIZE = 50;
        
        // Maximum number of albums to load at once
        public static final int ALBUM_PAGE_SIZE = 20;
        
        // Maximum number of artists to load at once
        public static final int ARTIST_PAGE_SIZE = 20;
        
        // Maximum number of playlist items to load at once
        public static final int PLAYLIST_ITEM_PAGE_SIZE = 100;
        
        // Database query timeout in milliseconds
        public static final long QUERY_TIMEOUT_MS = 30000;
        
        // Maximum database size in bytes (100MB)
        public static final long MAX_DATABASE_SIZE = 100 * 1024 * 1024;
    }
}