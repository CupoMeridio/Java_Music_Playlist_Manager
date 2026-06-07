package it.unisa.java_music_playlist_manager.model;

/**
 * Implementazione della strategia di riproduzione sequenziale.
 * In questa modalità, il lettore avanza semplicemente all'elemento successivo
 * della coda fino a quando non raggiunge la fine della lista.
 */
public class SequentialStrategy implements PlaybackStrategy {
    @Override
    public int getNextIndex(int currentIndex, int queueSize) {
        // Se non siamo all'ultimo elemento, passiamo al successivo
        if (currentIndex < queueSize - 1) {
            return currentIndex + 1;
        }
        // Restituisce la dimensione della coda per segnalare allo State che la lista è terminata.
        // Lo stato (es. PlayingState) interpreterà questo valore fuori range come fine coda.
        return queueSize;
    }
}