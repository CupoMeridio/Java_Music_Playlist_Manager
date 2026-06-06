package it.unisa.java_music_playlist_manager.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Playlist implements Playable {
    
    private String title; 
    private final List<Playable> elements;
    private final String id;

    public Playlist(String title) {
        this.id = UUID.randomUUID().toString();
        this.setTitle(title);
        this.elements = new ArrayList<>();
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della playlist non può essere vuoto o nullo.");
        }
        this.title = title;
    }

    public void add(Playable element) {
        if (element == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un componente nullo.");
        }
        if (element == this) {
            throw new IllegalArgumentException("Una playlist non può contenere se stessa.");
        }
        if (element instanceof Playlist playlist && playlist.containsRecursive(this)) {
            throw new IllegalArgumentException("Impossibile creare una dipendenza ciclica tra playlist.");
        }
        if (!elements.contains(element)) {
            elements.add(element);
        }
    }

    public void remove(Playable element) {
        if (element == null) {
            return;
        }
        elements.remove(element);
    }

    public boolean contains(Playable element) {
        return elements.contains(element);
    }

    private boolean containsRecursive(Playable target) {
        for (Playable element : elements) {
            if (element.equals(target)) {
                return true;
            }
            if (element instanceof Playlist playlist && playlist.containsRecursive(target)) {
                return true;
            }
        }
        return false;
    }

    public int getTrackCount() {
        return getTracks().size();
    }

    @Override
    public List<Track> getTracks() {
        List<Track> allTracks = new ArrayList<>();
        // Sfrutta la ricorsione del Composite per raccogliere tutte le tracce
        for (Playable element : elements) {
            allTracks.addAll(element.getTracks());
        }
        return allTracks;
    }

    public void addTrack(Track track) {
        add(track);
    }

    public boolean removeTrack(Track track) {
        if (track == null) return false;
        return elements.remove(track);
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public int getDuration() {
        int duration = 0;
        for (Track t : getTracks()) {
            duration += t.getDuration();
        }
        return duration;
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

    @Override
    public String toString() {
        return title;
    }
}
