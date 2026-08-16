package it.unisa.java_music_playlist_manager.model;

/**
 * Interfaccia che estende {@link Playable} per definire le operazioni di
 * modifica di una playlist.
 * Include metodi per l'aggiunta/rimozione di elementi e brani, e per la
 * riorganizzazione della coda di riproduzione.
 */
public interface MutablePlaylist extends Playable {
    /** Aggiunge un elemento alla playlist */
    void add(Playable element);

    /** Rimuove un elemento dalla playlist */
    void remove(Playable element);

    /** Rimuove un brano specifico dalla playlist */
    void removeTrack(Track track);

    /** Aggiunge un brano alla playlist */
    void addTrack(Track track);

    /** Sposta un elemento da una posizione all'altra */
    void moveElement(int fromIndex, int toIndex);
}
