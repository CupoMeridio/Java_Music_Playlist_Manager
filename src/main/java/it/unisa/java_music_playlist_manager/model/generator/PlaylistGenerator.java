package it.unisa.java_music_playlist_manager.model.generator;

import it.unisa.java_music_playlist_manager.model.Playable;

/**
 * Classe astratta che funge da Creator nel pattern Factory Method.
 * Definisce il metodo astratto per la generazione di playlist in modo
 * che la Library o il Controller possano usarla in modo polimorfico.
 */
public abstract class PlaylistGenerator {

    /**
     * Metodo di fabbrica (Factory Method) per creare una playlist.
     * * @param title Il titolo da assegnare alla playlist.
     * 
     * @return Un elemento riproducibile (Playable), nello specifico una Playlist.
     */
    public abstract Playable createPlaylist(String title);
}
