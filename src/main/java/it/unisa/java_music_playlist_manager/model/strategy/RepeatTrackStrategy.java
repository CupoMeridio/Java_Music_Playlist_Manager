package it.unisa.java_music_playlist_manager.model.strategy;

/**
 * Implementazione della strategia di ripetizione della singola traccia.
 * In questa modalità, una volta terminata la traccia corrente (anche
 * all'interno
 * di una playlist), il lettore ricomincia a riprodurre la stessa traccia
 * dall'inizio.
 */
public class RepeatTrackStrategy implements PlaybackStrategy {

    @Override
    public int getNextIndex(int currentIndex, int queueSize) {
        // Se per qualche motivo dobbiamo calcolare l'indice del prossimo Playable
        // manteniamo quello corrente (anche se non dovrebbe essere chiamato
        // finché non si cambia traccia manualmente).
        return currentIndex;
    }

    @Override
    public int getNextTrackIndex(int currentTrackIndex, int tracksSize) {
        // Ripete sempre la stessa traccia
        return currentTrackIndex;
    }
}
