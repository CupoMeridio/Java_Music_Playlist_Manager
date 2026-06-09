package it.unisa.java_music_playlist_manager.model;

/**
 * L'interfaccia Playlist definisce il contratto per una collezione di brani.
 * Estende l'interfaccia Playable per garantire la compatibilità con il sistema di riproduzione.
 */
public interface Playlist extends Playable {
    
    /**
     * Imposta il titolo della playlist.
     * @param title Il nuovo titolo della playlist.
     */
    void setTitle(String title);

    /**
     * Restituisce il numero totale di tracce contenute nella playlist.
     * @return Il conteggio totale delle tracce.
     */
    int getTrackCount();

    /**
     * Calcola la durata totale della playlist sommando le durate di tutte le tracce contenute.
     * @return La durata totale in secondi.
     */
    int getDuration();
}
