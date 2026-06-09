package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import java.util.function.Supplier;
import java.util.Optional;

/**
 * Concrete Creator per la creazione di playlist automatiche.
 * Per rispettare il Factory Method base, prende un parametro per decidere 
 * se usare Genre o Year (oppure offre due metodi factory distinti).
 */
public class AutomaticCreator implements PlaylistCreator {

    public enum Type {
        GENRE,
        YEAR
    }

    private final Type type;

    public AutomaticCreator(Type type) {
        this.type = type;
    }

    /**
     * Metodo del Factory Method.
     * @param param Il genere (se type=GENRE) o l'anno sotto forma di stringa (se type=YEAR).
     * @param trackSource Sorgente dei brani per calcolare la playlist.
     * @return La playlist automatica istanziata.
     */
    @Override
    public Playlist createPlaylist(String param, Supplier<List<Track>> trackSource) {
        if (trackSource == null) {
            throw new IllegalArgumentException("La lista dei brani (sorgente) non può essere nulla.");
        }

        AutomaticPlaylist playlist;
        if (type == Type.GENRE) {
            if (param == null || param.trim().isEmpty()) {
                throw new IllegalArgumentException("Il genere non può essere vuoto.");
            }
            playlist = new AutomaticPlaylistByGenre(param, trackSource);
        } else {
            if (param == null || param.trim().isEmpty()) {
                throw new IllegalArgumentException("L'anno non può essere vuoto.");
            }
            int year;
            try {
                year = Integer.parseInt(param);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("L'anno fornito non è valido.");
            }
            playlist = new AutomaticPlaylistByYear(year, trackSource);
        }

        return playlist;
    }

    /**
     * Metodo di utilità per restituire Optional.empty() se la playlist è vuota,
     * mimando il comportamento originale del PlaylistGenerator.
     */
    public Optional<Playlist> generateIfNotEmpty(String param, Supplier<List<Track>> trackSource) {
        Playlist playlist = createPlaylist(param, trackSource);
        if (playlist.getTrackCount() == 0) {
            return Optional.empty();
        }
        return Optional.of(playlist);
    }
}
