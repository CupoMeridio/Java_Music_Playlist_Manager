package it.unisa.java_music_playlist_manager.model;

/**
 * Implementazione della strategia di ripetizione dell'elemento corrente.
 * In questa modalità, una volta terminato un elemento (Track o Playlist),
 * il lettore ricomincia a riprodurre lo stesso elemento dall'inizio.
 */
public class RepeatStrategy implements PlaybackStrategy {
    @Override
    public int getNextIndex(int currentIndex, int queueSize) {
        // Ripete l'intera coda dall'inizio se siamo arrivati alla fine.
        if (queueSize == 0) return 0;
        return (currentIndex + 1) % queueSize;
    }
}
