package it.unisa.java_music_playlist_manager.model;

public interface PlaybackStrategy {
    int getNextIndex(int currentIndex, int queueSize);
}