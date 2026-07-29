
package it.unisa.java_music_playlist_manager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Prodotto concreto di {@link Playlist} automatica che raggruppa dinamicamente le tracce
 * che possiedono uno specifico {@link Tag}.
 */
public class AutomaticPlaylistByTag extends Playlist {
    private Tag filterTag;
    
    @JsonCreator
    public AutomaticPlaylistByTag(
            @JsonProperty("id") 
                    String id,
            @JsonProperty("title") 
                    String title,
            @JsonProperty("playCount") 
                    int playCount,
            @JsonProperty("filterTag") 
                    Tag filterTag) {
        super(id, title, playCount);
        this.filterTag = filterTag;
    }
    
    public AutomaticPlaylistByTag(String title, Tag filterTag) {
        this(null, title, 0, filterTag);
    }

   @Override
    public List<Track> getTracks() {
        // Interroga la library risolta (o il Singleton di fallback) in tempo reale
        return resolveLibrary().getTracks().stream()
                // Evita NullPointerException se una traccia non ha il set di tag inizializzato
                .filter(track -> track.getTags() != null)
                // Verifica se il tag cercato è presente tra quelli della traccia
                .filter(track -> track.getTags().contains(filterTag))
                // Colleziona il risultato in una lista
                .collect(Collectors.toList());
    }
    
    public Tag getFilterTag() {
        return filterTag;
    }
}
