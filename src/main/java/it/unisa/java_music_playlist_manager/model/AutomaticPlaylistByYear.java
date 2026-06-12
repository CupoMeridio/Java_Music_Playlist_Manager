
package it.unisa.java_music_playlist_manager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;

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
        return Library.getInstance().getTracks().stream()
                .filter(track -> track.getYear() != null)
                .filter(track -> track.getYear() == yearFilter)
                .collect(Collectors.toList());
    }
    
    public int getYearFilter() {
        return yearFilter;
    }
}
