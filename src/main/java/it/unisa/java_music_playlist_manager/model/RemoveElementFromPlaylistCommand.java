package it.unisa.java_music_playlist_manager.model;

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