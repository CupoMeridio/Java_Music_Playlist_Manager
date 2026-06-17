
package it.unisa.java_music_playlist_manager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ManualPlaylist.class, name = "ManualPlaylist"),
    @JsonSubTypes.Type(value = AutomaticPlaylistByYear.class, name = "AutomaticPlaylistByYear"),
    @JsonSubTypes.Type(value = AutomaticPlaylistByTag.class, name = "AutomaticPlaylistByTag"),
    @JsonSubTypes.Type(value = AutomaticPlaylistByGenre.class, name = "AutomaticPlaylistByGenre")
})
public abstract class Playlist implements Playable {
    
    private final String id;
    private String title;
    private int playCount = 0;

    @JsonCreator
    public Playlist(
            @JsonProperty("id") String id, 
            @JsonProperty("title") String title, 
            @JsonProperty("playCount") int playCount) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo della playlist non può essere vuoto.");
        }
        this.id = (id != null) ? id : UUID.randomUUID().toString();
        this.title = title.trim();
        this.playCount = playCount;
    }
    
    public Playlist(String title) {
        this(null, title, 0);
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo non può essere vuoto.");
        }
        this.title = title.trim();
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }
    
    
    public String getId() {
        return this.id;
    }

    public int getPlayCount() {
        return this.playCount;
    }

    public void incrementPlayCount() {
        this.playCount++;
    }

    @JsonIgnore
    public boolean isManuallyEditable() {
        return false;
    }

    @Override
    public abstract List<Track> getTracks();  

    @JsonIgnore
    public int getDuration() {
        return getTracks().stream()
                .mapToInt(Track::getDuration)
                .sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return Objects.equals(id, playlist.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @JsonIgnore
    public int getTrackCount() {
        return getTracks().size();
    }

    @Override
    public String toString() {
        return title;
    }
}
