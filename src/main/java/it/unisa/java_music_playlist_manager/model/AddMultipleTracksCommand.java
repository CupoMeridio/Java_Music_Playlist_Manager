package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Comando per l'aggiunta di più tracce musicali alla {@link Library} contemporaneamente.
 * Implementa il pattern Command permettendo l'annullamento dell'aggiunta multipla in un colpo solo.
 */
public class AddMultipleTracksCommand implements Command {
    private final Library library;
    private final List<Track> tracks;
    private final List<Track> actuallyAdded;

    public AddMultipleTracksCommand(Library library, List<Track> tracks) {
        this.library = library;
        this.tracks = tracks;
        this.actuallyAdded = new ArrayList<>();
    }

    @Override
    public void execute() {
        actuallyAdded.clear();
        for (Track track : tracks) {
            try {
                library.addTrack(track);
                actuallyAdded.add(track);
            } catch (IllegalArgumentException e) {
                // Il brano è già presente o non valido. Lo ignoriamo in modo da
                // non bloccare l'importazione degli altri brani e non rimuoverlo con l'undo.
            }
        }
    }

    @Override
    public void undo() {
        for (Track track : actuallyAdded) {
            library.removeTrack(track);
        }
    }
}
