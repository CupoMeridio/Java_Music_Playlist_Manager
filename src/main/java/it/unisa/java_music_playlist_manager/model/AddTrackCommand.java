package it.unisa.java_music_playlist_manager.model;


import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.Track;

public class AddTrackCommand implements Command {
    private final Library library;
    private final Track track;

    public AddTrackCommand(Library library, Track track) {
        this.library = library;
        this.track = track;
    }

    @Override
    public void execute() {
        library.addTrack(track);
    }

    @Override
    public void undo() {
        library.removeTrack(track);
    }
}