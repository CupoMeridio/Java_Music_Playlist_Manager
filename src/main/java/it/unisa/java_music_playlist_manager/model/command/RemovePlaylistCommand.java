package it.unisa.java_music_playlist_manager.model.command;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.Playlist;

/**
 * Comando per l'eliminazione di una playlist dalla {@link Library}.
 * Consente di ripristinare la playlist eliminata tramite l'operazione di Undo.
 */
public class RemovePlaylistCommand implements Command {
    private final Library library;
    private final Playlist playlist;

    public RemovePlaylistCommand(Library library, Playlist playlist) {
        this.library = library;
        this.playlist = playlist;
    }

    @Override
    public void execute() {
        library.removePlaylist(playlist);
        library.notifyObservers();
    }

    @Override
    public void undo() {
        library.addPlaylist(playlist);
        library.notifyObservers();
    }
}