package it.unisa.java_music_playlist_manager.model;

public class AddElementToPlaylistCommand implements Command {
    private final ManualPlaylist playlist;
    private final Playable element;

    public AddElementToPlaylistCommand(ManualPlaylist playlist, Playable element) {
        this.playlist = playlist;
        this.element = element;
    }

    @Override
    public void execute() {
        playlist.add(element);
        Library.getInstance().notifyObservers(); // Sveglia le viste
    }

    @Override
    public void undo() {
        playlist.remove(element);
        Library.getInstance().notifyObservers();
    }
}