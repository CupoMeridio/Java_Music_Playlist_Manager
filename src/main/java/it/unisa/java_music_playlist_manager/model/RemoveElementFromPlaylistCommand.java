package it.unisa.java_music_playlist_manager.model;

/**
 * Comando per la rimozione di un elemento {@link Playable} da una {@link MutablePlaylist}.
 * Consente di ripristinare l'elemento rimosso in caso di Undo.
 */
public class RemoveElementFromPlaylistCommand implements Command {
    private final MutablePlaylist playlist;
    private final Playable element;

    public RemoveElementFromPlaylistCommand(MutablePlaylist playlist, Playable element) {
        this.playlist = playlist;
        this.element = element;
    }

    @Override
    public void execute() {
        playlist.remove(element);
        Library.getInstance().notifyObservers();
    }

    @Override
    public void undo() {
        playlist.add(element);
        Library.getInstance().notifyObservers();
    }
}