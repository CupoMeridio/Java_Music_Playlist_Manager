
package it.unisa.java_music_playlist_manager.model;

import it.unisa.java_music_playlist_manager.model.generator.AutomaticPlaylistByYear;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Track.class, name = "Track"),
    @JsonSubTypes.Type(value = ManualPlaylist.class, name = "ManualPlaylist"),
    @JsonSubTypes.Type(value = AutomaticPlaylistByYear.class, name = "AutomaticPlaylistByYear"),
    @JsonSubTypes.Type(value = AutomaticPlaylistByTag.class, name = "AutomaticPlaylistByTag"),
    @JsonSubTypes.Type(value = AutomaticPlaylistByGenre.class, name = "AutomaticPlaylistByGenre")
})
public interface Playable {
    String getTitle();
    List<Track> getTracks(); 
}
