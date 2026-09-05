package it.unisa.java_music_playlist_manager.services;

import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Tag;
import it.unisa.java_music_playlist_manager.model.Track;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LibrarySearchService {

    public static List<Track> filterTracks(List<Track> tracks, String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return tracks;
        }
        return tracks.stream()
                .filter(track -> matchesTrackSearch(track, searchQuery))
                .collect(Collectors.toList());
    }

    public static List<Playlist> filterPlaylists(List<Playlist> playlists, String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return playlists;
        }
        return playlists.stream()
                .filter(playlist -> matchesPlaylistSearch(playlist, searchQuery))
                .collect(Collectors.toList());
    }

    private static boolean matchesTrackSearch(Track track, String searchQuery) {
        String query = normalizedSearchQuery(searchQuery);
        if (query.isBlank()) {
            return true;
        }

        return containsSearch(track.getTitle(), query)
                || containsSearch(track.getAuthor(), query)
                || containsSearch(track.getAlbum(), query)
                || containsSearch(track.getGenre(), query)
                || containsAnyTag(track, query);
    }

    private static boolean matchesPlaylistSearch(Playlist playlist, String searchQuery) {
        String query = normalizedSearchQuery(searchQuery);
        return query.isBlank() || containsSearch(playlist.getTitle(), query);
    }

    private static boolean containsAnyTag(Track track, String query) {
        if (track.getTags() == null) {
            return false;
        }

        for (Tag tag : track.getTags()) {
            if (tag != null && containsSearch(tag.getName(), query)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsSearch(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private static String normalizedSearchQuery(String searchQuery) {
        return searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
    }
}
