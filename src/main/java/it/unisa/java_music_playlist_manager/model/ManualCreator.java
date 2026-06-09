package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import java.util.function.Supplier;

/**
 * Concrete Creator per la creazione di ManualPlaylist.
 */
public class ManualCreator implements PlaylistCreator {

    /**
     * Crea una playlist manuale.
     * @param param Il titolo della playlist.
     * @param trackSource Non utilizzato per le playlist manuali (può essere null).
     * @return Una nuova ManualPlaylist.
     */
    @Override
    public Playlist createPlaylist(String param, Supplier<List<Track>> trackSource) {
        if (param == null || param.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo della playlist non può essere vuoto.");
        }
        return new ManualPlaylist(param);
    }
}
