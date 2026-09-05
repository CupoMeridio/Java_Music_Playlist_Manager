
package it.unisa.java_music_playlist_manager.model.generator;

import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Track;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Prodotto concreto di {@link Playlist} automatica che raggruppa dinamicamente le tracce
 * pubblicate in uno specifico anno.
 */
public class AutomaticPlaylistByYear extends Playlist {
    
    private final int yearFilter;
    
    @JsonCreator
    public AutomaticPlaylistByYear(
            @JsonProperty("id") String id,
            @JsonProperty("title") String title,
            @JsonProperty("playCount") int playCount,
            @JsonProperty("yearFilter") int yearFilter) {
        super(id, title, playCount);
        this.yearFilter = yearFilter;
    }
    
    public AutomaticPlaylistByYear(String title, int yearFilter) {
        this(null, title, 0, yearFilter);
    }

   @Override
    public List<Track> getTracks() {
        return resolveLibrary().getTracks().stream()
                .filter(track -> track.getYear() != null)
                .filter(track -> track.getYear() == yearFilter)
                .collect(Collectors.toList());
    }
    
    public int getYearFilter() {
        return yearFilter;
    }
}
