/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unisa.java_music_playlist_manager.model;
import java.util.ArrayList;
import java.util.List;


public class Library implements Subject{
    
    private static Library instance;

    private final List<Track> tracks;
    private final List<Playlist> playlists;
    private final List<Observer> observers;

    private Library() {
        this.tracks = new ArrayList<>();
        this.playlists = new ArrayList<>();
        this.observers = new ArrayList<>();
    }
    
    public static synchronized Library getInstance() {
        if (instance == null) {
            instance = new Library();
        }
        return instance;
    }

    public void addTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un brano nullo.");
        }

        this.tracks.add(track);
        notifyObservers();
    }
    
    // ritorna una copia della lista
    public List<Track> getTracks() {
        return new ArrayList<>(tracks);
    }


    public boolean removeTrack(Track track) {    // la rendo booleana per eventuali controlli
        if (track == null) {
            return false;
        }
        boolean isRemoved = this.tracks.remove(track);

        // se la traccia è stata effettivamente trovata e rimossa, eseguiamo le azioni successive
        if (isRemoved) {

            for (Playlist playlist : this.playlists) {
                playlist.removeTrack(track);
            }

            notifyObservers();
        }
        return isRemoved;
    }

    public void addPlaylist(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("Impossibile aggiungere una playlist nulla.");
        }

        this.playlists.add(playlist);
        notifyObservers();
    }

    public boolean removePlaylist(Playlist playlist) {
        if (playlist == null) {
            return false;
        }

        boolean isRemoved = this.playlists.remove(playlist);

        if (isRemoved) {
            notifyObservers();
        }

        return isRemoved;
    }

    public List<Playlist> getPlaylists() {
        return new ArrayList<>(playlists);
    }

    // Metodi interfaccia Subject
    @Override
    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }
}
