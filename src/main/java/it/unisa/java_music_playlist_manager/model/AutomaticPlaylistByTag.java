/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Mattia Sanzari
 */
public class AutomaticPlaylistByTag extends Playlist {
    Tag filterTag;
    public AutomaticPlaylistByTag(String title, Tag filterTag) {
        super(title);
        this.filterTag = filterTag;
    }

   @Override
    public List<Track> getTracks() {
        // Interroga la libreria centrale in tempo reale
        return Library.getInstance().getTracks().stream()
                // Evita NullPointerException se una traccia non ha il set di tag inizializzato
                .filter(track -> track.getTags() != null)
                // Verifica se il tag cercato è presente tra quelli della traccia
                .filter(track -> track.getTags().contains(filterTag))
                // Colleziona il risultato in una lista
                .collect(Collectors.toList());
    }
    
}
