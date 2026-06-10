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
public class AutomaticPlaylistByYear extends Playlist {
    
    private final int yearFilter;
    public AutomaticPlaylistByYear(String title, int yearFilter) {
        super(title);
        this.yearFilter=yearFilter;
    }

   @Override
    public List<Track> getTracks() {
        // Interroga la libreria centrale in tempo reale
        return Library.getInstance().getTracks().stream()
                // Assicura che la traccia abbia un anno assegnato (evita problemi con Integer nulli)
                .filter(track -> track.getYear() != null)
                // Verifica la corrispondenza dell'anno
                .filter(track -> track.getYear() == yearFilter)
                // Colleziona il risultato in una lista
                .collect(Collectors.toList());
    }
}
