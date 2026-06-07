package it.unisa.java_music_playlist_manager.model;

/**
 * L'interfaccia PlaybackStrategy definisce il contratto per le strategie di avanzamento nella coda.
 * È la componente base del Pattern Strategy utilizzato nel PlaybackManager.
 * 
 * Permette di cambiare a runtime il comportamento del lettore quando una traccia
 * o un elemento della coda termina, decidendo quale sarà il prossimo indice da riprodurre.
 */
public interface PlaybackStrategy {
    /**
     * Calcola l'indice del prossimo elemento da riprodurre nella coda.
     * 
     * @param currentIndex L'indice dell'elemento attualmente in riproduzione.
     * @param queueSize    La dimensione totale della coda.
     * @return L'indice del prossimo elemento, oppure un valore fuori range (es. queueSize) 
     *         se la riproduzione deve terminare.
     */
    int getNextIndex(int currentIndex, int queueSize);

    /**
     * Calcola l'indice della prossima traccia all'interno dell'elemento Playable corrente.
     * Per default, avanza semplicemente alla traccia successiva.
     *
     * @param currentTrackIndex L'indice della traccia attualmente in riproduzione.
     * @param tracksSize        Il numero totale di tracce nel Playable.
     * @return L'indice della prossima traccia.
     */
    default int getNextTrackIndex(int currentTrackIndex, int tracksSize) {
        return currentTrackIndex + 1;
    }
}