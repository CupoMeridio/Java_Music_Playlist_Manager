package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import java.util.function.Supplier;

public class AutomaticPlaylistByYear extends AutomaticPlaylist {
    private final Integer year;

    public AutomaticPlaylistByYear(Integer year, Supplier<List<Track>> trackSource) {
        super("Playlist " + year, trackSource);
        this.year = year;
    }

    @Override
    protected boolean matchCriteria(Track track) {
        return track.getYear() != null && track.getYear().equals(this.year);
    }
}
