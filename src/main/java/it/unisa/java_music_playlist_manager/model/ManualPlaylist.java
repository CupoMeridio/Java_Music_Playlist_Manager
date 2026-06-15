
package it.unisa.java_music_playlist_manager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class ManualPlaylist extends Playlist {

    private final List<Playable> elements;
    
    @JsonCreator
    public ManualPlaylist(
            @JsonProperty("id") String id,
            @JsonProperty("title") String title,
            @JsonProperty("playCount") int playCount,
            @JsonProperty("elements") List<Playable> elements) {
        super(id, title, playCount);
        this.elements = (elements != null) ? elements : new ArrayList<>();
    }

    public ManualPlaylist(String title) {
        this(null, title, 0, new ArrayList<>());
    }

    @Override
    public void add(Playable element) {
        if (element == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un componente nullo.");
        }
        if (element == this) {
            throw new IllegalArgumentException("Una playlist non può contenere se stessa.");
        }
        if (element instanceof ManualPlaylist playlist && playlist.containsRecursive(this)) {
            throw new IllegalArgumentException("Impossibile creare una dipendenza ciclica tra playlist.");
        }
        
        elements.add(element);
    }

    @Override
    public void remove(Playable element) {
        if (element == null) {
            return ;
        }
        elements.remove(element);
    }
    
    @Override
    public void removeTrack(Track track){
        if (track == null) {
            return ;
        }
        elements.remove(track);
    }

    @Override
    public void moveElement(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= elements.size() || toIndex < 0 || toIndex > elements.size()) {
            throw new IndexOutOfBoundsException("Indici di spostamento non validi.");
        }
        if (fromIndex == toIndex) {
            return;
        }
        Playable element = elements.remove(fromIndex);
        if (toIndex > fromIndex) {
            toIndex--; 
        }
        elements.add(toIndex, element);
    }

    public boolean contains(Playable element) {
        return elements.contains(element);
    }

    private boolean containsRecursive(Playable target) {
        for (Playable element : elements) {
            if (element.equals(target)) {
                return true;
            }
            if (element instanceof ManualPlaylist playlist && playlist.containsRecursive(target)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Track> getTracks() {
        List<Track> allTracks = new ArrayList<>();
        for (Playable element : elements) {
            allTracks.addAll(element.getTracks());
        }
        return allTracks;
    }

    @Override
    public void addTrack(Track track) {
        add(track);
    }

    @Override
    public boolean isManuallyEditable() {
        return true;
    }
    
    public List<Playable> getElements() {
        return elements;
    }
}
