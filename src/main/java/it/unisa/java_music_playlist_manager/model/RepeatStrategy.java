package it.unisa.java_music_playlist_manager.model;

/**
 * Implementazione della strategia di ripetizione dell'elemento corrente.
 * In questa modalità, una volta terminato un elemento (Track o Playlist),
 * il lettore ricomincia a riprodurre lo stesso elemento dall'inizio.
 */
public class RepeatStrategy implements PlaybackStrategy {
    @Override
    public int getNextIndex(int currentIndex, int queueSize) {
        // Restituendo lo stesso indice, il PlaybackManager ricomincerà la riproduzione
        // dell'elemento Playable corrente (sia esso una singola traccia o un'intera playlist).
        return currentIndex;
    }
}
