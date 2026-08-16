package it.unisa.java_music_playlist_manager.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Definisce i criteri di ordinamento disponibili per le tracce musicali.
 * Supporta l'ordinamento per data di inserimento, titolo, artista, album, anno e durata.
 */
public enum TrackSortOption {
    INSERTION_ORDER("Data di inserimento", null),
    TITLE_ASC("Titolo (A - Z)", Comparator.comparing(Track::getTitle, String.CASE_INSENSITIVE_ORDER)),
    TITLE_DESC("Titolo (Z - A)", Comparator.comparing(Track::getTitle, String.CASE_INSENSITIVE_ORDER).reversed()),
    ARTIST_ASC("Artista (A - Z)", Comparator.comparing(Track::getAuthor, String.CASE_INSENSITIVE_ORDER)),
    ARTIST_DESC("Artista (Z - A)", Comparator.comparing(Track::getAuthor, String.CASE_INSENSITIVE_ORDER).reversed()),
    ALBUM_ASC("Album (A - Z)", Comparator.comparing(Track::getAlbum, String.CASE_INSENSITIVE_ORDER)),
    YEAR_DESC("Anno (Più recenti)", Comparator.comparing(Track::getYear, Comparator.nullsLast(Comparator.reverseOrder()))),
    YEAR_ASC("Anno (Meno recenti)", Comparator.comparing(Track::getYear, Comparator.nullsLast(Comparator.naturalOrder()))),
    DURATION_ASC("Durata (Crescente)", Comparator.comparingInt(Track::getDuration)),
    DURATION_DESC("Durata (Decrescente)", Comparator.comparingInt(Track::getDuration).reversed());

    private final String displayName;
    private final Comparator<Track> comparator;

    TrackSortOption(String displayName, Comparator<Track> comparator) {
        this.displayName = displayName;
        this.comparator = comparator;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Comparator<Track> getComparator() {
        return comparator;
    }

    /**
     * Ordina la lista di tracce fornita secondo il criterio specificato.
     * Se il criterio è INSERTION_ORDER, restituisce una copia dell'ordine originale.
     *
     * @param tracks La lista di tracce da ordinare.
     * @return Una nuova lista contenente le tracce ordinate.
     */
    public List<Track> sort(List<Track> tracks) {
        if (tracks == null) {
            return Collections.emptyList();
        }
        List<Track> result = new ArrayList<>(tracks);
        if (comparator != null) {
            result.sort(comparator);
        }
        return result;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
