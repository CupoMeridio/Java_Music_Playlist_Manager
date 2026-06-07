package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Rappresenta una playlist automatica generata a partire dai metadati
 * dei brani presenti nella libreria.
 *
 * A differenza di una playlist normale, non memorizza manualmente
 * i brani al suo interno, ma li ricalcola dinamicamente in base
 * al criterio scelto.
 *
 * I criteri disponibili sono il genere musicale e l'anno di uscita.
 * In questo modo, se un brano cambia genere o anno, la playlist
 * viene aggiornata automaticamente.
 */
public class AutomaticPlaylist extends Playlist {

    public enum Criteria {
        GENRE,
        YEAR
    }

    private final Criteria criteria;
    private final String genre;
    private final Integer year;

    /**
     * Costruisce una playlist automatica.
     *
     *
     * @param title Titolo della playlist.
     * @param criteria Criterio di generazione automatica.
     * @param genre Genere musicale usato come filtro.
     * @param year Anno usato come filtro.
     */
    private AutomaticPlaylist(String title, Criteria criteria, String genre, Integer year) {
        super(title);
        this.criteria = criteria;
        this.genre = genre;
        this.year = year;
    }

    /**
     * Crea una playlist automatica filtrata per genere musicale.
     *
     * @param genre Genere musicale scelto come criterio di filtro.
     * @return La playlist automatica generata per genere.
     * @throws IllegalArgumentException Se il genere è nullo o vuoto.
     */
    public static AutomaticPlaylist byGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException("Il genere non può essere vuoto.");
        }

        String normalizedGenre = genre.trim();

        return new AutomaticPlaylist(
                "Playlist " + normalizedGenre,
                Criteria.GENRE,
                normalizedGenre,
                null
        );
    }

    /**
     * Crea una playlist automatica filtrata per anno di uscita.
     *
     * @param year Anno scelto come criterio di filtro.
     * @return La playlist automatica generata per anno.
     * @throws IllegalArgumentException Se l'anno è nullo.
     */
    public static AutomaticPlaylist byYear(Integer year) {
        if (year == null) {
            throw new IllegalArgumentException("L'anno non può essere vuoto.");
        }

        return new AutomaticPlaylist(
                "Playlist " + year,
                Criteria.YEAR,
                null,
                year
        );
    }


    /**
     * Restituisce i brani appartenenti alla playlist automatica.
     *
     * Il metodo legge i brani presenti nella libreria e restituisce
     * solo quelli che rispettano il criterio scelto, cioè genere
     * musicale oppure anno di uscita.
     *
     * @return La lista dei brani che rispettano il criterio della playlist.
     */
    @Override
    public List<Track> getTracks() {
        List<Track> libraryTracks = Library.getInstance().getTracks();

        if (criteria == Criteria.GENRE) {
            return libraryTracks.stream()
                    .filter(track -> track.getGenre() != null)
                    .filter(track -> track.getGenre().equalsIgnoreCase(genre))
                    .collect(Collectors.toList());
        }

        if (criteria == Criteria.YEAR) {
            return libraryTracks.stream()
                    .filter(track -> track.getYear() != null)
                    .filter(track -> track.getYear().equals(year))
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    /**
     * Impedisce l'aggiunta manuale di brani alla playlist automatica.
     *
     * Il contenuto di una playlist automatica deve dipendere solo
     * dal criterio scelto, non da modifiche manuali.
     *
     * @param track Il brano che si tenta di aggiungere.
     * @throws UnsupportedOperationException Sempre, perché l'operazione non è consentita.
     */
    @Override
    public void addTrack(Track track) {
        throw new UnsupportedOperationException("Non puoi aggiungere manualmente brani a una playlist automatica.");
    }


    /**
     * Impedisce la rimozione manuale di brani dalla playlist automatica.
     *
     * Per rimuovere un brano da una playlist automatica bisogna
     * modificare il metadato su cui si basa il filtro.
     *
     * @param track Il brano che si tenta di rimuovere.
     * @return Sempre false, perché la rimozione manuale non è consentita.
     */
    @Override
    public boolean removeTrack(Track track) {
        return false;
    }


    /**
     * Impedisce l'aggiunta manuale di elementi riproducibili.
     *
     * La playlist automatica mantiene la compatibilità con il Composite,
     * ma il suo contenuto deve essere calcolato solo dal criterio scelto.
     *
     * @param element L'elemento riproducibile che si tenta di aggiungere.
     * @throws UnsupportedOperationException Sempre, perché l'operazione non è consentita.
     */
    @Override
    public void add(Playable element) {
        throw new UnsupportedOperationException("Non puoi modificare manualmente una playlist automatica.");
    }


    /**
     * Impedisce la rimozione manuale di elementi riproducibili.
     *
     * Non viene eseguita nessuna operazione perché la playlist
     * automatica si aggiorna dinamicamente leggendo i dati dalla libreria.
     *
     * @param element L'elemento riproducibile che si tenta di rimuovere.
     */
    @Override
    public void remove(Playable element) {
        // Non serve rimuovere manualmente: la playlist si aggiorna da sola.
    }
}