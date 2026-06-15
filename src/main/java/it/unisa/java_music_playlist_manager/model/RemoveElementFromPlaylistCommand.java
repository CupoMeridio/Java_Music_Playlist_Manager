package it.unisa.java_music_playlist_manager.model;

public class RemoveElementFromPlaylistCommand implements Command {
    private final ManualPlaylist playlist;
    private final Playable element;

    public RemoveElementFromPlaylistCommand(ManualPlaylist playlist, Playable element) {
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