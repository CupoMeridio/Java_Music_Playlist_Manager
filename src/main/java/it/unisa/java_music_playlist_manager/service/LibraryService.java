package it.unisa.java_music_playlist_manager.service;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Track;

/**
 * Service class that acts as a service layer between the UI
 * and the domain model. Handles creation, modification, deletion, and relations
 * between tracks and playlists, shielding the controller from direct instantiation/mutations.
 */
public class LibraryService {

    private static LibraryService instance;
    private final Library library;

    private LibraryService() {
        this.library = Library.getInstance();
    }

    public static synchronized LibraryService getInstance() {
        if (instance == null) {
            instance = new LibraryService();
        }
        return instance;
    }

    /**
     * Creates and registers a new track in the library.
     */
    public Track addTrack(String title, String author, int duration, String genre, int year) {
        Track newTrack = new Track(title, author, duration, genre, year);
        library.addTrack(newTrack);
        return newTrack;
    }

    /**
     * Updates an existing track's metadata.
     */
    public void updateTrack(Track track, String title, String author, int duration, String genre, int year) {
        if (track == null) {
            throw new IllegalArgumentException("Impossibile modificare un brano nullo.");
        }
        track.setTitle(title);
        track.setAuthor(author);
        track.setDuration(duration);
        track.setGenre(genre);
        track.setYear(year);
        // Force library update notification since track details changed
        library.notifyObservers();
    }

    /**
     * Removes a track from the library (and automatically from all playlists).
     */
    public boolean deleteTrack(Track track) {
        return library.removeTrack(track);
    }

    /**
     * Creates and registers a new playlist in the library.
     */
    public Playlist createPlaylist(String title) {
        Playlist playlist = new Playlist(title);
        library.addPlaylist(playlist);
        return playlist;
    }

    /**
     * Renames an existing playlist.
     */
    public void renamePlaylist(Playlist playlist, String newTitle) {
        if (playlist == null) {
            throw new IllegalArgumentException("Impossibile rinominare una playlist nulla.");
        }
        playlist.setTitle(newTitle);
        library.notifyObservers();
    }

    /**
     * Deletes a playlist from the library.
     */
    public boolean deletePlaylist(Playlist playlist) {
        return library.removePlaylist(playlist);
    }

    /**
     * Associates a track to a playlist.
     */
    public void addTrackToPlaylist(Track track, Playlist playlist) {
        if (playlist == null || track == null) {
            throw new IllegalArgumentException("Brano o playlist non possono essere nulli.");
        }
        playlist.addTrack(track);
        library.notifyObservers();
    }
}
