package it.unisa.java_music_playlist_manager.model;



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