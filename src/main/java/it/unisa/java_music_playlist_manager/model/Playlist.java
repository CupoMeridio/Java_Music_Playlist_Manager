/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unisa.java_music_playlist_manager.model;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Playlist implements Playable{
    
    private String title; 
    private Set <Playable> tracks ;
    private final String id;

    public Playlist(String title) {
        this.id = UUID.randomUUID().toString();
        this.setTitle(title);
        this.tracks =  new LinkedHashSet<>();
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della playlist non può essere vuoto o nullo.");
        }
        this.title = title;
    }
    
    private int computeDurationPlaylist( Set <Playable> collezione){
        int duration =0;
        for (Playable i : collezione) {
            
            duration+= i.getDuration();
        }
        return duration;
    }

    public boolean contains(Playable component) {
        return tracks.contains(component);
    }

    public int getTrackCount() {
        return getTracks().size();
    }

    public List<Track> getTracks() {
        List<Track> result = new ArrayList<>();
        collectTracks(this.tracks, result);
        return result;
    }

    private void collectTracks(Set<Playable> components, List<Track> result) {
        for (Playable component : components) {
            if (component instanceof Track track) {
                result.add(track);
            } else if (component instanceof Playlist playlist) {
                result.addAll(playlist.getTracks());
            }
        }
    }
    
    @Override
    public void play() {
        System.out.println("Riproduzione della playlist: " + title);
        for (Playable component : tracks) {
            component.play();
        }
    }

    @Override
    public void add(Playable component) {
       if (component == null) throw new IllegalArgumentException("Impossibile aggiungere un componente nullo.");
        tracks.add(component); // Il Set rifiuterà automaticamente l'elemento se già presente
    }

    @Override
    public boolean remove(Playable component) {
       return tracks.remove(component);
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public int getDuration() {
        return computeDurationPlaylist( this.tracks);
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
