
package it.unisa.java_music_playlist_manager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;

public class AutomaticPlaylistByGenre extends Playlist {

    private final String genreFilter;

    @JsonCreator
    public AutomaticPlaylistByGenre(
            @JsonProperty("id") String id,
            @JsonProperty("title") String title,
            @JsonProperty("playCount") int playCount,
            @JsonProperty("genreFilter") String genreFilter) {
        super(id, title, playCount);
        this.genreFilter = genreFilter;
    }
    
    public AutomaticPlaylistByGenre(String title, String genreFilter) {
        this(null, title, 0, genreFilter);
    }

    @Override
    public List<Track> getTracks() {
        return Library.getInstance().getTracks().stream()
                .filter(track -> track.getGenre() != null)
                .filter(track -> track.getGenre().equalsIgnoreCase(genreFilter))
                .collect(Collectors.toList());
    }
    
    public String getGenreFilter() {
        return genreFilter;
    }
}
