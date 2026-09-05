package it.unisa.java_music_playlist_manager.model;

import it.unisa.java_music_playlist_manager.model.command.Command;

/**
 * Comando per l'aggiunta di una playlist alla {@link Library}.
 * Permette il ripristino dello stato precedente (rimozione) in caso di Undo.
 */
public class AddPlaylistCommand implements Command {
    private final Library library;
    private final Playlist playlist;

    public AddPlaylistCommand(Library library, Playlist playlist) {
        this.library = library;
        this.playlist = playlist;
    }

    @Override
    public void execute() {
        library.addPlaylist(playlist);
        library.notifyObservers();
    }

    @Override
    public void undo() {
        library.removePlaylist(playlist);
        library.notifyObservers();
    }
}