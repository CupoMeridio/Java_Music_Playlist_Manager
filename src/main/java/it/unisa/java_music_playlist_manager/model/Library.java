/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unisa.java_music_playlist_manager.model;
import java.util.ArrayList;
import java.util.List;


public class Library {
    
    private static Library instance;
    
    private final List<Track> tracks;
    // private final List<Observer> observers;
    
    private Library() {
        this.tracks = new ArrayList<>();
        // this.observers = new ArrayList<>();
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

        // quando implementeremo observer, andrà qui la notifica per il controller
        // notifyObservers();
    }
    
    // ritorna una copia della lista
    public List<Track> getTracks() {
        return new ArrayList<>(tracks);
    }
    
    public void removeTrack(Track track) {
        if (track == null) {
            return; 
        }
        boolean isRemoved = this.tracks.remove(track);

        // se la traccia è stata effettivamente trovata e rimossa, eseguiamo le azioni successive
        if (isRemoved) {
            
            // QUANDO LA PLAYLIST SARà IMPLEMENTATA
            
            // for (Playlist playlist : this.playlists) {
            //    playlist.removeTrack(track);
            //}
        }
    }
    
    // qui potrebbero andare in futuro i metodi da overridare per renderla una subject
}
