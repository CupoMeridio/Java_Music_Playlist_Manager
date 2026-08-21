package it.unisa.java_music_playlist_manager.utils;

public class TimeFormatUtils {
    
    /**
     * Formats a duration in seconds into a mm:ss string.
     * @param seconds duration in seconds
     * @return formatted string (e.g. "03:45")
     */
    public static String formatDuration(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
