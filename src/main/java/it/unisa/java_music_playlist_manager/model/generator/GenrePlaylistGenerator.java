package it.unisa.java_music_playlist_manager.model.generator;

import it.unisa.java_music_playlist_manager.model.AutomaticPlaylistByGenre;
import it.unisa.java_music_playlist_manager.model.Playable;

/**
 * Creatore concreto (GoF Concrete Creator) per la generazione di playlist automatiche filtrate per genere.
 */
public class GenrePlaylistGenerator extends PlaylistGenerator {

    private final String genre;

    /**
     * Costruisce il generatore specificando il genere musicale di filtraggio.
     *
     * @param genre Il genere musicale (es. "Rock", "Pop").
     */
    public GenrePlaylistGenerator(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException("Il genere musicale non può essere nullo o vuoto.");
        }
        this.genre = genre;
    }

    @Override
    public Playable createPlaylist(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo della playlist non può essere vuoto.");
        }
        return new AutomaticPlaylistByGenre(title, genre);
    }
}
