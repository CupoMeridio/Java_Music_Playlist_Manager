package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Supplier;

/**
 * Rappresenta una playlist automatica base (Pattern Factory Method & Template Method).
 * Implementa l'interfaccia Playlist (non estende ManualPlaylist).
 */
public abstract class AutomaticPlaylist implements Playlist {

    private final String title;
    protected final Supplier<List<Track>> trackSource;

    public AutomaticPlaylist(String title, Supplier<List<Track>> trackSource) {
        this.title = title;
        this.trackSource = trackSource;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        throw new UnsupportedOperationException("Non puoi modificare il titolo di una playlist automatica dopo la creazione.");
    }

    @Override
    public int getTrackCount() {
        return getTracks().size();
    }

    @Override
    public int getDuration() {
        return getTracks().stream().mapToInt(Track::getDuration).sum();
    }

    /**
     * Template Method: filtra la libreria utilizzando il criterio definito dalle sottoclassi.
     */
    @Override
    public List<Track> getTracks() {
        return trackSource.get().stream()
                .filter(this::matchCriteria)
                .collect(Collectors.toList());
    }

    /**
     * Primitive Operation delegata alle sottoclassi concrete.
     * @param track La traccia da verificare.
     * @return true se la traccia rispetta il criterio della playlist, false altrimenti.
     */
    protected abstract boolean matchCriteria(Track track);
}