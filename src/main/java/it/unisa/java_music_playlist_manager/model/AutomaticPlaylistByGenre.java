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
public class AutomaticPlaylistByGenre extends Playlist {
    private final String genreFilter;

    public AutomaticPlaylistByGenre(String title, String genreFilter) {
        super(title);
        this.genreFilter = genreFilter;
    }

    @Override
    public List<Track> getTracks() {
        return Library.getInstance().getTracks().stream()
                .filter(track -> track.getGenre() != null)
                .filter(track -> track.getGenre().equalsIgnoreCase(genreFilter))
                .collect(Collectors.toList());
    }
}
