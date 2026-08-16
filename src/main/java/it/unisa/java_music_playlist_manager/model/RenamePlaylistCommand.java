package it.unisa.java_music_playlist_manager.model;

/**
 * Comando per la rinominazione di una {@link Playlist}.
 * Mantiene il nome precedente per consentire il ripristino in caso di Undo.
 */
public class RenamePlaylistCommand implements Command {
    private final Playlist playlist;
    private final String oldName;
    private final String newName;

    public RenamePlaylistCommand(Playlist playlist, String newName) {
        this.playlist = playlist;
        this.newName = newName;
        this.oldName = playlist.getTitle(); // Memorizza il nome precedente per l'undo
    }

    @Override
    public void execute() {
        playlist.setTitle(newName);
        Library.getInstance().notifyObservers(); // Aggiorna la UI (sidebar e tabelle)
    }

    @Override
    public void undo() {
        playlist.setTitle(oldName); // Ripristina il vecchio nome
        Library.getInstance().notifyObservers();
    }
}