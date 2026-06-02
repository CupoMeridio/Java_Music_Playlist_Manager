package it.unisa.java_music_playlist_manager.model;

public class SequentialStrategy implements PlaybackStrategy {
    @Override
    public int getNextIndex(int currentIndex, int queueSize) {
        if (currentIndex < queueSize - 1) {
            return currentIndex + 1;
        }
        // Restituisce la dimensione della coda per segnalare allo State che la lista è terminata
        return queueSize;
    }
}