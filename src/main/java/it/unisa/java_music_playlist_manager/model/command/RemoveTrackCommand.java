package it.unisa.java_music_playlist_manager.model.command;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.ManualPlaylist;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * Comando per la rimozione di un brano dalla {@link Library}.
 * Mantiene la memoria delle playlist manuali che contenevano la traccia per consentire
 * il ripristino completo in caso di Undo.
 */
public class RemoveTrackCommand implements Command {
    private final Library library;
    private final Track track;
    private final List<ManualPlaylist> affectedPlaylists = new ArrayList<>();

    public RemoveTrackCommand(Library library, Track track) {
        this.library = library;
        this.track = track;
    }

    @Override
    public void execute() {
        // Prima di rimuovere, memorizzo in quali playlist manuali si trovava il brano
        for (Playlist p : library.getPlaylists()) {
            if (p instanceof ManualPlaylist mp && mp.contains(track)) {
                affectedPlaylists.add(mp);
            }
        }
        library.removeTrack(track);
    }

    @Override
    public void undo() {
        // Ripristino nella libreria
        library.addTrack(track);
        // Ripristino a cascata nelle singole playlist manuali
        for (ManualPlaylist mp : affectedPlaylists) {
            mp.addTrack(track);
        }
        library.notifyObservers(); // Forza l'aggiornamento grafico generale
    }
}