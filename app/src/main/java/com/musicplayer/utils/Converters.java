package com.musicplayer.utils;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Type converters for Room database to handle complex data types.
 */
public class Converters {
    
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }
    
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}