package it.unisa.java_music_playlist_manager.model.generator;

import it.unisa.java_music_playlist_manager.model.Playable;

/**
 * Creatore concreto (GoF Concrete Creator) per la generazione di playlist automatiche filtrate per anno.
 */
public class YearPlaylistGenerator extends PlaylistGenerator {

    private final int year;

    /**
     * Costruisce il generatore specificando l'anno di pubblicazione di filtraggio.
     *
     * @param year L'anno di pubblicazione (es. 2024).
     */
    public YearPlaylistGenerator(int year) {
        this.year = year;
    }

    @Override
    public Playable createPlaylist(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo della playlist non può essere vuoto.");
        }
        return new AutomaticPlaylistByYear(title, year);
    }
}
