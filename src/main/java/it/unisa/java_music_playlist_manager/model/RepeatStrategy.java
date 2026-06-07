package it.unisa.java_music_playlist_manager.model;

public class RepeatStrategy implements PlaybackStrategy {
    @Override
    public int getNextIndex(int currentIndex, int queueSize) {
        // Il pattern Composite permette di ripetere l'entità corrente.
        // Se è una singola Traccia, si ripeterà.
        // Se è una Playlist, si ripeterà dall'inizio una volta terminate tutte le sue tracce.
        return currentIndex;
    }
}
