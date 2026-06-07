package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import java.util.Optional;

/**
 * Classe responsabile della creazione delle playlist.
 *
 * Permette di creare sia playlist vuote, modificabili manualmente
 * dall'utente, sia playlist automatiche basate sui metadati dei brani.
 *
 * Le playlist automatiche possono essere generate per genere musicale
 * oppure per anno di uscita.
 */
public class PlaylistGenerator {


    /**
     * Crea una playlist vuota con il titolo indicato.
     *
     * La playlist creata può essere modificata manualmente dall'utente,
     * aggiungendo o rimuovendo brani.
     *
     * @param title Titolo della playlist da creare.
     * @return Una nuova playlist vuota.
     * @throws IllegalArgumentException Se il titolo è nullo o vuoto.
     */
    public Playlist createEmptyPlaylist(String title) {
        return new Playlist(title);
    }

    /**
     * Crea una playlist automatica filtrata per genere musicale.
     *
     * Il metodo verifica che il genere indicato sia valido e che la lista
     * dei brani non sia nulla. La playlist restituita non contiene una copia
     * statica dei brani, ma ricalcola dinamicamente il proprio contenuto
     * tramite la classe AutomaticPlaylist.
     *
     * Se non esistono brani compatibili con il genere selezionato,
     * viene restituito Optional.empty().
     *
     * @param genre Genere musicale usato come criterio di filtro.
     * @param tracks Lista dei brani disponibili nella libreria.
     * @return Optional contenente la playlist automatica, se esistono brani compatibili.
     * @throws IllegalArgumentException Se il genere è nullo o vuoto.
     * @throws IllegalArgumentException Se la lista dei brani è nulla.
     */
    public Optional<Playlist> createPlaylistByGenre(String genre, List<Track> tracks) {
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException("Il genere non può essere vuoto.");
        }

        if (tracks == null) {
            throw new IllegalArgumentException("La lista dei brani non può essere nulla.");
        }

        AutomaticPlaylist playlist = AutomaticPlaylist.byGenre(genre);

        if (playlist.getTrackCount() == 0) {
            return Optional.empty();
        }

        return Optional.of(playlist);
    }

    /**
     * Crea una playlist automatica filtrata per anno di uscita.
     *
     * Il metodo verifica che l'anno indicato sia valido e che la lista
     * dei brani non sia nulla. La playlist restituita non contiene una copia
     * statica dei brani, ma ricalcola dinamicamente il proprio contenuto
     * tramite la classe AutomaticPlaylist.
     *
     * Se non esistono brani compatibili con l'anno selezionato,
     * viene restituito Optional.empty().
     *
     * @param year Anno usato come criterio di filtro.
     * @param tracks Lista dei brani disponibili nella libreria.
     * @return Optional contenente la playlist automatica, se esistono brani compatibili.
     * @throws IllegalArgumentException Se l'anno è nullo.
     * @throws IllegalArgumentException Se la lista dei brani è nulla.
     */
    public Optional<Playlist> createPlaylistByYear(Integer year, List<Track> tracks) {
        if (year == null) {
            throw new IllegalArgumentException("L'anno non può essere vuoto.");
        }

        if (tracks == null) {
            throw new IllegalArgumentException("La lista dei brani non può essere nulla.");
        }

        AutomaticPlaylist playlist = AutomaticPlaylist.byYear(year);

        if (playlist.getTrackCount() == 0) {
            return Optional.empty();
        }

        return Optional.of(playlist);
    }
}