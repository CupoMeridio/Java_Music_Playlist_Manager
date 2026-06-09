package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import java.util.function.Supplier;

public class AutomaticPlaylistByGenre extends AutomaticPlaylist {
    private final String genre;

    public AutomaticPlaylistByGenre(String genre, Supplier<List<Track>> trackSource) {
        super("Playlist " + genre.trim(), trackSource);
        this.genre = genre.trim();
    }

    @Override
    protected boolean matchCriteria(Track track) {
        return track.getGenre() != null && track.getGenre().equalsIgnoreCase(this.genre);
    }
}
