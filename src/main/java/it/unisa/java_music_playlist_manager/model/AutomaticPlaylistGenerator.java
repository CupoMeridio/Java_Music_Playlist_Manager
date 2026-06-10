package it.unisa.java_music_playlist_manager.model;

/**
 *
 * @author Mattia Sanzari
 */
/**
 * Creatore concreto per playlist dinamiche guidate da criteri.
 * Contiene la logica di selezione della specifica sottoclasse di playlist automatica.
 */
public class AutomaticPlaylistGenerator extends PlaylistGenerator {
    
    /**
     * Spostato qui dal vecchio prodotto: definisce i criteri di generazione supportati.
     */
    public enum Criteria {
        GENRE,
        YEAR,
        TAG
    }

    private final Criteria criteria;
    private final Object filterValue;

    /**
     * Costruisce il generatore impostando la configurazione del filtro.
     * * @param criteria Il tipo di filtro automatico (GENRE, YEAR, TAG).
     * @param filterValue Il valore associato al filtro (es. "Rock", 2026, "Favorites").
     */
    public AutomaticPlaylistGenerator(Criteria criteria, Object filterValue) {
        if (criteria == null || filterValue == null) {
            throw new IllegalArgumentException("Criterio e valore di filtraggio non possono essere nulli.");
        }
        this.criteria = criteria;
        this.filterValue = filterValue;
    }

    @Override
    public Playable createPlaylist(String title) {
        switch (criteria) {
            case GENRE:
                return new AutomaticPlaylistByGenre(title, (String) filterValue);
                
            case YEAR:
                if (filterValue instanceof Integer year) {
                    return new AutomaticPlaylistByYear(title, year);
                }
                throw new IllegalArgumentException("Il criterio YEAR richiede un valore intero.");
                
            case TAG:
                return new AutomaticPlaylistByTag(title, (Tag) filterValue);
                
            default:
                throw new UnsupportedOperationException("Criterio di generazione sconosciuto.");
        }
    }
}