/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unisa.java_music_playlist_manager.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Playlist {
    
    private String title; 
    private final List<Track> tracks;
    private final String id;

    public Playlist(String title) {
        this.id = UUID.randomUUID().toString();
        this.setTitle(title);
        this.tracks = new ArrayList<>();
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della playlist non può essere vuoto o nullo.");
        }
        this.title = title;
    }

    public boolean contains(Track track) {
        return tracks.contains(track);
    }

    public int getTrackCount() {
        return tracks.size();
    }

    public List<Track> getTracks() {
        return new ArrayList<>(tracks);
    }

    public void addTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un brano nullo.");
        }
        if (!tracks.contains(track)) {
            tracks.add(track);
        }
    }

    public boolean removeTrack(Track track) {
        if (track == null) {
            return false;
        }
        return tracks.remove(track);
    }

    public String getTitle() {
        return this.title;
    }

    public int getDuration() {
        int duration = 0;
        for (Track t : tracks) {
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
